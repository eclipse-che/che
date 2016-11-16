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
package org.eclipse.che.ide.editor.orion.client;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.actions.LinkWithEditorAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.debug.BreakpointRendererFactory;
import org.eclipse.che.ide.api.debug.HasBreakpointRenderer;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelEvent;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelHandler;
import org.eclipse.che.ide.api.editor.annotation.ClearAnnotationModelEvent;
import org.eclipse.che.ide.api.editor.annotation.ClearAnnotationModelHandler;
import org.eclipse.che.ide.api.editor.annotation.HasAnnotationRendering;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistantFactory;
import org.eclipse.che.ide.api.editor.codeassist.CompletionsSource;
import org.eclipse.che.ide.api.editor.codeassist.HasCompletionInformation;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.document.DocumentStorage;
import org.eclipse.che.ide.api.editor.editorconfig.EditorUpdateAction;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.events.CompletionRequestEvent;
import org.eclipse.che.ide.api.editor.events.DocumentReadyEvent;
import org.eclipse.che.ide.api.editor.events.GutterClickEvent;
import org.eclipse.che.ide.api.editor.events.GutterClickHandler;
import org.eclipse.che.ide.api.editor.filetype.FileTypeIdentifier;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.gutter.Gutters;
import org.eclipse.che.ide.api.editor.gutter.HasGutter;
import org.eclipse.che.ide.api.editor.keymap.KeyBinding;
import org.eclipse.che.ide.api.editor.keymap.KeyBindingAction;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.link.LinkedModelData;
import org.eclipse.che.ide.api.editor.link.LinkedModelGroup;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistAssistant;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistantFactory;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerWithAutoSave;
import org.eclipse.che.ide.api.editor.signature.SignatureHelp;
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.ContentInitializedHandler;
import org.eclipse.che.ide.api.editor.texteditor.CursorModelWithHandler;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidgetFactory;
import org.eclipse.che.ide.api.editor.texteditor.HandlesTextOperations;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.HasKeyBindings;
import org.eclipse.che.ide.api.editor.texteditor.HasReadOnlyProperty;
import org.eclipse.che.ide.api.editor.texteditor.TemporaryKeyBindingsManager;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorOperations;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPartView;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.event.ng.DeletedFilesController;
import org.eclipse.che.ide.api.hotkeys.HasHotKeyItems;
import org.eclipse.che.ide.api.hotkeys.HotKeyItem;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelDataOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelGroupOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelOverlay;
import org.eclipse.che.ide.editor.orion.client.menu.EditorContextMenu;
import org.eclipse.che.ide.editor.orion.client.signature.SignatureHelpView;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.resource.Path;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingStartEvent;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.DERIVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

/**
 * {@link TextEditor} using orion.
 * This class is only defined to allow the Gin binding to be performed.
 */
