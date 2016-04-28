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

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorPartPresenter.EditorPartCloseHandler;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEvent.FileOperation;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.event.WindowActionHandler;
import org.eclipse.che.ide.api.event.project.DeleteProjectEvent;
import org.eclipse.che.ide.api.event.project.DeleteProjectHandler;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.texteditor.HasReadOnlyProperty;
import org.eclipse.che.ide.project.event.ResourceNodeDeletedEvent;
import org.eclipse.che.ide.project.event.ResourceNodeRenamedEvent;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ItemReferenceBasedNode;
import org.eclipse.che.ide.project.node.ModuleNode;
import org.eclipse.che.ide.project.node.NodeManager;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;

/** @author Evgen Vidolob */
@Singleton
public class EditorAgentImpl implements EditorAgent {

    private final List<EditorPartPresenter> openedEditors;
    /** Used to notify {@link EditorAgentImpl} that editor has closed */
    private final EditorPartCloseHandler editorClosed     = new EditorPartCloseHandler() {
        @Override
        public void onClose(EditorPartPresenter editor) {
            editorClosed(editor);
        }
    };
    private final FileEventHandler       fileEventHandler = new FileEventHandler() {
        @Override
        public void onFileOperation(final FileEvent event) {
            if (event.getOperationType() == FileOperation.OPEN) {
                openEditor(event.getFile());
            } else if (event.getOperationType() == CLOSE) {
                closeEditor(event.getFile());
            }
        }
    };
    private final EventBus                  eventBus;
    private final WorkspaceAgent            workspace;
    private       List<EditorPartPresenter> dirtyEditors;
    private       FileTypeRegistry          fileTypeRegistry;
    private       EditorRegistry            editorRegistry;
    private       EditorPartPresenter       activeEditor;
    private final ActivePartChangedHandler activePartChangedHandler = new ActivePartChangedHandler() {
        @Override
        public void onActivePartChanged(ActivePartChangedEvent event) {
            if (event.getActivePart() instanceof EditorPartPresenter) {
                activeEditor = (EditorPartPresenter)event.getActivePart();
                activeEditor.activate();
            }
        }
    };
    private       NotificationManager      notificationManager;
    private       CoreLocalizationConstant coreLocalizationConstant;
    private final NodeManager              nodeManager;
    private final ProjectServiceClient     projectService;
    private final AppContext               appContext;
    private final DtoUnmarshallerFactory   unmarshallerFactory;
    private final WindowActionHandler windowActionHandler = new WindowActionHandler() {
        @Override
        public void onWindowClosing(final WindowActionEvent event) {
            for (EditorPartPresenter editorPartPresenter : openedEditors) {
                if (editorPartPresenter.isDirty()) {
                    event.setMessage(coreLocalizationConstant.changesMayBeLost());
                }
            }
        }

        @Override
        public void onWindowClosed(WindowActionEvent event) {
        }
    };

    @Inject
    public EditorAgentImpl(EventBus eventBus,
                           FileTypeRegistry fileTypeRegistry,
                           EditorRegistry editorRegistry,
                           final WorkspaceAgent workspace,
                           final NotificationManager notificationManager,
                           CoreLocalizationConstant coreLocalizationConstant,
                           NodeManager nodeManager,
                           ProjectServiceClient projectServiceClient,
                           AppContext appContext,
                           DtoUnmarshallerFactory unmarshallerFactory) {
        super();
        this.eventBus = eventBus;
        this.fileTypeRegistry = fileTypeRegistry;
        this.editorRegistry = editorRegistry;
        this.workspace = workspace;
        this.notificationManager = notificationManager;
        this.coreLocalizationConstant = coreLocalizationConstant;
        this.nodeManager = nodeManager;
        this.appContext = appContext;
        this.projectService = projectServiceClient;
        this.unmarshallerFactory = unmarshallerFactory;
        openedEditors = new ArrayList<>();

        bind();
    }

