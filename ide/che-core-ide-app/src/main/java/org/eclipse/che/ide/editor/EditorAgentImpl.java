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

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.util.ArrayOf;

import com.google.common.base.Optional;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.LinkWithEditorAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.constraints.Direction;
import org.eclipse.che.ide.api.data.HasDataObject;
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
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEvent.FileEventHandler;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.event.WindowActionHandler;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorMultiPartStackState;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.editor.synchronization.EditorContentSynchronizer;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.parseBoolean;
import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;

/**
 * Default implementation of {@link EditorAgent}.
 *
 * @see EditorAgent
 **/
@Singleton
public class EditorAgentImpl implements EditorAgent,
                                        EditorPartCloseHandler,
                                        FileEventHandler,
                                        ActivePartChangedHandler,
                                        SelectionChangedHandler,
                                        WindowActionHandler,
                                        WsAgentStateHandler,
                                        StateComponent {

    private final EventBus                 eventBus;
    private final WorkspaceAgent           workspaceAgent;
    private final FileTypeRegistry         fileTypeRegistry;
    private final PreferencesManager       preferencesManager;
    private final EditorRegistry           editorRegistry;
    private final CoreLocalizationConstant coreLocalizationConstant;
    private final EditorMultiPartStack     editorMultiPartStack;

    private final List<EditorPartPresenter>           openedEditors;
    private final Map<EditorPartPresenter, String>    openedEditorsToProviders;
    private final Provider<EditorContentSynchronizer> editorContentSynchronizerProvider;
    private final AppContext                          appContext;
    private final PromiseProvider                     promiseProvider;
    private       List<EditorPartPresenter>           dirtyEditors;
    private       EditorPartPresenter                 activeEditor;
    private       PartPresenter                       activePart;

    @Inject
    public EditorAgentImpl(EventBus eventBus,
                           FileTypeRegistry fileTypeRegistry,
                           PreferencesManager preferencesManager,
                           EditorRegistry editorRegistry,
                           WorkspaceAgent workspaceAgent,
                           CoreLocalizationConstant coreLocalizationConstant,
                           EditorMultiPartStackPresenter editorMultiPartStack,
                           Provider<EditorContentSynchronizer> editorContentSynchronizerProvider,
                           AppContext appContext,
                           PromiseProvider promiseProvider) {
        this.eventBus = eventBus;
        this.fileTypeRegistry = fileTypeRegistry;
        this.preferencesManager = preferencesManager;
        this.editorRegistry = editorRegistry;
        this.workspaceAgent = workspaceAgent;
        this.coreLocalizationConstant = coreLocalizationConstant;
        this.editorMultiPartStack = editorMultiPartStack;
        this.editorContentSynchronizerProvider = editorContentSynchronizerProvider;
        this.appContext = appContext;
        this.promiseProvider = promiseProvider;
        this.openedEditors = newArrayList();
        this.openedEditorsToProviders = new HashMap<>();

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
        eventBus.addHandler(SelectionChangedEvent.TYPE, this);
        eventBus.addHandler(FileEvent.TYPE, this);
        eventBus.addHandler(WindowActionEvent.TYPE, this);
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @Override
    public void onClose(EditorPartPresenter editor) {
        closeEditor(editor);
    }

    @Override
    public void onFileOperation(FileEvent event) {
        switch (event.getOperationType()) {
            case OPEN:
                openEditor(event.getFile());
                break;
            case CLOSE:
                closeEditor(event.getEditorTab());
        }
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        activePart = event.getActivePart();
        if (!(event.getActivePart() instanceof EditorPartPresenter)) {
            return;
        }
        activeEditor = (EditorPartPresenter)event.getActivePart();
        activeEditor.activate();
        final String isLinkedWithEditor = preferencesManager.getValue(LinkWithEditorAction.LINK_WITH_EDITOR);
        if (parseBoolean(isLinkedWithEditor)) {
            final VirtualFile file = activeEditor.getEditorInput().getFile();
            eventBus.fireEvent(new RevealResourceEvent(file.getLocation()));
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
            closeEditor(editor);
        }
    }

    @Override
    public void openEditor(@NotNull final VirtualFile file) {
        doOpen(file, new OpenEditorCallbackImpl(), null);
    }

    @Override
    public void openEditor(@NotNull VirtualFile file, Constraints constraints) {
        doOpen(file, new OpenEditorCallbackImpl(), constraints);
    }

    private void closeEditor(EditorTab tab) {
        checkArgument(tab != null, "Null editor tab occurred");

        EditorPartPresenter editor = tab.getRelativeEditorPart();
        if (editor == null) {
            return;
        }

        openedEditors.remove(editor);
        openedEditorsToProviders.remove(editor);

        editor.close(false);

        if (editor instanceof TextEditor) {
            editorContentSynchronizerProvider.get().unTrackEditor(editor);
        }

        if (activeEditor != null && activeEditor == editor) {
            activeEditor = null;
        }
    }

    @Override
    public void closeEditor(EditorPartPresenter editor) {
        if (editor == null) {
            return;
        }

        EditorPartStack editorPartStack = editorMultiPartStack.getPartStackByPart(editor);
        if (editorPartStack == null) {
            return;
        }

        EditorTab editorTab = editorPartStack.getTabByPart(editor);
        //we have the handlers for the closing file event in different places of the project
        //so we need to notify them about it (we can't just pass doClose() method)
        eventBus.fireEvent(FileEvent.createCloseFileEvent(editorTab));
    }

    @Override
    public void openEditor(@NotNull VirtualFile file, @NotNull OpenEditorCallback callback) {
        doOpen(file, callback, null);
    }

    private void doOpen(final VirtualFile file, final OpenEditorCallback callback, final Constraints constraints) {
        EditorPartStack activePartStack = editorMultiPartStack.getActivePartStack();
        if (constraints == null && activePartStack != null) {
            PartPresenter partPresenter = activePartStack.getPartByPath(file.getLocation());
            if (partPresenter != null) {
                workspaceAgent.setActivePart(partPresenter, EDITING);
                callback.onEditorActivated((EditorPartPresenter)partPresenter);
                return;
            }
        }

        final FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
        final EditorProvider editorProvider = editorRegistry.getEditor(fileType);
        if (editorProvider instanceof AsyncEditorProvider) {
            AsyncEditorProvider provider = (AsyncEditorProvider)editorProvider;
            Promise<EditorPartPresenter> promise = provider.createEditor(file);
            if (promise != null) {
                promise.then(new Operation<EditorPartPresenter>() {
                    @Override
                    public void apply(EditorPartPresenter arg) throws OperationException {
                        initEditor(file, callback, fileType, arg, constraints, editorProvider);
                    }
                });
                return;
            }
        }

        final EditorPartPresenter editor = editorProvider.getEditor();
        initEditor(file, callback, fileType, editor, constraints, editorProvider);
    }

    private void initEditor(final VirtualFile file, final OpenEditorCallback callback, FileType fileType,
                            final EditorPartPresenter editor, final Constraints constraints, EditorProvider editorProvider) {
        editor.init(new EditorInputImpl(fileType, file), callback);
        editor.addCloseHandler(this);

        workspaceAgent.openPart(editor, EDITING, constraints);
        finalizeInit(file, callback, editor, editorProvider);
    }

    private void finalizeInit(final VirtualFile file, final OpenEditorCallback callback, final EditorPartPresenter editor,
                              EditorProvider editorProvider) {
        openedEditors.add(editor);
        openedEditorsToProviders.put(editor, editorProvider.getId());

        workspaceAgent.setActivePart(editor);
        editor.addPropertyListener(new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                if (propId == EditorPartPresenter.PROP_INPUT) {
                    if (editor instanceof HasReadOnlyProperty) {
                        ((HasReadOnlyProperty)editor).setReadOnly(file.isReadOnly());
                    }

                    if (editor instanceof TextEditor) {
                        editorContentSynchronizerProvider.get().trackEditor(editor);
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

    @NotNull
    @Override
    public List<EditorPartPresenter> getOpenedEditors() {
        return newArrayList(openedEditors);
    }

    @Override
    public List<EditorPartPresenter> getOpenedEditorsFor(EditorPartStack editorPartStack) {
        List<EditorPartPresenter> result = newArrayList();
        for (EditorPartPresenter editor : openedEditors) {
            if (editorPartStack.containsPart(editor)) {
                result.add(editor);
            }
        }
        return result;
    }

    @Nullable
    @Override
    public EditorPartPresenter getOpenedEditor(Path path) {
        EditorPartStack editorPartStack = editorMultiPartStack.getPartStackByPart(activeEditor);
        return editorPartStack == null ? null : (EditorPartPresenter)editorPartStack.getPartByPath(path);
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

    @Override
    public EditorPartPresenter getNextFor(EditorPartPresenter editorPart) {
        return editorMultiPartStack.getNextFor(editorPart);
    }

    @Override
    public EditorPartPresenter getPreviousFor(EditorPartPresenter editorPart) {
        return editorMultiPartStack.getPreviousFor(editorPart);
    }

    @Override
    public JsonObject getState() {
        JsonObject state = Json.createObject();

        EditorMultiPartStackState stacks = null;
        try {
            stacks = editorMultiPartStack.getState();
        } catch (IllegalStateException ignore) {
        }
        if (stacks != null) {
            state.put("FILES", storeEditors(stacks));
        }
        EditorPartPresenter activeEditor = getActiveEditor();
        if (activeEditor != null) {
            state.put("ACTIVE_EDITOR", activeEditor.getEditorInput().getFile().getLocation().toString());
        }
        return state;
    }

    private JsonObject storeEditors(EditorMultiPartStackState splitStacks) {
        JsonObject result = Json.createObject();
        if (splitStacks.getEditorPartStack() != null) {
            result.put("FILES", storeEditors(splitStacks.getEditorPartStack()));
        } else {
            result.put("DIRECTION", splitStacks.getDirection().toString());
            result.put("SPLIT_FIRST", storeEditors(splitStacks.getSplitFirst()));
            result.put("SPLIT_SECOND", storeEditors(splitStacks.getSplitSecond()));
            result.put("SIZE", splitStacks.getSize());
        }
        return result;
    }

    private JsonArray storeEditors(EditorPartStack partStack) {
        JsonArray result = Json.createArray();
        int i = 0;
        List<EditorPartPresenter> parts = partStack.getParts();
        for (EditorPartPresenter part : parts) {
            JsonObject file = Json.createObject();
            file.put("PATH", part.getEditorInput().getFile().getLocation().toString());
            file.put("EDITOR_PROVIDER", openedEditorsToProviders.get(part));
            if (part instanceof TextEditor) {
                file.put("CURSOR_OFFSET", ((TextEditor)part).getCursorOffset());
                file.put("TOP_VISIBLE_LINE", ((TextEditor)part).getTopVisibleLine());
            }
            if (partStack.getActivePart().equals(part)) {
                file.put("ACTIVE", true);
            }
            result.set(i++, file);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadState(@NotNull final JsonObject state) {
        if (state.hasKey("FILES")) {
            JsonObject files = state.getObject("FILES");
            EditorPartStack partStack = editorMultiPartStack.createRootPartStack();
            final Map<EditorPartPresenter, EditorPartStack> activeEditors = new HashMap<>();
            List<Promise<Void>> restore = restore(files, partStack, activeEditors);
            Promise<ArrayOf<?>> promise = promiseProvider.all2(restore.toArray(new Promise[restore.size()]));
            promise.then(new Operation() {
                @Override
                public void apply(Object arg) throws OperationException {
                    String activeFile = "";
                    if (state.hasKey("ACTIVE_EDITOR")) {
                        activeFile = state.getString("ACTIVE_EDITOR");
                    }
                    EditorPartPresenter activeEditorPart = null;
                    for (Map.Entry<EditorPartPresenter, EditorPartStack> entry : activeEditors.entrySet()) {
                        entry.getValue().setActivePart(entry.getKey());
                        if (activeFile.equals(entry.getKey().getEditorInput().getFile().getLocation().toString())) {
                            activeEditorPart = entry.getKey();
                        }
                    }
                    workspaceAgent.setActivePart(activeEditorPart);
                }
            });
        }
    }

    private List<Promise<Void>> restore(JsonObject files, EditorPartStack editorPartStack,
                                        Map<EditorPartPresenter, EditorPartStack> activeEditors) {

        if (files.hasKey("FILES")) {
            //plain
            JsonArray filesArray = files.getArray("FILES");
            List<Promise<Void>> promises = new ArrayList<>();
            for (int i = 0; i < filesArray.length(); i++) {
                JsonObject file = filesArray.getObject(i);
                Promise<Void> openFile = openFile(file, editorPartStack, activeEditors);
                promises.add(openFile);
            }
            return promises;
        } else {
            //split
            return restoreSplit(files, editorPartStack, activeEditors);

        }

    }

    private List<Promise<Void>> restoreSplit(JsonObject files, EditorPartStack editorPartStack,
                                             Map<EditorPartPresenter, EditorPartStack> activeEditors) {
        JsonObject splitFirst = files.getObject("SPLIT_FIRST");
        String direction = files.getString("DIRECTION");
        double size = files.getNumber("SIZE");
        EditorPartStack split = editorMultiPartStack.split(editorPartStack, new Constraints(Direction.valueOf(direction), null), size);
        List<Promise<Void>> restoreFirst = restore(splitFirst, editorPartStack, activeEditors);
        JsonObject splitSecond = files.getObject("SPLIT_SECOND");
        List<Promise<Void>> restoreSecond = restore(splitSecond, split, activeEditors);
        List<Promise<Void>> result = new ArrayList<>();
        result.addAll(restoreFirst);
        result.addAll(restoreSecond);
        return result;
    }

    private Promise<Void> openFile(final JsonObject file, final EditorPartStack editorPartStack,
                                   final Map<EditorPartPresenter, EditorPartStack> activeEditors) {
        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                String path = file.getString("PATH");
                appContext.getWorkspaceRoot().getFile(path).then(new Operation<Optional<File>>() {
                    @Override
                    public void apply(final Optional<File> optionalFile) throws OperationException {
                        if (optionalFile.isPresent()) {
                            restoreCreateEditor(optionalFile.get(), file, editorPartStack, callback, activeEditors);
                        } else {
                            callback.onSuccess(null);
                        }
                    }
                });
            }
        });


    }

    private void restoreCreateEditor(final File resourceFile, JsonObject file, final EditorPartStack editorPartStack,
                                     final AsyncCallback<Void> openCallback,
                                     final Map<EditorPartPresenter, EditorPartStack> activeEditors) {
        String providerId = file.getString("EDITOR_PROVIDER");
        final OpenEditorCallback callback;
        if (file.hasKey("CURSOR_OFFSET") && file.hasKey("TOP_VISIBLE_LINE")) {
            final int cursorOffset = (int)file.getNumber("CURSOR_OFFSET");
            final int topLine = (int)file.getNumber("TOP_VISIBLE_LINE");
            callback = new RestoreStateEditorCallBack(cursorOffset, topLine);
        } else {
            callback = new OpenEditorCallbackImpl();
        }
        final boolean active = file.hasKey("ACTIVE") && file.getBoolean("ACTIVE");

        final EditorProvider provider = editorRegistry.findEditorProviderById(providerId);
        if (provider instanceof AsyncEditorProvider) {
            ((AsyncEditorProvider)provider).createEditor(resourceFile).then(new Operation<EditorPartPresenter>() {
                @Override
                public void apply(EditorPartPresenter arg) throws OperationException {
                    restoreInitEditor(resourceFile, callback, fileTypeRegistry.getFileTypeByFile(resourceFile), arg, provider,
                                      editorPartStack);
                    if (active) {
                        activeEditors.put(arg, editorPartStack);
                    }

                }
            });
        } else {
            EditorPartPresenter editor = provider.getEditor();
            restoreInitEditor(resourceFile, callback, fileTypeRegistry.getFileTypeByFile(resourceFile), editor, provider, editorPartStack);
            if (active) {
                activeEditors.put(editor, editorPartStack);
            }
        }
        openCallback.onSuccess(null);
    }

    private void restoreInitEditor(final VirtualFile file, final OpenEditorCallback callback, FileType fileType,
                                   final EditorPartPresenter editor, EditorProvider editorProvider, EditorPartStack editorPartStack) {
        editor.init(new EditorInputImpl(fileType, file), callback);
        editor.addCloseHandler(this);

        editorPartStack.addPart(editor);
        finalizeInit(file, callback, editor, editorProvider);
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent event) {
        final String isLinkedWithEditor = preferencesManager.getValue(LinkWithEditorAction.LINK_WITH_EDITOR);
        if (!parseBoolean(isLinkedWithEditor)) {
            return;
        }

        final Selection<?> selection = event.getSelection();
        if (selection instanceof Selection.NoSelectionProvided) {
            return;
        }

        Resource currentResource = null;

        if (selection == null || selection.getHeadElement() == null || selection.getAllElements().size() > 1) {
            return;
        }

        final Object headObject = selection.getHeadElement();

        if (headObject instanceof HasDataObject) {
            Object data = ((HasDataObject)headObject).getData();

            if (data instanceof Resource) {
                currentResource = (Resource)data;
            }
        } else if (headObject instanceof Resource) {
            currentResource = (Resource)headObject;
        }

        EditorPartStack activePartStack = editorMultiPartStack.getActivePartStack();
        if (currentResource == null || activePartStack == null || activeEditor == null) {
            return;
        }

        final Path locationOfActiveOpenedFile = activeEditor.getEditorInput().getFile().getLocation();
        final Path selectedResourceLocation = currentResource.getLocation();
        if (!(activePart instanceof ProjectExplorerPresenter) &&
            selectedResourceLocation.equals(locationOfActiveOpenedFile)) {
            return;
        }

        PartPresenter partPresenter = activePartStack.getPartByPath(selectedResourceLocation);
        if (partPresenter != null) {
            workspaceAgent.setActivePart(partPresenter, EDITING);
        }
    }

    private static class RestoreStateEditorCallBack extends OpenEditorCallbackImpl {
        private final int cursorOffset;
        private final int topLine;

        public RestoreStateEditorCallBack(int cursorOffset, int topLine) {
            this.cursorOffset = cursorOffset;
            this.topLine = topLine;
        }

        @Override
        public void onEditorOpened(EditorPartPresenter editor) {
            if (editor instanceof TextEditor) {
                TextEditor textEditor = (TextEditor)editor;
                textEditor.getCursorModel().setCursorPosition(cursorOffset);

            }
        }

        @Override
        public void onEditorActivated(EditorPartPresenter editor) {
            if (editor instanceof TextEditor) {
                ((TextEditor)editor).setTopLine(topLine);
            }
        }
    }
}
