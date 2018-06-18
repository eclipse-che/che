/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.parseBoolean;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;
import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.util.ArrayOf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.LinkWithEditorAction;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.constraints.Direction;
import org.eclipse.che.ide.api.editor.AsyncEditorProvider;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorPartPresenter.EditorPartCloseHandler;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.editor.texteditor.HasReadOnlyProperty;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorMultiPartStackState;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionChangedEvent;
import org.eclipse.che.ide.api.selection.SelectionChangedHandler;
import org.eclipse.che.ide.api.statepersistance.StateComponent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.editor.synchronization.EditorContentSynchronizer;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.ui.smartTree.data.HasDataObject;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Default implementation of {@link EditorAgent}.
 *
 * @see EditorAgent
 */
@Singleton
public class EditorAgentImpl
    implements EditorAgent,
        EditorPartCloseHandler,
        ActivePartChangedHandler,
        SelectionChangedHandler,
        StateComponent,
        WorkspaceStoppedEvent.Handler,
        WsAgentServerStoppedEvent.Handler {

  private final EventBus eventBus;
  private final WorkspaceAgent workspaceAgent;
  private final FileTypeRegistry fileTypeRegistry;
  private final PreferencesManager preferencesManager;
  private final EditorRegistry editorRegistry;
  private final CoreLocalizationConstant coreLocalizationConstant;
  private final EditorMultiPartStack editorMultiPartStack;

  private final List<EditorPartPresenter> openedEditors;
  private final Map<EditorPartStack, Set<Path>> openingEditorsPathsToStacks;
  private final Map<EditorPartPresenter, String> openedEditorsToProviders;
  private final EditorContentSynchronizer editorContentSynchronizer;
  private final PromiseProvider promiseProvider;
  private final ResourceProvider resourceProvider;
  private final NotificationManager notificationManager;
  private List<EditorPartPresenter> dirtyEditors;
  private EditorPartPresenter activeEditor;
  private PartPresenter activePart;

  @Inject
  public EditorAgentImpl(
      EventBus eventBus,
      FileTypeRegistry fileTypeRegistry,
      PreferencesManager preferencesManager,
      EditorRegistry editorRegistry,
      WorkspaceAgent workspaceAgent,
      CoreLocalizationConstant coreLocalizationConstant,
      EditorMultiPartStackPresenter editorMultiPartStack,
      EditorContentSynchronizer editorContentSynchronizer,
      PromiseProvider promiseProvider,
      ResourceProvider resourceProvider,
      NotificationManager notificationManager) {
    this.eventBus = eventBus;
    this.fileTypeRegistry = fileTypeRegistry;
    this.preferencesManager = preferencesManager;
    this.editorRegistry = editorRegistry;
    this.workspaceAgent = workspaceAgent;
    this.coreLocalizationConstant = coreLocalizationConstant;
    this.editorMultiPartStack = editorMultiPartStack;
    this.editorContentSynchronizer = editorContentSynchronizer;
    this.promiseProvider = promiseProvider;
    this.resourceProvider = resourceProvider;
    this.notificationManager = notificationManager;
    this.openedEditors = newArrayList();
    this.openingEditorsPathsToStacks = new HashMap<>();
    this.openedEditorsToProviders = new HashMap<>();

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    eventBus.addHandler(SelectionChangedEvent.TYPE, this);
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, this);
  }

  @Override
  public void onClose(EditorPartPresenter editor) {
    if (editor == null) {
      return;
    }

    final EditorPartStack editorPartStack = editorMultiPartStack.getPartStackByPart(editor);
    if (editorPartStack == null) {
      return;
    }

    EditorTab editorTab = editorPartStack.getTabByPart(editor);
    doCloseEditor(editorTab);
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    activePart = event.getActivePart();
    if (!(event.getActivePart() instanceof EditorPartPresenter)) {
      return;
    }
    activeEditor = (EditorPartPresenter) event.getActivePart();
    activeEditor.activate();
    final String isLinkedWithEditor =
        preferencesManager.getValue(LinkWithEditorAction.LINK_WITH_EDITOR);
    if (parseBoolean(isLinkedWithEditor)) {
      final VirtualFile file = activeEditor.getEditorInput().getFile();
      eventBus.fireEvent(new RevealResourceEvent(file.getLocation(), true, false));
    }
  }

  @Override
  public void openEditor(@NotNull final VirtualFile file) {
    openEditor(file, new OpenEditorCallbackImpl());
  }

  @Override
  public void openEditor(@NotNull VirtualFile file, Constraints constraints) {
    if (constraints == null) {
      openEditor(file);
      return;
    }

    EditorPartStack relativeEditorPartStack =
        editorMultiPartStack.getPartStackByTabId(constraints.relativeId);
    if (relativeEditorPartStack == null) {
      String errorMessage =
          coreLocalizationConstant.canNotOpenFileInSplitMode(file.getLocation().toString());
      notificationManager.notify(errorMessage, FAIL, EMERGE_MODE);
      Log.error(getClass(), errorMessage + ": relative part stack is not found");
      return;
    }

    EditorPartStack editorPartStackConsumer =
        editorMultiPartStack.split(relativeEditorPartStack, constraints, -1);
    if (editorPartStackConsumer == null) {
      String errorMessage =
          coreLocalizationConstant.canNotOpenFileInSplitMode(file.getLocation().toString());
      notificationManager.notify(errorMessage, FAIL, EMERGE_MODE);
      Log.error(getClass(), errorMessage + ": split part stack is not found");
      return;
    }

    doOpen(file, editorPartStackConsumer, new OpenEditorCallbackImpl());
  }

  @Override
  public void closeEditor(final EditorPartPresenter editor) {
    if (editor == null) {
      return;
    }

    final EditorPartStack editorPartStack = editorMultiPartStack.getPartStackByPart(editor);
    if (editorPartStack == null) {
      return;
    }

    editor.onClosing(
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            EditorTab editorTab = editorPartStack.getTabByPart(editor);
            doCloseEditor(editorTab);
          }

          @Override
          public void onFailure(Throwable caught) {}
        });
  }

  private void doCloseEditor(EditorTab tab) {
    checkArgument(tab != null, "Null editor tab occurred");

    EditorPartPresenter editor = tab.getRelativeEditorPart();
    if (editor == null) {
      return;
    }

    openedEditors.remove(editor);
    openedEditorsToProviders.remove(editor);

    editor.close(false);

    if (editor instanceof TextEditor) {
      editorContentSynchronizer.unTrackEditor(editor);
    }

    if (activeEditor != null && activeEditor == editor) {
      activeEditor = null;
    }

    eventBus.fireEvent(FileEvent.createFileClosedEvent(tab));
  }

  @Override
  public void openEditor(@NotNull VirtualFile file, @NotNull OpenEditorCallback callback) {
    Path path = file.getLocation();
    EditorPartStack activeEditorPartStack = editorMultiPartStack.getActivePartStack();
    if (activeEditorPartStack != null) {
      PartPresenter openedPart = activeEditorPartStack.getPartByPath(path);
      if (openedPart != null) {
        editorMultiPartStack.setActivePart(openedPart);
        callback.onEditorActivated((EditorPartPresenter) openedPart);
        return;
      }

      if (isFileOpening(path, activeEditorPartStack)) {
        return;
      }
    } else {
      activeEditorPartStack = editorMultiPartStack.createRootPartStack();
    }

    doOpen(file, activeEditorPartStack, callback);
  }

  private void doOpen(
      VirtualFile file, EditorPartStack editorPartStackConsumer, OpenEditorCallback callback) {

    addToOpeningFilesList(file.getLocation(), editorPartStackConsumer);

    final FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
    final EditorProvider editorProvider = editorRegistry.getEditor(fileType);
    if (editorProvider instanceof AsyncEditorProvider) {
      AsyncEditorProvider provider = (AsyncEditorProvider) editorProvider;
      Promise<EditorPartPresenter> promise = provider.createEditor(file);
      if (promise != null) {
        promise.then(
            editor -> {
              initEditor(file, callback, fileType, editor, editorPartStackConsumer, editorProvider);
            });
        return;
      }
    }

    final EditorPartPresenter editor = editorProvider.getEditor();
    initEditor(file, callback, fileType, editor, editorPartStackConsumer, editorProvider);
  }

  private void initEditor(
      VirtualFile file,
      OpenEditorCallback openEditorCallback,
      FileType fileType,
      EditorPartPresenter editor,
      EditorPartStack editorPartStack,
      EditorProvider editorProvider) {
    OpenEditorCallback initializeCallback =
        new OpenEditorCallbackImpl() {
          @Override
          public void onEditorOpened(EditorPartPresenter editor) {
            editorPartStack.addPart(editor);
            editorMultiPartStack.setActivePart(editor);

            openedEditors.add(editor);
            removeFromOpeningFilesList(file.getLocation(), editorPartStack);

            openEditorCallback.onEditorOpened(editor);

            eventBus.fireEvent(new EditorOpenedEvent(file, editor));
            eventBus.fireEvent(FileEvent.createFileOpenedEvent(file));
          }

          @Override
          public void onInitializationFailed() {
            openEditorCallback.onInitializationFailed();

            removeFromOpeningFilesList(file.getLocation(), editorPartStack);

            if (!openingEditorsPathsToStacks.containsKey(editorPartStack)
                && editorPartStack.getParts().isEmpty()) {
              editorMultiPartStack.removePartStack(editorPartStack);
            }
          }
        };

    editor.init(new EditorInputImpl(fileType, file), initializeCallback);
    finalizeInit(file, editor, editorProvider);
  }

  private void finalizeInit(
      VirtualFile file, EditorPartPresenter editor, EditorProvider editorProvider) {
    openedEditorsToProviders.put(editor, editorProvider.getId());

    editor.addCloseHandler(this);
    editor.addPropertyListener(
        (source, propId) -> {
          if (propId == EditorPartPresenter.PROP_INPUT) {
            if (editor instanceof HasReadOnlyProperty) {
              ((HasReadOnlyProperty) editor).setReadOnly(file.isReadOnly());
            }

            if (editor instanceof TextEditor) {
              editorContentSynchronizer.trackEditor(editor);
            }
          }
        });
  }

  private boolean isFileOpening(Path path, EditorPartStack editorPartStack) {
    Set<Path> openingFiles = openingEditorsPathsToStacks.get(editorPartStack);
    return openingFiles != null && openingFiles.contains(path);
  }

  private void addToOpeningFilesList(Path path, EditorPartStack editorPartStack) {
    if (editorPartStack == null) {
      return;
    }

    Set<Path> openingFiles =
        openingEditorsPathsToStacks.computeIfAbsent(editorPartStack, k -> new HashSet<>());
    openingFiles.add(path);
  }

  private void removeFromOpeningFilesList(Path path, EditorPartStack editorPartStack) {
    if (editorPartStack == null || !openingEditorsPathsToStacks.containsKey(editorPartStack)) {
      return;
    }

    Set<Path> openingFiles = openingEditorsPathsToStacks.get(editorPartStack);
    openingFiles.remove(path);
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
    return editorPartStack == null
        ? null
        : (EditorPartPresenter) editorPartStack.getPartByPath(path);
  }

  /** {@inheritDoc} */
  @Override
  public void saveAll(final AsyncCallback<Void> callback) {
    dirtyEditors = getDirtyEditors();
    if (dirtyEditors.isEmpty()) {
      callback.onSuccess(null);
    } else {
      doSave(callback);
    }
  }

  private void doSave(final AsyncCallback<Void> callback) {
    final EditorPartPresenter partPresenter = dirtyEditors.get(0);
    partPresenter.doSave(
        new AsyncCallback<EditorInput>() {
          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(EditorInput result) {
            dirtyEditors.remove(partPresenter);
            if (dirtyEditors.isEmpty()) {
              callback.onSuccess(null);
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
        file.put("CURSOR_OFFSET", ((TextEditor) part).getCursorOffset());
        file.put("TOP_VISIBLE_LINE", ((TextEditor) part).getTopVisibleLine());
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
  public Promise<Void> loadState(@NotNull final JsonObject state) {
    if (state.hasKey("FILES")) {
      JsonObject files = state.getObject("FILES");
      EditorPartStack editorPartStackConsumer = editorMultiPartStack.getActivePartStack();
      if (editorPartStackConsumer == null) {
        editorPartStackConsumer = editorMultiPartStack.createRootPartStack();
      }

      final Map<EditorPartPresenter, EditorPartStack> activeEditors = new HashMap<>();
      List<Promise<Void>> restore = restore(files, editorPartStackConsumer, activeEditors);
      Promise<ArrayOf<?>> promise =
          promiseProvider.all2(restore.toArray(new Promise[restore.size()]));
      promise.then(
          (Operation)
              ignored -> {
                String activeFile = "";
                if (state.hasKey("ACTIVE_EDITOR")) {
                  activeFile = state.getString("ACTIVE_EDITOR");
                }
                EditorPartPresenter activeEditorPart = null;
                for (Map.Entry<EditorPartPresenter, EditorPartStack> entry :
                    activeEditors.entrySet()) {
                  entry.getValue().setActivePart(entry.getKey());
                  if (activeFile.equals(
                      entry.getKey().getEditorInput().getFile().getLocation().toString())) {
                    activeEditorPart = entry.getKey();
                  }
                }
                workspaceAgent.setActivePart(activeEditorPart);
              });

      return promise.thenPromise(ignored -> promiseProvider.resolve(null));
    }

    return promiseProvider.resolve(null);
  }

  private List<Promise<Void>> restore(
      JsonObject files,
      EditorPartStack editorPartStack,
      Map<EditorPartPresenter, EditorPartStack> activeEditors) {

    if (files.hasKey("FILES")) {
      // plain
      JsonArray filesArray = files.getArray("FILES");
      List<Promise<Void>> promises = new ArrayList<>();
      for (int i = 0; i < filesArray.length(); i++) {
        JsonObject file = filesArray.getObject(i);
        Promise<Void> openFile = openFile(file, editorPartStack, activeEditors);
        promises.add(openFile);
      }
      return promises;
    } else {
      // split
      return restoreSplit(files, editorPartStack, activeEditors);
    }
  }

  private List<Promise<Void>> restoreSplit(
      JsonObject files,
      EditorPartStack editorPartStack,
      Map<EditorPartPresenter, EditorPartStack> activeEditors) {
    JsonObject splitFirst = files.getObject("SPLIT_FIRST");
    String direction = files.getString("DIRECTION");
    double size = files.getNumber("SIZE");
    EditorPartStack split =
        editorMultiPartStack.split(
            editorPartStack, new Constraints(Direction.valueOf(direction), null), size);
    List<Promise<Void>> restoreFirst = restore(splitFirst, editorPartStack, activeEditors);
    JsonObject splitSecond = files.getObject("SPLIT_SECOND");
    List<Promise<Void>> restoreSecond = restore(splitSecond, split, activeEditors);
    List<Promise<Void>> result = new ArrayList<>();
    result.addAll(restoreFirst);
    result.addAll(restoreSecond);
    return result;
  }

  private Promise<Void> openFile(
      final JsonObject file,
      final EditorPartStack editorPartStack,
      final Map<EditorPartPresenter, EditorPartStack> activeEditors) {
    return AsyncPromiseHelper.createFromAsyncRequest(
        new AsyncPromiseHelper.RequestCall<Void>() {
          @Override
          public void makeCall(final AsyncCallback<Void> callback) {
            String location = file.getString("PATH");
            Path path = Path.valueOf(location);
            if (isFileOpening(path, editorPartStack)) {
              callback.onSuccess(null);
              return;
            }

            addToOpeningFilesList(path, editorPartStack);
            resourceProvider
                .getResource(location)
                .then(
                    new Operation<java.util.Optional<VirtualFile>>() {
                      @Override
                      public void apply(java.util.Optional<VirtualFile> optionalFile)
                          throws OperationException {
                        if (optionalFile.isPresent()) {
                          restoreCreateEditor(
                              optionalFile.get(), file, editorPartStack, callback, activeEditors);
                        } else {
                          removeFromOpeningFilesList(path, editorPartStack);
                          callback.onSuccess(null);
                        }
                      }
                    });
          }
        });
  }

  private void restoreCreateEditor(
      final VirtualFile resourceFile,
      JsonObject file,
      final EditorPartStack editorPartStack,
      final AsyncCallback<Void> openCallback,
      final Map<EditorPartPresenter, EditorPartStack> activeEditors) {
    String providerId = file.getString("EDITOR_PROVIDER");
    final OpenEditorCallback callback;
    if (file.hasKey("CURSOR_OFFSET") && file.hasKey("TOP_VISIBLE_LINE")) {
      final int cursorOffset = (int) file.getNumber("CURSOR_OFFSET");
      final int topLine = (int) file.getNumber("TOP_VISIBLE_LINE");
      callback = new RestoreStateEditorCallBack(cursorOffset, topLine);
    } else {
      callback = new OpenEditorCallbackImpl();
    }
    final boolean active = file.hasKey("ACTIVE") && file.getBoolean("ACTIVE");

    final EditorProvider provider = editorRegistry.findEditorProviderById(providerId);
    if (provider instanceof AsyncEditorProvider) {
      ((AsyncEditorProvider) provider)
          .createEditor(resourceFile)
          .then(
              editor -> {
                restoreInitEditor(
                        resourceFile,
                        callback,
                        fileTypeRegistry.getFileTypeByFile(resourceFile),
                        editor,
                        provider,
                        editorPartStack)
                    .then(
                        arg -> {
                          if (active) {
                            activeEditors.put(editor, editorPartStack);
                          }
                          openCallback.onSuccess(null);
                        });
              });
    } else {
      EditorPartPresenter editor = provider.getEditor();
      restoreInitEditor(
              resourceFile,
              callback,
              fileTypeRegistry.getFileTypeByFile(resourceFile),
              editor,
              provider,
              editorPartStack)
          .then(
              arg -> {
                if (active) {
                  activeEditors.put(editor, editorPartStack);
                }
                openCallback.onSuccess(null);
              });
    }
  }

  private Promise<Void> restoreInitEditor(
      final VirtualFile file,
      final OpenEditorCallback openEditorCallback,
      FileType fileType,
      final EditorPartPresenter editor,
      EditorProvider editorProvider,
      EditorPartStack editorPartStack) {
    return AsyncPromiseHelper.createFromAsyncRequest(
        (AsyncCallback<Void> promiseCallback) -> {
          OpenEditorCallback initializeCallback =
              new OpenEditorCallbackImpl() {
                @Override
                public void onEditorOpened(EditorPartPresenter editor) {
                  editorPartStack.addPart(editor);

                  openedEditors.add(editor);
                  removeFromOpeningFilesList(file.getLocation(), editorPartStack);

                  promiseCallback.onSuccess(null);
                  openEditorCallback.onEditorOpened(editor);
                  openEditorCallback.onEditorActivated(editor);

                  eventBus.fireEvent(new EditorOpenedEvent(file, editor));
                  eventBus.fireEvent(FileEvent.createFileOpenedEvent(file));
                }

                @Override
                public void onInitializationFailed() {
                  promiseCallback.onFailure(
                      new Exception("Can not initialize editor for " + file.getLocation()));
                  openEditorCallback.onInitializationFailed();
                  removeFromOpeningFilesList(file.getLocation(), editorPartStack);

                  if (!openingEditorsPathsToStacks.containsKey(editorPartStack)
                      && editorPartStack.getParts().isEmpty()) {
                    editorMultiPartStack.removePartStack(editorPartStack);
                  }
                }
              };

          editor.init(new EditorInputImpl(fileType, file), initializeCallback);
          finalizeInit(file, editor, editorProvider);
        });
  }

  @Override
  public void onSelectionChanged(SelectionChangedEvent event) {
    final String isLinkedWithEditor =
        preferencesManager.getValue(LinkWithEditorAction.LINK_WITH_EDITOR);
    if (!parseBoolean(isLinkedWithEditor)) {
      return;
    }

    final Selection<?> selection = event.getSelection();
    if (selection instanceof Selection.NoSelectionProvided) {
      return;
    }

    Resource currentResource = null;

    if (selection == null
        || selection.getHeadElement() == null
        || selection.getAllElements().size() > 1) {
      return;
    }

    final Object headObject = selection.getHeadElement();

    if (headObject instanceof HasDataObject) {
      Object data = ((HasDataObject) headObject).getData();

      if (data instanceof Resource) {
        currentResource = (Resource) data;
      }
    } else if (headObject instanceof Resource) {
      currentResource = (Resource) headObject;
    }

    EditorPartStack activePartStack = editorMultiPartStack.getActivePartStack();
    if (currentResource == null || activePartStack == null || activeEditor == null) {
      return;
    }

    final Path locationOfActiveOpenedFile = activeEditor.getEditorInput().getFile().getLocation();
    final Path selectedResourceLocation = currentResource.getLocation();
    if (!(activePart instanceof ProjectExplorerPresenter)
        && selectedResourceLocation.equals(locationOfActiveOpenedFile)) {
      return;
    }

    PartPresenter partPresenter = activePartStack.getPartByPath(selectedResourceLocation);
    if (partPresenter != null) {
      workspaceAgent.setActivePart(partPresenter, EDITING);
    }
  }

  @Override
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    for (EditorPartPresenter editor : getOpenedEditors()) {
      closeEditor(editor);
    }
  }

  @Override
  public void onWsAgentServerStopped(WsAgentServerStoppedEvent event) {
    List<EditorPartPresenter> editorsToReadOnlyMode =
        getOpenedEditors()
            .stream()
            .filter(
                editor ->
                    editor instanceof HasReadOnlyProperty
                        && !((HasReadOnlyProperty) editor).isReadOnly())
            .collect(toList());

    if (editorsToReadOnlyMode.isEmpty()) {
      return;
    }

    notificationManager.notify(
        "", coreLocalizationConstant.messageSwitchEditorsInReadOnlyMode(), WARNING, EMERGE_MODE);

    editorsToReadOnlyMode.forEach(
        editor -> {
          EditorTab editorTab = editorMultiPartStack.getTabByPart(editor);
          if (editorTab != null) {
            editorTab.setReadOnlyMark(true);
          }

          ((HasReadOnlyProperty) editor).setReadOnly(true);
        });
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
        TextEditor textEditor = (TextEditor) editor;
        textEditor.getCursorModel().setCursorPosition(cursorOffset);
      }
    }

    @Override
    public void onEditorActivated(EditorPartPresenter editor) {
      if (editor instanceof TextEditor) {
        Scheduler.get().scheduleDeferred(() -> ((TextEditor) editor).setTopLine(topLine));
      }
    }
  }

  @Override
  public int getPriority() {
    return MIN_PRIORITY;
  }

  @Override
  public String getId() {
    return "editor";
  }
}