    protected void bind() {
        eventBus.addHandler(ActivePartChangedEvent.TYPE, activePartChangedHandler);
        eventBus.addHandler(FileEvent.TYPE, fileEventHandler);
        eventBus.addHandler(WindowActionEvent.TYPE, windowActionHandler);
        eventBus.addHandler(ResourceNodeDeletedEvent.getType(), new ResourceNodeDeletedEvent.ResourceNodeDeletedHandler() {
            @Override
            public void onResourceEvent(ResourceNodeDeletedEvent event) {
                ResourceBasedNode node = event.getNode();
                if (node instanceof FileReferenceNode) {
                    for (EditorPartPresenter editor : openedEditors) {
                        VirtualFile deletedVFile = (VirtualFile)node;
                        if (deletedVFile.getPath().equals(editor.getEditorInput().getFile().getPath())) {
                            eventBus.fireEvent(new FileEvent(editor.getEditorInput().getFile(), CLOSE));
                        }
                    }
                } else if (node instanceof FolderReferenceNode) {
                    for (EditorPartPresenter editor : openedEditors) {
                        if (editor.getEditorInput().getFile().getPath().startsWith(((FolderReferenceNode)node).getStorablePath())) {
                            eventBus.fireEvent(new FileEvent(editor.getEditorInput().getFile(), CLOSE));
                        }
                    }
                } else if (node instanceof ModuleNode) {
                    for (EditorPartPresenter editor : openedEditors) {
                        VirtualFile virtualFile = editor.getEditorInput().getFile();
                        if (moduleHasFile(node.getProjectConfig(), virtualFile)) {
                            eventBus.fireEvent(new FileEvent(virtualFile, CLOSE));
                        }
                        if (node.getParent() == null || !(node.getParent() instanceof HasStorablePath)) {
                            return;
                        }

                        String parentPath = ((HasStorablePath)node.getParent()).getStorablePath();
                        String openFileName = virtualFile.getName();
                        String openFilePath = virtualFile.getPath();
                        if (openFilePath.contains(parentPath) && openFileName.equals("modules")) {
                            eventBus.fireEvent(new FileContentUpdateEvent(openFilePath));
                        }
                    }
                }
            }
        });
        eventBus.addHandler(DeleteProjectEvent.TYPE, new DeleteProjectHandler() {
            @Override
            public void onProjectDeleted(DeleteProjectEvent event) {
                ProjectConfigDto configDto = event.getProjectConfig();
                for (EditorPartPresenter editor : openedEditors) {
                    VirtualFile virtualFile = editor.getEditorInput().getFile();
                    if (moduleHasFile(configDto, virtualFile)) {
                        eventBus.fireEvent(new FileEvent(virtualFile, CLOSE));
                    }
                }
            }
        });
        eventBus.addHandler(ResourceNodeRenamedEvent.getType(), new ResourceNodeRenamedEvent.ResourceNodeRenamedHandler() {
            @Override
            public void onResourceRenamedEvent(ResourceNodeRenamedEvent event) {
                ResourceBasedNode<?> resourceBaseNode = event.getNode();

                if (resourceBaseNode instanceof FolderReferenceNode || resourceBaseNode instanceof ModuleNode) {
                    HasStorablePath renamedTargetStoragePath = ((HasStorablePath)resourceBaseNode);
                    final String oldTargetPath = renamedTargetStoragePath.getStorablePath();
                    final String newTargetPath;
                    if (resourceBaseNode instanceof FolderReferenceNode) {
                        newTargetPath = ((ItemReference)event.getNewDataObject()).getPath();
                    } else {
                        newTargetPath = ((ProjectConfigDto)event.getNewDataObject()).getPath();
                    }
                    final Unmarshallable<ItemReference> unmarshaller = unmarshallerFactory.newUnmarshaller(ItemReference.class);
                    updateEditorPartsAfterRename(new LinkedList<>(openedEditors),
                                                 oldTargetPath,
                                                 newTargetPath,
                                                 unmarshaller);
                }
            }
        });

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                //to do nothing
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                List<VirtualFile> filesToClose = new ArrayList<>();
                for (EditorPartPresenter editor : openedEditors) {
                    filesToClose.add(editor.getEditorInput().getFile());
                }

                for (VirtualFile virtualFile : filesToClose) {
                    eventBus.fireEvent(new FileEvent(virtualFile, FileEvent.FileOperation.CLOSE));
                }
            }
        });
    }

    //todo Warning: this code should be reworked or deleted when folders and maven modules won't being closed after rename

    /**
     * Recursive update opened editor parts after renaming their parent folder or parent module
     *
     * @param editorParts
     *         list opened editor parts
     */
    private void updateEditorPartsAfterRename(final LinkedList<EditorPartPresenter> editorParts,
                                              final String oldTargetPath,
                                              final String newTargetPath,
                                              final Unmarshallable<ItemReference> unmarshaller) {
        if (editorParts.isEmpty()) {
            return;
        }
        final EditorPartPresenter editorPart = editorParts.pop();
        final String oldFilePath = editorPart.getEditorInput().getFile().getPath();

        if (!oldFilePath.startsWith(oldTargetPath + "/")) {
            updateEditorPartsAfterRename(editorParts, oldTargetPath, newTargetPath, unmarshaller);
        }

        final String newFilePath = oldFilePath.replaceFirst(oldTargetPath, newTargetPath);

        projectService.getItem(appContext.getDevMachine(), newFilePath, new AsyncRequestCallback<ItemReference>(unmarshaller) {
            @Override
            protected void onSuccess(ItemReference result) {
                FileReferenceNode fileReferenceNode = ((FileReferenceNode)editorPart.getEditorInput().getFile());
                ProjectConfigDto configDto = fileReferenceNode.getProjectConfig();
                final ItemReferenceBasedNode wrappedNode = nodeManager.wrap(result, configDto);

                if (wrappedNode instanceof FileReferenceNode) {
                    updateEditorNode(oldFilePath, (FileReferenceNode)wrappedNode);
                }
                updateEditorPartsAfterRename(editorParts, oldTargetPath, newTargetPath, unmarshaller);
            }

            @Override
            protected void onFailure(Throwable exception) {
                updateEditorPartsAfterRename(editorParts, oldTargetPath, newTargetPath, unmarshaller);
            }
        });
    }

    private boolean moduleHasFile(ProjectConfigDto configDto, VirtualFile virtualFile) {
        String descriptorPath = configDto.getPath();
        String descriptorPathOfFile = virtualFile.getProject().getProjectConfig().getPath();
        return descriptorPathOfFile.equals(descriptorPath) || descriptorPathOfFile.startsWith(descriptorPath + "/");
    }

    /** {@inheritDoc} */
    @Override
    public void openEditor(@NotNull final VirtualFile file) {
        doOpen(file, null);
    }

    /** {@inheritDoc} */
    public void closeEditor(VirtualFile file) {
        EditorPartPresenter openedEditor = getOpenedEditor(Path.valueOf(file.getPath()));
        if (openedEditor != null) {
            editorClosed(openedEditor);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void openEditor(@NotNull VirtualFile file, @NotNull OpenEditorCallback callback) {
        doOpen(file, callback);
    }

    private void doOpen(final VirtualFile file, final OpenEditorCallback callback) {
        EditorPartPresenter openedEditor = getOpenedEditor(Path.valueOf(file.getPath()));
        if (openedEditor != null) {
            workspace.setActivePart(openedEditor, EDITING);
            callback.onEditorActivated(openedEditor);
        } else {
            FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
            EditorProvider editorProvider = editorRegistry.getEditor(fileType);
            final EditorPartPresenter editor = editorProvider.getEditor();

            editor.init(new EditorInputImpl(fileType, file), callback);
            editor.addCloseHandler(editorClosed);

            workspace.openPart(editor, EDITING);
            openedEditors.add(editor);

            workspace.setActivePart(editor);
            editor.addPropertyListener(new PropertyListener() {
                @Override
                public void propertyChanged(PartPresenter source, int propId) {
                    if (propId == EditorPartPresenter.PROP_INPUT) {
                        if (editor instanceof HasReadOnlyProperty) {
                            ((HasReadOnlyProperty)editor).setReadOnly(file.isReadOnly());
                        }
                        if (callback != null) {
                            callback.onEditorOpened(editor);
                        }

                        eventBus.fireEvent(new EditorOpenedEvent(file, editor));
                    }
                }
            });

        }
    }

    /** {@inheritDoc} */
    @Override
    public void activateEditor(@NotNull EditorPartPresenter editor) {
        workspace.setActivePart(editor);
    }

    /** {@inheritDoc} */
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

    /** @param editor */
    protected void editorClosed(EditorPartPresenter editor) {
        openedEditors.remove(editor);

        //call close() method
        editor.close(false);

        if (activeEditor == null) {
            return;
        }

        String closedFilePath = editor.getEditorInput().getFile().getPath();
        String activeFilePath = activeEditor.getEditorInput().getFile().getPath();
        if (activeFilePath.equals(closedFilePath)) {
            activeEditor = null;
        }
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public List<EditorPartPresenter> getOpenedEditors() {
        return openedEditors;
    }

    /** {@inheritDoc} */
    @Override
    public EditorPartPresenter getOpenedEditor(Path path) {
        for (EditorPartPresenter editor : openedEditors) {
            if (path.equals(Path.valueOf(editor.getEditorInput().getFile().getPath()))) {
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
                notificationManager.notify(coreLocalizationConstant.someFilesCanNotBeSaved(), StatusNotification.Status.FAIL, FLOAT_MODE);
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

    /** {@inheritDoc} */
    @Override
    public void updateEditorNode(@NotNull String path, @NotNull VirtualFile virtualFile) {
        EditorPartPresenter openedEditor = getOpenedEditor(Path.valueOf(path));
        if (openedEditor == null) {
            return;
        }
        openedEditor.getEditorInput().setFile(virtualFile);
        openedEditor.onFileChanged();
    }

    /** {@inheritDoc} */
    @Override
    public EditorPartPresenter getActiveEditor() {
        return activeEditor;
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public EditorPartPresenter getNextEditor() {
        EditorPartPresenter nextPart = null;
        Iterator<EditorPartPresenter> iterator = openedEditors.iterator();
        while (iterator.hasNext()) {
            EditorPartPresenter editor = iterator.next();
            if (activeEditor.equals(editor) && iterator.hasNext()) {
                nextPart = iterator.next();
                break;
            }
        }
        return nextPart;
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public EditorPartPresenter getPreviousEditor() {
        EditorPartPresenter previousEditor = null;
        for (EditorPartPresenter editor : openedEditors) {
            if (activeEditor.equals(editor) && previousEditor != null) {
                break;
            }
            previousEditor = editor;
        }
        return previousEditor;
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public EditorPartPresenter getLastEditor() {
        return openedEditors.isEmpty() ? null : openedEditors.get(openedEditors.size() - 1);
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public EditorPartPresenter getFirstEditor() {
        return openedEditors.isEmpty() ? null : openedEditors.get(0);
    }
}
