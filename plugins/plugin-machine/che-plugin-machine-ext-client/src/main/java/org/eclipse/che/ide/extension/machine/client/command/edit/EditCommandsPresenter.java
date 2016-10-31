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
package org.eclipse.che.ide.extension.machine.client.command.edit;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandPage.DirtyStateListener;
import org.eclipse.che.ide.api.command.CommandPage.FieldStateActionDelegate;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.dialogs.ChoiceDialog;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Presenter for managing commands.
 *
 * @author Artem Zatsarynnyi
 * @author Oleksii Orel
 * @author Valeriy Svydenko
 */
@Singleton
public class EditCommandsPresenter implements EditCommandsView.ActionDelegate, FieldStateActionDelegate {

    public static final String PREVIEW_URL_ATTR = "previewUrl";

    private final EditCommandsView            view;
    private final CommandManager              commandManager;
    private final CommandTypeRegistry         commandTypeRegistry;
    private final DialogFactory               dialogFactory;
    private final MachineLocalizationConstant machineLocale;
    private final CoreLocalizationConstant    coreLocale;

    private final Comparator<CommandImpl> commandsComparator;

    // initial name of the currently edited command
    String editedCommandNameInitial;
    // initial preview URL of the currently edited command
    String editedCommandPreviewUrlInitial;

    CommandProcessingCallback commandProcessingCallback;

    private CommandPage editedPage;
    // command that being edited
    private CommandImpl editedCommand;

    @Inject
    protected EditCommandsPresenter(EditCommandsView view,
                                    CommandManager commandManager,
                                    CommandTypeRegistry commandTypeRegistry,
                                    DialogFactory dialogFactory,
                                    MachineLocalizationConstant machineLocale,
                                    CoreLocalizationConstant coreLocale) {
        this.view = view;
        this.commandManager = commandManager;
        this.commandTypeRegistry = commandTypeRegistry;
        this.dialogFactory = dialogFactory;
        this.machineLocale = machineLocale;
        this.coreLocale = coreLocale;
        this.view.setDelegate(this);

        commandsComparator = new Comparator<CommandImpl>() {
            @Override
            public int compare(CommandImpl o1, CommandImpl o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
    }

    @Override
    public void onCloseClicked() {
        onNameChanged();

        final CommandImpl selectedCommand = view.getSelectedCommand();
        if (selectedCommand != null && isViewModified()) {
            onCommandSelected(selectedCommand);
        }

        view.close();
    }

    @Override
    public void onSaveClicked() {
        final CommandImpl selectedCommand = view.getSelectedCommand();
        if (selectedCommand == null) {
            return;
        }

        updateCommand(selectedCommand).then(new Operation<CommandImpl>() {
            @Override
            public void apply(CommandImpl arg) throws OperationException {
                commandProcessingCallback = getCommandProcessingCallback();
                refreshView();
            }
        });
    }

    private Promise<CommandImpl> updateCommand(final CommandImpl command) {
        return commandManager.update(editedCommandNameInitial, command)
                             .then(new Operation<CommandImpl>() {
                                 @Override
                                 public void apply(CommandImpl updatedCommand) throws OperationException {
                                     editedPage.onSave();

                                     if (!command.getName().equals(updatedCommand.getName())) {
                                         onNameChanged();
                                     }
                                 }
                             })
                             .catchError(new Operation<PromiseError>() {
                                 @Override
                                 public void apply(PromiseError arg) throws OperationException {
                                     dialogFactory.createMessageDialog("Error", arg.getMessage(), null).show();
                                 }
                             });
    }

    @Override
    public void onCancelClicked() {
        commandProcessingCallback = getCommandProcessingCallback();
        refreshView();
    }

    @Override
    public void onAddClicked() {
        final String selectedType = view.getSelectedCommandType();

        if (selectedType != null) {
            createNewCommand(selectedType, null, null, null);
        }
    }

    @Override
    public void onDuplicateClicked() {
        final CommandImpl selectedCommand = view.getSelectedCommand();

        if (selectedCommand != null) {
            createNewCommand(selectedCommand.getType(),
                             selectedCommand.getCommandLine(),
                             selectedCommand.getName(),
                             selectedCommand.getAttributes());
        }
    }

    private void createNewCommand(final String type,
                                  final String commandLine,
                                  final String name,
                                  final Map<String, String> attributes) {
        if (!isViewModified()) {
            createCommand(type, commandLine, name, attributes);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateCommand(editedCommand).then(new Operation<CommandImpl>() {
                    @Override
                    public void apply(CommandImpl arg) throws OperationException {
                        createCommand(type, commandLine, name, attributes);
                    }
                });
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                refreshView();
                createCommand(type, commandLine, name, attributes);
            }
        };

        ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                machineLocale.editCommandsSaveChangesTitle(),
                machineLocale.editCommandsSaveChangesConfirmation(editedCommand.getName()),
                coreLocale.save(),
                machineLocale.editCommandsSaveChangesDiscard(),
                saveCallback, discardCallback);
        dialog.show();
    }

    private void createCommand(String type, String commandLine, String name, Map<String, String> attributes) {
        commandManager.create(name, commandLine, type, attributes).then(new Operation<CommandImpl>() {
            @Override
            public void apply(CommandImpl command) throws OperationException {
                view.selectCommand(command);
                refreshView();
            }
        });
    }