public class OrionEditorPresenter extends AbstractEditorPresenter implements TextEditor,
                                                                             UndoableEditor,
                                                                             HasBreakpointRenderer,
                                                                             HasReadOnlyProperty,
                                                                             HandlesTextOperations,
                                                                             EditorWithAutoSave,
                                                                             EditorWithErrors,
                                                                             HasHotKeyItems,
                                                                             TextEditorPartView.Delegate,
                                                                             HasAnnotationRendering,
                                                                             HasLinkedMode,
                                                                             HasCompletionInformation,
                                                                             HasGutter {
    /** File type used when we have no idea of the actual content type. */
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private static final String TOGGLE_LINE_BREAKPOINT = "Toggle line breakpoint";

    private final CodeAssistantFactory                   codeAssistantFactory;
    private final DeletedFilesController                 deletedFilesController;
    private final BreakpointManager                      breakpointManager;
    private final PreferencesManager                     preferencesManager;
    private final BreakpointRendererFactory              breakpointRendererFactory;
    private final DialogFactory                          dialogFactory;
    private final DocumentStorage                        documentStorage;
    private final EditorMultiPartStackPresenter          editorMultiPartStackPresenter;
    private final EditorLocalizationConstants            constant;
    private final EditorWidgetFactory<OrionEditorWidget> editorWidgetFactory;
    private final EditorInitializePromiseHolder editorModule;
    private final TextEditorPartView            editorView;
    private final EventBus                      generalEventBus;
    private final FileTypeIdentifier            fileTypeIdentifier;
    private final QuickAssistantFactory         quickAssistantFactory;
    private final WorkspaceAgent                workspaceAgent;
    private final NotificationManager           notificationManager;
    private final AppContext                    appContext;
    private final SignatureHelpView             signatureHelpView;
    private final EditorContextMenu             contextMenu;

    private final AnnotationRendering rendering = new AnnotationRendering();
    private HasKeyBindings           keyBindingsManager;
    private List<EditorUpdateAction> updateActions;
    private TextEditorConfiguration  configuration;
    private OrionEditorWidget        editorWidget;
    private Document                 document;
    private CursorModelWithHandler   cursorModel;
    private QuickAssistAssistant     quickAssistant;
    /** The editor's error state. */
    private EditorState              errorState;
    private boolean                  delayedFocus;
    private boolean                  isFocused;
    private BreakpointRenderer       breakpointRenderer;
    private List<String>             fileTypes;
    private TextPosition             cursorPosition;
    private HandlerRegistration      resourceChangeHandler;
    private OrionEditorInit          editorInit;

    @Inject
    public OrionEditorPresenter(final CodeAssistantFactory codeAssistantFactory,
                                final DeletedFilesController deletedFilesController,
                                final BreakpointManager breakpointManager,
                                final PreferencesManager preferencesManager,
                                final BreakpointRendererFactory breakpointRendererFactory,
                                final DialogFactory dialogFactory,
                                final DocumentStorage documentStorage,
                                final EditorMultiPartStackPresenter editorMultiPartStackPresenter,
                                final EditorLocalizationConstants constant,
                                final EditorWidgetFactory<OrionEditorWidget> editorWigetFactory,
                                final EditorInitializePromiseHolder editorModule,
                                final TextEditorPartView editorView,
                                final EventBus eventBus,
                                final FileTypeIdentifier fileTypeIdentifier,
                                final QuickAssistantFactory quickAssistantFactory,
                                final WorkspaceAgent workspaceAgent,
                                final NotificationManager notificationManager,
                                final AppContext appContext,
                                final SignatureHelpView signatureHelpView,
                                final EditorContextMenu contextMenu) {
        this.codeAssistantFactory = codeAssistantFactory;
        this.deletedFilesController = deletedFilesController;
        this.breakpointManager = breakpointManager;
        this.preferencesManager = preferencesManager;
        this.breakpointRendererFactory = breakpointRendererFactory;
        this.dialogFactory = dialogFactory;
        this.documentStorage = documentStorage;
        this.editorMultiPartStackPresenter = editorMultiPartStackPresenter;
        this.constant = constant;
        this.editorWidgetFactory = editorWigetFactory;
        this.editorModule = editorModule;
        this.editorView = editorView;
        this.generalEventBus = eventBus;
        this.fileTypeIdentifier = fileTypeIdentifier;
        this.quickAssistantFactory = quickAssistantFactory;
        this.workspaceAgent = workspaceAgent;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.signatureHelpView = signatureHelpView;
        this.contextMenu = contextMenu;

        keyBindingsManager = new TemporaryKeyBindingsManager();

        this.editorView.setDelegate(this);
    }

    @Override
    protected void initializeEditor(final EditorAgent.OpenEditorCallback callback) {
        QuickAssistProcessor processor = configuration.getQuickAssistProcessor();
        if (quickAssistantFactory != null && processor != null) {
            quickAssistant = quickAssistantFactory.createQuickAssistant(this);
            quickAssistant.setQuickAssistProcessor(processor);
        }

        editorInit = new OrionEditorInit(configuration,
                                         this.codeAssistantFactory,
                                         this.quickAssistant,
                                         this);

        Promise<Void> initializerPromise = editorModule.getInitializerPromise();
        initializerPromise.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                displayErrorPanel(constant.editorInitErrorMessage());
                callback.onInitializationFailed();
            }
        }).thenPromise(new Function<Void, Promise<String>>() {
            @Override
            public Promise<String> apply(Void arg) throws FunctionException {
                return documentStorage.getDocument(input.getFile());
            }
        }).then(new Operation<String>() {
            @Override
            public void apply(String content) throws OperationException {
                createEditor(content);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                displayErrorPanel(constant.editorFileErrorMessage());
                callback.onInitializationFailed();
            }
        });

    }

    private void createEditor(final String content) {
        this.fileTypes = detectFileType(getEditorInput().getFile());
        editorWidgetFactory.createEditorWidget(fileTypes, new EditorWidgetInitializedCallback(content));
    }

    private void setupEventHandlers() {
        this.editorWidget.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(final ChangeEvent event) {
                handleDocumentChanged();
            }
        });
        this.editorWidget.addGutterClickHandler(new GutterClickHandler() {
            @Override
            public void onGutterClick(final GutterClickEvent event) {
                if (Gutters.BREAKPOINTS_GUTTER.equals(event.getGutterId())
                    || Gutters.LINE_NUMBERS_GUTTER.equals(event.getGutterId())) {
                    breakpointManager.changeBreakpointState(event.getLineNumber());
                }
            }
        });
        this.editorWidget.addKeyBinding(new KeyBinding(true, false, false, false, KeyCodes.KEY_F8, new KeyBindingAction() {
            @Override
            public boolean action() {
                int currentLine = editorWidget.getDocument().getCursorPosition().getLine();
                breakpointManager.changeBreakpointState(currentLine);
                return true;
            }
        }), TOGGLE_LINE_BREAKPOINT);
    }

    private void setupFileContentUpdateHandler() {

        resourceChangeHandler =
                generalEventBus.addHandler(ResourceChangedEvent.getType(), new ResourceChangedEvent.ResourceChangedHandler() {
                    @Override
                    public void onResourceChanged(ResourceChangedEvent event) {
                        final ResourceDelta delta = event.getDelta();

                        switch (delta.getKind()) {
                            case ADDED:
                                onResourceCreated(delta);
                                break;
                            case REMOVED:
                                onResourceRemoved(delta);
                                break;
                            case UPDATED:
                                onResourceUpdated(delta);
                        }
                    }
                });
    }

    private void onResourceCreated(ResourceDelta delta) {
        if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) == 0) {
            return;
        }

        //file moved directly
        if (delta.getFromPath().equals(document.getFile().getLocation())) {
            final Resource resource = delta.getResource();
            final Path movedFrom = delta.getFromPath();

            if (document.getFile().getLocation().equals(movedFrom)) {
                deletedFilesController.add(movedFrom.toString());
                document.setFile((File)resource);
                input.setFile((File)resource);
            }

            updateContent();
        } else if (delta.getFromPath().isPrefixOf(document.getFile().getLocation())) { //directory where file moved
            final Path relPath = document.getFile().getLocation().removeFirstSegments(delta.getFromPath().segmentCount());
            final Path newPath = delta.getToPath().append(relPath);

            appContext.getWorkspaceRoot().getFile(newPath).then(new Operation<Optional<File>>() {
                @Override
                public void apply(Optional<File> file) throws OperationException {
                    if (file.isPresent()) {
                        final Path location = document.getFile().getLocation();
                        deletedFilesController.add(location.toString());
                        generalEventBus.fireEvent(newFileTrackingStartEvent(file.get().getLocation().toString()));

                        document.setFile(file.get());
                        input.setFile(file.get());
                        updateTabReference(file.get(), location);

                        updateContent();
                    }
                }
            });
        }

    }

    private void updateTabReference(File file, Path oldPath) {
        final PartPresenter activePart = editorMultiPartStackPresenter.getActivePart();
        final EditorPartStack activePartStack = editorMultiPartStackPresenter.getPartStackByPart(activePart);
        if (activePartStack == null) {
            return;
        }
        final EditorTab editorTab = activePartStack.getTabByPath(oldPath);
        if (editorTab != null) {
            editorTab.setFile(file);
        }
    }

    private void onResourceRemoved(ResourceDelta delta) {
        if ((delta.getFlags() & DERIVED) == 0) {
            return;
        }

        final Resource resource = delta.getResource();

        if (resource.isFile() && document.getFile().getLocation().equals(resource.getLocation())) {
            handleClose();
        }
    }

    private void onResourceUpdated(ResourceDelta delta) {
        if ((delta.getFlags() & DERIVED) == 0) {
            return;
        }

        if (delta.getResource().isFile() && document.getFile().getLocation().equals(delta.getResource().getLocation())) {
            updateContent();
        }
    }

    private void updateContent() {
        generalEventBus.fireEvent(new FileContentUpdateEvent(document.getFile().getLocation().toString()));
    }

    private void displayErrorPanel(final String message) {
        this.editorView.showPlaceHolder(new Label(message));
    }

    private void handleDocumentChanged() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                updateDirtyState(editorWidget.isDirty());
            }
        });
    }

    protected void showQuickAssist() {
        if (quickAssistant == null) {
            return;
        }
        PositionConverter positionConverter = getPositionConverter();
        if (positionConverter != null) {
            TextPosition cursor = getCursorPosition();
            PositionConverter.PixelCoordinates pixelPos = positionConverter.textToPixel(cursor);
            quickAssistant.showPossibleQuickAssists(getCursorModel().getCursorPosition().getOffset(),
                                                    pixelPos.getX(),
                                                    pixelPos.getY());
        }
    }

    @Override
    public void storeState() {
        cursorPosition = getCursorPosition();
    }

    @Override
    public void restoreState() {
        if (cursorPosition != null) {
            setFocus();

            getDocument().setCursorPosition(cursorPosition);
        }
    }

    @Override
    public void close(boolean save) {
        this.documentStorage.documentClosed(this.document);
        editorInit.uninstall();
        workspaceAgent.removePart(this);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void doRevertToSaved() {
        // do nothing
    }


    protected Widget getWidget() {
        return this.editorView.asWidget();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(getWidget());
    }

    @Override
    public String getTitleToolTip() {
        return null;
    }

    @Override
    public void onClose(@NotNull final AsyncCallback<Void> callback) {
        if (isDirty()) {
            dialogFactory.createConfirmDialog(
                    constant.askWindowCloseTitle(),
                    constant.askWindowSaveChangesMessage(getEditorInput().getName()),
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
        } else {
            handleClose();
            callback.onSuccess(null);
        }
    }

    @Override
    protected void handleClose() {
        if (resourceChangeHandler != null) {
            resourceChangeHandler.removeHandler();
            resourceChangeHandler = null;
        }
        super.handleClose();
    }

    @Override
    public TextEditorPartView getView() {
        return this.editorView;
    }

    @Override
    public void activate() {
        if (editorWidget != null) {
            editorWidget.refresh();
            editorWidget.setFocus();
            final String isLinkedWithEditor = preferencesManager.getValue(LinkWithEditorAction.LINK_WITH_EDITOR);
            if (!parseBoolean(isLinkedWithEditor)) {
                setSelection(new Selection<>(input.getFile()));
            }
        } else {
            this.delayedFocus = true;
        }
    }

    @Override
    public void initialize(@NotNull TextEditorConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public TextEditorConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public SVGResource getTitleImage() {
        return input.getSVGResource();
    }

    @Override
    public String getTitle() {
        return input.getFile().getDisplayName();
    }

    @Override
    public void doSave() {
        doSave(new AsyncCallback<EditorInput>() {
            @Override
            public void onSuccess(final EditorInput result) {
                // do nothing
            }

            @Override
            public void onFailure(final Throwable caught) {
                // do nothing
            }
        });
    }

    @Override
    public void doSave(final AsyncCallback<EditorInput> callback) {
        //If the workspace is stopped we shouldn't try to save a file
        if (appContext.getDevMachine() == null) {
            return;
        }
        this.documentStorage.saveDocument(getEditorInput(), this.document, false, new AsyncCallback<EditorInput>() {
            @Override
            public void onSuccess(EditorInput editorInput) {
                updateDirtyState(false);
                editorWidget.markClean();
                afterSave();
                if (callback != null) {
                    callback.onSuccess(editorInput);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                notificationManager.notify(constant.failedToUpdateContentOfFiles(), caught.getMessage(), FAIL, NOT_EMERGE_MODE);
                if (callback != null) {
                    callback.onFailure(caught);
                }
            }
        });
    }

    @Override
    public void doSaveAs() {

    }

    protected void afterSave() {
    }

    @Override
    public HandlesUndoRedo getUndoRedo() {
        if (this.editorWidget != null) {
            return this.editorWidget.getUndoRedo();
        } else {
            return null;
        }
    }

    @Override
    public EditorState getErrorState() {
        return this.errorState;
    }

    @Override
    public void setErrorState(EditorState errorState) {
        this.errorState = errorState;
        firePropertyChange(ERROR_STATE);
    }

    @Override
    public BreakpointRenderer getBreakpointRenderer() {
        if (this.breakpointRenderer == null && this.editorWidget != null && this instanceof HasGutter) {
            this.breakpointRenderer = this.breakpointRendererFactory.create(((HasGutter)this).getGutter(),
                                                                            this.editorWidget.getLineStyler(),
                                                                            this.document);
        }
        return this.breakpointRenderer;
    }

    @Override
    public Document getDocument() {
        return this.document;
    }

    @Override
    public String getContentType() {
        // Before the editor content is ready, the content type is not defined
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            return null;
        } else {
            return this.fileTypes.get(0);
        }
    }

    @Override
    public TextRange getSelectedTextRange() {
        return getDocument().getSelectedTextRange();
    }

    @Override
    public LinearRange getSelectedLinearRange() {
        return getDocument().getSelectedLinearRange();
    }

    @Override
    public void showMessage(String message) {
        this.editorWidget.showMessage(message);
    }

    @Override
    public TextPosition getCursorPosition() {
        return getDocument().getCursorPosition();
    }

    @Override
    public int getCursorOffset() {
        final TextPosition textPosition = getDocument().getCursorPosition();
        return getDocument().getIndexFromPosition(textPosition);
    }

    @Override
    public int getTopVisibleLine() {
        return editorWidget.getTopVisibleLine();
    }

    @Override
    public void setTopLine(int line) {
        editorWidget.setTopLine(line);
    }

    @Override
    public void refreshEditor() {
        if (this.updateActions != null) {
            for (final EditorUpdateAction action : this.updateActions) {
                action.doRefresh();
            }
        }
    }

    @Override
    public void addEditorUpdateAction(EditorUpdateAction action) {
        if (action == null) {
            return;
        }
        if (this.updateActions == null) {
            this.updateActions = new ArrayList<>();
        }
        this.updateActions.add(action);
    }

    @Override
    public void addKeybinding(KeyBinding keyBinding) {
        // the actual HasKeyBindings object can change, so use indirection
        getHasKeybindings().addKeyBinding(keyBinding);
    }

    private List<String> detectFileType(final VirtualFile file) {
        final List<String> result = new ArrayList<>();
        if (file != null) {
            // use the identification patterns
            final List<String> types = this.fileTypeIdentifier.identifyType(file);
            if (types != null && !types.isEmpty()) {
                result.addAll(types);
            }
        }

        // ultimate fallback - can't make more generic for text
        result.add(DEFAULT_CONTENT_TYPE);

        return result;
    }

    public HasKeyBindings getHasKeybindings() {
        return this.keyBindingsManager;
    }

    @Override
    public CursorModelWithHandler getCursorModel() {
        return this.cursorModel;
    }

    @Override
    public PositionConverter getPositionConverter() {
        return this.editorWidget.getPositionConverter();
    }

    public void showCompletionProposals(CompletionsSource source) {
        this.editorView.showCompletionProposals(this.editorWidget, source);
    }

    public void showCompletionProposals() {
        this.editorView.showCompletionProposals(this.editorWidget);
    }

    private void switchHasKeybinding() {
        final HasKeyBindings current = getHasKeybindings();
        if (!(current instanceof TemporaryKeyBindingsManager)) {
            return;
        }
        // change the key binding instance and add all bindings to the new one
        this.keyBindingsManager = this.editorWidget;
        final List<KeyBinding> bindings = ((TemporaryKeyBindingsManager)current).getbindings();
        for (final KeyBinding binding : bindings) {
            this.keyBindingsManager.addKeyBinding(binding);
        }
    }

    @Override
    public List<HotKeyItem> getHotKeys() {
        return editorWidget.getHotKeys();
    }

    @Override
    public void onResize() {
        if (this.editorWidget != null) {
            this.editorWidget.onResize();
        }
    }

    @Override
    public void editorLostFocus() {
        this.editorView.updateInfoPanelUnfocused(this.document.getLineCount());
        this.isFocused = false;
        if (isDirty()) {
            doSave();
        }
    }

    @Override
    public void editorGotFocus() {
        this.isFocused = true;
        this.editorView.updateInfoPanelPosition(this.document.getCursorPosition());
    }

    @Override
    public void editorCursorPositionChanged() {
        this.editorView.updateInfoPanelPosition(this.document.getCursorPosition());
    }

    @Override
    public boolean canDoOperation(int operation) {
        if (TextEditorOperations.CODEASSIST_PROPOSALS == operation) {
            Map<String, CodeAssistProcessor> contentAssistProcessors = getConfiguration().getContentAssistantProcessors();
            if (contentAssistProcessors != null && !contentAssistProcessors.isEmpty()) {
                return true;
            }
        }
        if (TextEditorOperations.FORMAT == operation) {
            if (getConfiguration().getContentFormatter() != null) {
                return true;
            }
        }

        if (TextEditorOperations.QUICK_ASSIST == operation) {
            if (quickAssistant != null) {
                return true;
            }
        }

        if (TextEditorOperations.SIGNATURE_HELP == operation) {
            if (getConfiguration().getSignatureHelpProvider() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doOperation(int operation) {
        switch (operation) {
            case TextEditorOperations.CODEASSIST_PROPOSALS:
                if (this.document != null) {
                    this.document.getDocumentHandle().getDocEventBus().fireEvent(new CompletionRequestEvent());
                }
                break;
            case TextEditorOperations.FORMAT:
                ContentFormatter formatter = getConfiguration().getContentFormatter();
                if (this.document != null && formatter != null) {
                    formatter.format(getDocument());
                }
                break;
            case TextEditorOperations.QUICK_ASSIST:
                showQuickAssist();
                break;
            case TextEditorOperations.SIGNATURE_HELP:
                showSignatureHelp();
                break;
            default:
                throw new UnsupportedOperationException("Operation code: " + operation + " is not supported!");
        }
    }

    private void showSignatureHelp() {
        //TODO XXX
        SignatureHelpProvider signatureHelpProvider = getConfiguration().getSignatureHelpProvider();
        if (document != null && signatureHelpProvider != null) {
            Promise<Optional<SignatureHelp>> promise = signatureHelpProvider.signatureHelp(document, getCursorOffset());
            PositionConverter.PixelCoordinates coordinates = getPositionConverter().offsetToPixel(getCursorOffset());
            signatureHelpView.showSignature(promise, coordinates.getX(), coordinates.getY() - editorWidget.getTextView().getLineHeight());
        }
    }

    @Override
    public boolean isReadOnly() {
        return this.editorWidget.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.editorWidget.setReadOnly(readOnly);
    }

    @Override
    public EditorWidget getEditorWidget() {
        return this.editorWidget;
    }

    @Override
    public boolean isFocused() {
        return this.isFocused;
    }

    @Override
    public boolean isAutoSaveEnabled() {
        ReconcilerWithAutoSave autoSave = getAutoSave();
        return autoSave != null && autoSave.isAutoSaveEnabled();
    }

    private ReconcilerWithAutoSave getAutoSave() {
        Reconciler reconciler = getConfiguration().getReconciler();

        if (reconciler != null && reconciler instanceof ReconcilerWithAutoSave) {
            return ((ReconcilerWithAutoSave)reconciler);
        }
        return null;
    }

    @Override
    public void enableAutoSave() {
        ReconcilerWithAutoSave autoSave = getAutoSave();
        if (autoSave != null) {
            autoSave.enableAutoSave();
        }
    }

    @Override
    public void disableAutoSave() {
        ReconcilerWithAutoSave autoSave = getAutoSave();
        if (autoSave != null) {
            autoSave.disableAutoSave();
        }
    }

    @Override
    public void configure(AnnotationModel model, DocumentHandle document) {
        document.getDocEventBus().addHandler(AnnotationModelEvent.TYPE, rendering);
        document.getDocEventBus().addHandler(ClearAnnotationModelEvent.TYPE, rendering);
    }

    @Override
    public LinkedMode getLinkedMode() {
        EditorWidget editorWidget = getEditorWidget();
        if (editorWidget != null) {
            OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
            return orion.getLinkedMode();
        }
        return null;
    }

    @Override
    public LinkedModel createLinkedModel() {
        return OrionLinkedModelOverlay.create();
    }

    @Override
    public LinkedModelGroup createLinkedGroup() {
        return OrionLinkedModelGroupOverlay.create();
    }

    @Override
    public LinkedModelData createLinkedModelData() {
        return OrionLinkedModelDataOverlay.create();
    }

    @Override
    public void showCompletionInformation() {
        EditorWidget editorWidget = getEditorWidget();
        if (editorWidget != null) {
            OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
            orion.showCompletionInformation();
        }
    }

    @Override
    public Gutter getGutter() {
        final EditorWidget editorWidget = getEditorWidget();
        if (editorWidget instanceof HasGutter) {
            return ((HasGutter)editorWidget).getGutter();
        } else {
            throw new IllegalStateException("incorrect editor state");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus() {
        EditorWidget editorWidget = getEditorWidget();
        if (editorWidget != null) {
            OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
            orion.setFocus();
        }

    }

    private class AnnotationRendering implements AnnotationModelHandler, ClearAnnotationModelHandler {

        @Override
        public void onAnnotationModel(AnnotationModelEvent event) {
            EditorWidget editorWidget = getEditorWidget();
            if (editorWidget != null) {
                OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
                orion.showErrors(event);
            }
        }

        @Override
        public void onClearModel(ClearAnnotationModelEvent event) {
            EditorWidget editorWidget = getEditorWidget();
            if (editorWidget != null) {
                OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
                orion.clearErrors();
            }
        }
    }

    private class EditorWidgetInitializedCallback implements EditorWidget.WidgetInitializedCallback {
        private final String content;

        private boolean isInitialized;

        private EditorWidgetInitializedCallback(String content) {
            this.content = content;
        }

        @Override
        public void initialized(EditorWidget widget) {
            editorWidget = (OrionEditorWidget)widget;
            // finish editor initialization
            editorView.setEditorWidget(editorWidget);

            document = editorWidget.getDocument();
            document.setFile(input.getFile());
            cursorModel = new OrionCursorModel(document);

            editorWidget.setTabSize(configuration.getTabWidth());

            // initialize info panel
            editorView.initInfoPanel(editorWidget.getMode(),
                                     editorWidget.getKeymap(),
                                     document.getLineCount(),
                                     configuration.getTabWidth());

            //TODO: delayed activation
            // handle delayed focus (initialization editor widget)
            // should also check if I am visible, but how ?
            if (delayedFocus) {
                editorWidget.refresh();
                editorWidget.setFocus();
                setSelection(new Selection<>(input.getFile()));
                delayedFocus = false;
            }

            // delayed keybindings creation ?
            switchHasKeybinding();

            editorWidget.setValue(content, new ContentInitializedHandler() {
                @Override
                public void onContentInitialized() {
                    if (isInitialized) {
                        return;
                    }
                    editorInit.init(document);
                    generalEventBus.fireEvent(new DocumentReadyEvent(document));
                    firePropertyChange(PROP_INPUT);
                    setupEventHandlers();
                    setupFileContentUpdateHandler();

                    isInitialized = true;
                }
            });

            editorWidget.addDomHandler(new ContextMenuHandler() {
                @Override
                public void onContextMenu(ContextMenuEvent event) {
                    contextMenu.show(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                }
            }, ContextMenuEvent.getType());
        }
    }
}
