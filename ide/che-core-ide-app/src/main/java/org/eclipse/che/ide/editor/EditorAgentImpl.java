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
package org.eclipse.che.ide.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.editor.AsyncEditorProvider;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorPartPresenter.EditorPartCloseHandler;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.texteditor.HasReadOnlyProperty;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.event.WindowActionHandler;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;

/**
 * Default implementation of {@link EditorAgent}.
 *
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 * @see EditorAgent
 **/
@Singleton
public class EditorAgentImpl implements EditorAgent,
                                        EditorPartCloseHandler,
                                        FileEventHandler,
                                        ActivePartChangedHandler,
                                        WindowActionHandler,
                                        WsAgentStateHandler {

    private final EventBus                 eventBus;
    private final WorkspaceAgent           workspaceAgent;
    private final FileTypeRegistry         fileTypeRegistry;
    private final EditorRegistry           editorRegistry;
    private final CoreLocalizationConstant coreLocalizationConstant;

    private final List<EditorPartPresenter> openedEditors;
    private       List<EditorPartPresenter> dirtyEditors;
    private       EditorPartPresenter       activeEditor;

    @Inject
    public EditorAgentImpl(EventBus eventBus,
                           FileTypeRegistry fileTypeRegistry,
                           EditorRegistry editorRegistry,
                           WorkspaceAgent workspaceAgent,
                           CoreLocalizationConstant coreLocalizationConstant) {
        this.eventBus = eventBus;
        this.fileTypeRegistry = fileTypeRegistry;
        this.editorRegistry = editorRegistry;
        this.workspaceAgent = workspaceAgent;
        this.coreLocalizationConstant = coreLocalizationConstant;
        this.openedEditors = newArrayList();

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
        eventBus.addHandler(FileEvent.TYPE, this);
        eventBus.addHandler(WindowActionEvent.TYPE, this);
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @Override
    public void onClose(EditorPartPresenter editor) {
        closeEditorPart(editor);
    }

    @Override
    public void onFileOperation(FileEvent event) {
        switch (event.getOperationType()) {
            case OPEN:
                openEditor(event.getFile());
                break;
            case CLOSE:
                closeEditor(event.getFile());
        }
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        if (event.getActivePart() instanceof EditorPartPresenter) {
            activeEditor = (EditorPartPresenter)event.getActivePart();
            activeEditor.activate();
        }
    }

    @Override
    public void onWindowClosing(WindowActionEvent event) {
        for (EditorPartPresenter editorPartPresenter : getOpenedEditors()) {
            if (editorPartPresenter.isDirty()) {
                event.setMessage(coreLocalizationConstant.changesMayBeLost()); //TODO need to move this into standalone component
                return;
            }
        }
    }

    @Override
    public void onWindowClosed(WindowActionEvent event) {
        //do nothing
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        //do nothing
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        for (EditorPartPresenter editor : getOpenedEditors()) {
            closeEditorPart(editor);
        }
    }

    @Override
    public void openEditor(@NotNull final VirtualFile file) {
        doOpen(file, new OpenEditorCallbackImpl());
    }

    @Override
    public void closeEditor(VirtualFile file) {
        checkArgument(file != null, "Null file occurred");

        closeEditorPart(getOpenedEditor(file.getLocation()));
    }

    @Override
    public void openEditor(@NotNull VirtualFile file, @NotNull OpenEditorCallback callback) {
        doOpen(file, callback);
    }

    private void doOpen(final VirtualFile file, final OpenEditorCallback callback) {
        EditorPartPresenter openedEditor = getOpenedEditor(file.getLocation());
        if (openedEditor != null) {
            workspaceAgent.setActivePart(openedEditor, EDITING);
            callback.onEditorActivated(openedEditor);
        } else {
            final FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
            EditorProvider editorProvider = editorRegistry.getEditor(fileType);
            if (editorProvider instanceof AsyncEditorProvider) {
                AsyncEditorProvider provider = (AsyncEditorProvider)editorProvider;
                Promise<EditorPartPresenter> promise = provider.createEditor(file);
                if (promise != null) {
                    promise.then(new Operation<EditorPartPresenter>() {
                        @Override
                        public void apply(EditorPartPresenter arg) throws OperationException {
                            initEditor(file, callback, fileType, arg);
                        }
                    });
                    return;
                }
            }
            final EditorPartPresenter editor = editorProvider.getEditor();

            initEditor(file, callback, fileType, editor);
        }
    }

    private void initEditor(final VirtualFile file, final OpenEditorCallback callback, FileType fileType,
                            final EditorPartPresenter editor) {
        editor.init(new EditorInputImpl(fileType, file), callback);
        editor.addCloseHandler(this);

        workspaceAgent.openPart(editor, EDITING);
        openedEditors.add(editor);

        workspaceAgent.setActivePart(editor);
        editor.addPropertyListener(new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                if (propId == EditorPartPresenter.PROP_INPUT) {
                    if (editor instanceof HasReadOnlyProperty) {
                        ((HasReadOnlyProperty)editor).setReadOnly(file.isReadOnly());
                    }

                    callback.onEditorOpened(editor);
                    eventBus.fireEvent(new EditorOpenedEvent(file, editor));
                }
            }
        });
    }

    @Override
    public void activateEditor(@NotNull EditorPartPresenter editor) {
        workspaceAgent.setActivePart(editor);
    }

    @Override
    public List<EditorPartPresenter> getDirtyEditors() {
        List<EditorPartPresenter> dirtyEditors = new ArrayList<>();
        for (EditorPartPresenter partPresenter : openedEditors) {
            if (partPresenter.isDirty()) {
                dirtyEditors.add(partPresenter);
            }
        }
        return dirtyEditors;
    }

    protected void closeEditorPart(EditorPartPresenter editor) {
        if (editor == null) {
            return;
        }

        openedEditors.remove(editor);

        editor.close(false);

        if (activeEditor == null) {
            return;
        }

        final VirtualFile closedFile = editor.getEditorInput().getFile();
        final VirtualFile activeFile = activeEditor.getEditorInput().getFile();
        if (activeFile.equals(closedFile)) {
            activeEditor = null;
        }
    }

    @NotNull
    @Override
    public List<EditorPartPresenter> getOpenedEditors() {
        return newArrayList(openedEditors);
    }

    @Override
    public EditorPartPresenter getOpenedEditor(Path path) {
        for (EditorPartPresenter editor : openedEditors) {
            if (path.equals(editor.getEditorInput().getFile().getLocation())) {
                return editor;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void saveAll(final AsyncCallback callback) {
        dirtyEditors = getDirtyEditors();
        if (dirtyEditors.isEmpty()) {
            callback.onSuccess("Success");
        } else {
            doSave(callback);
        }
    }

    private void doSave(final AsyncCallback callback) {
        final EditorPartPresenter partPresenter = dirtyEditors.get(0);
        partPresenter.doSave(new AsyncCallback<EditorInput>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(EditorInput result) {
                dirtyEditors.remove(partPresenter);
                if (dirtyEditors.isEmpty()) {
                    callback.onSuccess("Success");
                } else {
                    doSave(callback);
                }
            }
        });
    }

    @Override
    public EditorPartPresenter getActiveEditor() {
        return activeEditor;
    }
}