    @Override
    public void onRemoveClicked() {
        final CommandImpl selectedCommand = view.getSelectedCommand();
        if (selectedCommand == null) {
            return;
        }

        final ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                commandManager.remove(selectedCommand.getName()).then(new Operation<Void>() {
                    @Override
                    public void apply(Void arg) throws OperationException {
                        view.selectNeighborCommand(selectedCommand);
                        commandProcessingCallback = getCommandProcessingCallback();
                        refreshView();
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        dialogFactory.createMessageDialog("Error", arg.getMessage(), null).show();
                    }
                });
            }
        };

        ConfirmDialog confirmDialog = dialogFactory.createConfirmDialog(
                machineLocale.editCommandsViewRemoveTitle(),
                machineLocale.editCommandsRemoveConfirmation(selectedCommand.getName()),
                confirmCallback, null);
        confirmDialog.show();
    }

    @Override
    public void onEnterClicked() {
        if (view.isCancelButtonInFocus()) {
            onCancelClicked();
            return;
        }

        if (view.isCloseButtonInFocus()) {
            onCloseClicked();
            return;
        }

        onSaveClicked();
    }

    @Override
    public void onCommandSelected(final CommandImpl command) {
        if (!isViewModified()) {
            handleCommandSelection(command);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateCommand(editedCommand).then(new Operation<CommandImpl>() {
                    @Override
                    public void apply(CommandImpl arg) throws OperationException {
                        refreshView();
                        handleCommandSelection(command);
                    }
                });
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                refreshView();
                handleCommandSelection(command);
            }
        };

        ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                machineLocale.editCommandsSaveChangesTitle(),
                machineLocale.editCommandsSaveChangesConfirmation(editedCommand.getName()),
                coreLocale.save(),
                machineLocale.editCommandsSaveChangesDiscard(),
                saveCallback,
                discardCallback);

        dialog.show();
    }

    private void handleCommandSelection(CommandImpl command) {
        editedCommand = command;
        editedCommandNameInitial = command.getName();
        editedCommandPreviewUrlInitial = getPreviewUrlOrNull(command);

        view.setCommandName(command.getName());
        view.setCommandPreviewUrl(getPreviewUrlOrNull(command));

        final List<CommandPage> pages = commandManager.getPages(command.getType());
        if (!pages.isEmpty()) {
            editedPage = pages.get(0); // for now, show only the 1'st page

            editedPage.setFieldStateActionDelegate(this);

            editedPage.setDirtyStateListener(new DirtyStateListener() {
                @Override
                public void onDirtyStateChanged() {
                    view.setCancelButtonState(isViewModified());
                    view.setSaveButtonState(isViewModified());
                }
            });

            editedPage.resetFrom(command);
            editedPage.go(view.getCommandPageContainer());
        }
    }

    private String getPreviewUrlOrNull(CommandImpl command) {
        if (command.getAttributes() != null && command.getAttributes().containsKey(PREVIEW_URL_ATTR)) {
            return command.getAttributes().get(PREVIEW_URL_ATTR);
        }
        return null;
    }

    @Override
    public void onNameChanged() {
        final CommandImpl selectedCommand = view.getSelectedCommand();
        if (selectedCommand == null || !selectedCommand.equals(editedCommand)) {
            return;
        }

        selectedCommand.setName(view.getCommandName());

        view.setCancelButtonState(isViewModified());
        view.setSaveButtonState(isViewModified());
    }

    @Override
    public void onPreviewUrlChanged() {
        final CommandImpl selectedCommand = view.getSelectedCommand();
        if (selectedCommand == null || !selectedCommand.equals(editedCommand)) {
            return;
        }

        selectedCommand.getAttributes().put(PREVIEW_URL_ATTR, view.getCommandPreviewUrl());

        view.setCancelButtonState(isViewModified());
        view.setSaveButtonState(isViewModified());
    }

    /** Show dialog. */
    public void show() {
        view.show();

        refreshView();
    }

    private void refreshView() {
        reset();

        List<CommandImpl> allCommands = commandManager.getCommands();
        Map<CommandType, List<CommandImpl>> typeToCommands = new HashMap<>();

        for (CommandType type : commandTypeRegistry.getCommandTypes()) {
            final List<CommandImpl> commandsOfType = new ArrayList<>();

            for (CommandImpl command : allCommands) {
                if (type.getId().equals(command.getType())) {
                    commandsOfType.add(command);
                }
            }

            Collections.sort(commandsOfType, commandsComparator);
            typeToCommands.put(type, commandsOfType);
        }

        view.setData(typeToCommands);
        view.setFilterState(!allCommands.isEmpty());

        if (commandProcessingCallback != null) {
            commandProcessingCallback.onCompleted();
            commandProcessingCallback = null;
        }
    }

    private void reset() {
        editedCommand = null;
        editedCommandNameInitial = null;
        editedCommandPreviewUrlInitial = null;
        editedPage = null;

        view.setCommandName("");
        view.clearCommandPageContainer();
        view.setCommandPreviewUrl("");

        view.setCancelButtonState(false);
        view.setSaveButtonState(false);
    }

    private boolean isViewModified() {
        if (editedCommand == null || editedPage == null) {
            return false;
        }

        return editedPage.isDirty()
               || !editedCommandNameInitial.equals(view.getCommandName())
               || !Strings.nullToEmpty(editedCommandPreviewUrlInitial).equals(Strings.nullToEmpty(view.getCommandPreviewUrl()));
    }

    private CommandProcessingCallback getCommandProcessingCallback() {
        return new CommandProcessingCallback() {
            @Override
            public void onCompleted() {
                view.setCloseButtonInFocus();
            }
        };
    }

    @Override
    public void updatePreviewURLState(boolean isVisible) {
        view.setPreviewUrlState(isVisible);
    }

    interface CommandProcessingCallback {
        /** Called when handling of command is completed successfully. */
        void onCompleted();
    }
}
