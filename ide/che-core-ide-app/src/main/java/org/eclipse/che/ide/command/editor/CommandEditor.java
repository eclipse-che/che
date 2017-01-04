/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandManager.CommandChangedListener;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.command.editor.page.arguments.ArgumentsPage;
import org.eclipse.che.ide.command.editor.page.info.InfoPage;
import org.eclipse.che.ide.command.editor.page.previewurl.PreviewUrlPage;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.LinkedList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;

/**
 * Presenter for editing commands.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditor extends AbstractEditorPresenter implements CommandEditorView.ActionDelegate,
                                                                      CommandChangedListener {

    private final CommandEditorView        view;
    private final WorkspaceAgent           workspaceAgent;
    private final IconRegistry             iconRegistry;
    private final CommandManager           commandManager;
    private final NotificationManager      notificationManager;
    private final DialogFactory            dialogFactory;
    private final EditorAgent              editorAgent;
    private final CoreLocalizationConstant localizationConstants;
    private final EditorMessages           messages;
    private final NodeFactory              nodeFactory;
    private final CommandExecutor          commandExecutor;

    private final List<CommandEditorPage> pages;

    /** Edited command. */
    private ContextualCommand editedCommand;
    /** Initial (before any modification) name of the edited command. */
    private String            commandNameInitial;

    @Inject
    public CommandEditor(CommandEditorView view,
                         WorkspaceAgent workspaceAgent,
                         IconRegistry iconRegistry,
                         CommandManager commandManager,
                         InfoPage infoPage,
                         ArgumentsPage argumentsPage,
                         PreviewUrlPage previewUrlPage,
                         NotificationManager notificationManager,
                         DialogFactory dialogFactory,
                         EditorAgent editorAgent,
                         CoreLocalizationConstant localizationConstants,
                         EditorMessages messages,
                         NodeFactory nodeFactory,
                         CommandExecutor commandExecutor) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.iconRegistry = iconRegistry;
        this.commandManager = commandManager;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.editorAgent = editorAgent;
        this.localizationConstants = localizationConstants;
        this.messages = messages;
        this.nodeFactory = nodeFactory;
        this.commandExecutor = commandExecutor;

        view.setDelegate(this);

        commandManager.addCommandChangedListener(this);

        pages = new LinkedList<>();
        pages.add(infoPage);
        pages.add(argumentsPage);
        pages.add(previewUrlPage);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(getView());
    }

    @Override
    protected void initializeEditor(EditorAgent.OpenEditorCallback callback) {
        final VirtualFile file = getEditorInput().getFile();

        if (file instanceof CommandFileNode) {
            // make a copy of the given command to avoid modifying of the provided command
            editedCommand = new ContextualCommand(((CommandFileNode)file).getData());

            initializePages();

            for (CommandEditorPage page : pages) {
                view.addPage(page.getView(), page.getTitle(), page.getTooltip());
            }
        } else {
            callback.onInitializationFailed();
        }
    }

    /** Initialize editor's pages with the edited command. */
    private void initializePages() {
        commandNameInitial = editedCommand.getName();

        for (final CommandEditorPage page : pages) {
            page.setDirtyStateListener(new CommandEditorPage.DirtyStateListener() {
                @Override
                public void onDirtyStateChanged() {
                    updateDirtyState(page.isDirty());

                    view.setSaveEnabled(page.isDirty());
                }
            });

            page.edit(editedCommand);
        }
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        final VirtualFile file = getEditorInput().getFile();

        if (file instanceof CommandFileNode) {
            final ContextualCommand command = ((CommandFileNode)file).getData();
            final Icon icon = iconRegistry.getIconIfExist("command.type." + command.getType());

            if (icon != null) {
                final SVGImage svgImage = icon.getSVGImage();

                if (svgImage != null) {
                    return icon.getSVGResource();
                }
            }
        }

        return input.getSVGResource();
    }

    @Override
    public String getTitle() {
        return (isDirty() ? "* " : "") + input.getName();
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return input.getName();
    }

    @Override
    public void doSave() {
        doSave(new AsyncCallback<EditorInput>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(EditorInput result) {
            }
        });
    }

    @Override
    public void doSave(final AsyncCallback<EditorInput> callback) {
        commandManager.updateCommand(commandNameInitial, editedCommand).then(new Operation<ContextualCommand>() {
            @Override
            public void apply(ContextualCommand arg) throws OperationException {
                updateDirtyState(false);

                if (!commandNameInitial.equals(editedCommand.getName())) {
                    input.setFile(nodeFactory.newCommandFileNode(editedCommand));
                }

                initializePages();

                callback.onSuccess(getEditorInput());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(messages.editorMessageUnableToSave(),
                                           arg.getMessage(),
                                           WARNING,
                                           EMERGE_MODE);

                callback.onFailure(arg.getCause());

                throw new OperationException(arg.getMessage());
            }
        });
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void activate() {
    }

    @Override
    public void close(boolean save) {
        workspaceAgent.removePart(this);
    }

    @Override
    public void onClose(final AsyncCallback<Void> callback) {
        // TODO: find the right place for this code since #onClose is never calling
        if (!isDirty()) {
            handleClose();
            callback.onSuccess(null);
        } else {
            dialogFactory.createConfirmDialog(
                    localizationConstants.askWindowCloseTitle(),
                    localizationConstants.messagesSaveChanges(getEditorInput().getName()),
                    new ConfirmCallback() {
                        @Override
                        public void accepted() {
                            doSave();
                            handleClose();
                            callback.onSuccess(null);
                        }
                    },
                    new CancelCallback() {
                        @Override
                        public void cancelled() {
                            handleClose();
                            callback.onSuccess(null);
                        }
                    }).show();
        }
    }

    @Override
    public void onCommandSave() {
        doSave();
    }

    @Override
    public void onCommandTest() {
        commandExecutor.executeCommand(editedCommand);
    }

    @Override
    public void onCommandAdded(ContextualCommand command) {
    }

    @Override
    public void onCommandUpdated(ContextualCommand command) {
    }

    @Override
    public void onCommandRemoved(ContextualCommand command) {
        if (command.getName().equals(editedCommand.getName())) {
            editorAgent.closeEditor(this);
        }
    }
}
