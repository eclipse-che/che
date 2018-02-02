/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.orion.client;

import static org.eclipse.che.ide.editor.orion.client.KeyMode.EMACS;
import static org.eclipse.che.ide.editor.orion.client.KeyMode.VI;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelEvent;
import org.eclipse.che.ide.api.editor.codeassist.AdditionalInfoCallback;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.codeassist.CompletionReadyCallback;
import org.eclipse.che.ide.api.editor.codeassist.CompletionsSource;
import org.eclipse.che.ide.api.editor.events.CursorActivityEvent;
import org.eclipse.che.ide.api.editor.events.CursorActivityHandler;
import org.eclipse.che.ide.api.editor.events.GutterClickEvent;
import org.eclipse.che.ide.api.editor.events.GutterClickHandler;
import org.eclipse.che.ide.api.editor.events.HasCursorActivityHandlers;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.gutter.Gutters;
import org.eclipse.che.ide.api.editor.gutter.HasGutter;
import org.eclipse.che.ide.api.editor.hotkeys.HotKeyItem;
import org.eclipse.che.ide.api.editor.keymap.KeyBinding;
import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.api.editor.keymap.KeymapChangeEvent;
import org.eclipse.che.ide.api.editor.keymap.KeymapChangeHandler;
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.api.editor.text.RegionImpl;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.api.editor.texteditor.ContentInitializedHandler;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.LineStyler;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.selection.SelectionChangedEvent;
import org.eclipse.che.ide.api.selection.SelectionChangedHandler;
import org.eclipse.che.ide.editor.EditorAgentImpl;
import org.eclipse.che.ide.editor.orion.client.events.HasScrollHandlers;
import org.eclipse.che.ide.editor.orion.client.events.ScrollEvent;
import org.eclipse.che.ide.editor.orion.client.events.ScrollHandler;
import org.eclipse.che.ide.editor.orion.client.incremental.find.IncrementalFindReportStatusObserver;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationModelOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationTypeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationsOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionContentAssistOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOptionsOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorViewOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEventTargetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionInputChangedEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyBindingsRelationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyModeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyStrokeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionRulerClickEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionSelectionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionStyleOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.StatusMessageReporterOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.UiUtilsOverlay;
import org.eclipse.che.ide.editor.preferences.keymaps.KeyMapsPreferencePresenter;
import org.eclipse.che.ide.status.message.StatusMessageReporter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.requirejs.ModuleHolder;

/**
 * Orion implementation for {@link EditorWidget}.
 *
 * @author "Mickaël Leduque"
 */
public class OrionEditorWidget extends Composite
    implements EditorWidget,
        HasChangeHandlers,
        HasCursorActivityHandlers,
        HasScrollHandlers,
        HasGutter {

  /** The UI binder instance. */
  private static final OrionEditorWidgetUiBinder UIBINDER =
      GWT.create(OrionEditorWidgetUiBinder.class);

  /** The logger. */
  private static final Logger LOG = Logger.getLogger(OrionEditorWidget.class.getSimpleName());

  private final ModuleHolder moduleHolder;
  private final EventBus eventBus;
  private final KeyModeInstances keyModeInstances;
  private final JavaScriptObject uiUtilsOverlay;
  private EditorAgentImpl editorAgent;
  private final ContentAssistWidgetFactory contentAssistWidgetFactory;
  private final DialogFactory dialogFactory;
  private final PreferencesManager preferencesManager;
  private final OrionSettingsController orionSettingsController;
  private final OrionAnnotationTypeOverlay annotationType;

  private final List<OrionAnnotationOverlay> problems = new ArrayList<>();

  @UiField SimplePanel panel;
  /** The instance of the orion editor native element style. */
  @UiField EditorElementStyle editorElementStyle;

  private OrionEditorViewOverlay editorViewOverlay;
  private OrionEditorOverlay editorOverlay;
  private String modeName;
  private OrionExtRulerOverlay orionLineNumberRuler;
  /** Component that handles undo/redo. */
  private HandlesUndoRedo undoRedo;

  private OrionDocument embeddedDocument;
  private OrionKeyModeOverlay cheContentAssistMode;

  private Keymap keymap;
  private ContentAssistWidget assistWidget;
  private Gutter gutter;

  private boolean changeHandlerAdded = false;
  private boolean focusHandlerAdded = false;
  private boolean blurHandlerAdded = false;
  private boolean scrollHandlerAdded = false;
  private boolean cursorHandlerAdded = false;
  private boolean gutterClickHandlerAdded = false;

  /** Component that handles line styling. */
  private LineStyler lineStyler;

  @AssistedInject
  public OrionEditorWidget(
      final ModuleHolder moduleHolder,
      final KeyModeInstances keyModeInstances,
      final EditorAgentImpl editorAgent,
      final EventBus eventBus,
      final Provider<OrionCodeEditWidgetOverlay> orionCodeEditWidgetProvider,
      final ContentAssistWidgetFactory contentAssistWidgetFactory,
      final DialogFactory dialogFactory,
      final PreferencesManager preferencesManager,
      @Assisted final List<String> editorModes,
      @Assisted final WidgetInitializedCallback widgetInitializedCallback,
      final Provider<OrionEditorOptionsOverlay> editorOptionsProvider,
      final StatusMessageReporter statusMessageReporter,
      final IncrementalFindReportStatusObserver incrementalFindObserver,
      final OrionSettingsController orionSettingsController) {
    this.editorAgent = editorAgent;
    this.contentAssistWidgetFactory = contentAssistWidgetFactory;
    this.moduleHolder = moduleHolder;
    this.keyModeInstances = keyModeInstances;
    this.eventBus = eventBus;
    this.dialogFactory = dialogFactory;
    this.preferencesManager = preferencesManager;
    this.orionSettingsController = orionSettingsController;
    initWidget(UIBINDER.createAndBindUi(this));

    this.uiUtilsOverlay = moduleHolder.getModule("UiUtils");
    this.annotationType =
        moduleHolder
            .getModule("OrionAnnotations")
            .<OrionAnnotationsOverlay>cast()
            .getAnnotationType();

    // just first choice for the moment
    if (editorModes != null && !editorModes.isEmpty()) {
      setMode(editorModes.get(0));
    }

    panel.getElement().setId("orion-parent-" + Document.get().createUniqueId());
    panel.getElement().addClassName(this.editorElementStyle.editorParent());

    OrionEditorOptionsOverlay editorOptions =
        initEditorOptions(editorOptionsProvider.get(), statusMessageReporter);

    orionCodeEditWidgetProvider
        .get()
        .createEditorView(panel.getElement(), editorOptions)
        .then(new EditorViewCreatedOperation(widgetInitializedCallback));

    incrementalFindObserver.setEditorWidget(this);
    statusMessageReporter.registerObserver(incrementalFindObserver);

    registerPromptFunction();
  }

  private OrionEditorOptionsOverlay initEditorOptions(
      OrionEditorOptionsOverlay orionEditorOptionsOverlay,
      StatusMessageReporter statusMessageReporter) {
    StatusMessageReporterOverlay statusMessageReporterOverlay =
        StatusMessageReporterOverlay.create(statusMessageReporter);
    orionEditorOptionsOverlay.setStatusReporter(statusMessageReporterOverlay);
    return orionEditorOptionsOverlay;
  }

  private Gutter initBreakpointRuler(ModuleHolder moduleHolder) {
    JavaScriptObject orionEventTargetModule = moduleHolder.getModule("OrionEventTarget");

    orionLineNumberRuler = editorOverlay.getTextView().getRulers()[1];
    orionLineNumberRuler.overrideOnClickEvent();
    OrionEventTargetOverlay.addMixin(orionEventTargetModule, orionLineNumberRuler);

    return new OrionBreakpointRuler(orionLineNumberRuler, editorOverlay);
  }

  @Override
  public String getValue() {
    return editorOverlay.getText();
  }

  @Override
  public void setValue(String newValue, final ContentInitializedHandler initializationHandler) {
    editorOverlay.addEventListener(
        OrionInputChangedEventOverlay.TYPE,
        (OrionEditorOverlay.EventHandler<OrionInputChangedEventOverlay>)
            event -> {
              orionSettingsController.updateSettings();
              if (initializationHandler != null) {
                initializationHandler.onContentInitialized();
              }
            },
        true);

    this.editorViewOverlay.setContents(newValue, modeName);
    this.editorOverlay.getUndoStack().reset();
  }

  @Override
  public String getMode() {
    return modeName;
  }

  public void setMode(final String modeName) {
    String mode = modeName;
    if (modeName.equals("text/x-java")) {
      mode = "text/x-java-source";
    }
    LOG.fine("Requested mode: " + modeName + " kept " + mode);

    this.modeName = mode;
  }

  @Override
  public boolean isReadOnly() {
    return this.editorOverlay.getTextView().getOptions().isReadOnly();
  }

  @Override
  public void setReadOnly(final boolean isReadOnly) {
    editorViewOverlay.setReadonly(isReadOnly);
    orionSettingsController.updateSettings();
  }

  @Override
  public void setAnnotationRulerVisible(boolean show) {
    editorOverlay.setAnnotationRulerVisible(show);
  }

  @Override
  public void setFoldingRulerVisible(boolean show) {
    editorOverlay.setFoldingRulerVisible(show);
  }

  @Override
  public void setZoomRulerVisible(boolean show) {
    editorOverlay.setZoomRulerVisible(show);
  }

  @Override
  public void setOverviewRulerVisible(boolean show) {
    editorOverlay.setOverviewRulerVisible(show);
  }

  @Override
  public boolean isDirty() {
    return this.editorOverlay.isDirty();
  }

  @Override
  public void markClean() {
    this.editorOverlay.setDirty(false);
  }

  @Override
  public void markDirty() {
    this.editorOverlay.setDirty(true);
  }

  private void selectKeyMode(Keymap keymap) {
    resetKeyModes();
    Keymap usedKeymap = keymap;
    if (usedKeymap == null) {
      usedKeymap = KeyMode.DEFAULT;
    }
    if (KeyMode.DEFAULT.equals(usedKeymap)) {
      // nothing to do
    } else if (EMACS.equals(usedKeymap)) {
      this.editorOverlay.getTextView().addKeyMode(keyModeInstances.getInstance(EMACS));
    } else if (VI.equals(usedKeymap)) {
      this.editorOverlay.getTextView().addKeyMode(keyModeInstances.getInstance(VI));
    } else {
      usedKeymap = KeyMode.DEFAULT;
      Log.error(
          OrionEditorWidget.class,
          "Unknown keymap type: " + keymap + " - changing to default one.");
    }
    this.keymap = usedKeymap;
  }

  private void resetKeyModes() {
    this.editorOverlay.getTextView().removeKeyMode(keyModeInstances.getInstance(VI));
    this.editorOverlay.getTextView().removeKeyMode(keyModeInstances.getInstance(EMACS));
  }

  @Override
  public org.eclipse.che.ide.api.editor.document.Document getDocument() {
    if (this.embeddedDocument == null) {
      this.embeddedDocument =
          new OrionDocument(this.editorOverlay.getTextView(), this, editorOverlay);
    }
    return this.embeddedDocument;
  }

  @Override
  public Region getSelectedRange() {
    final OrionSelectionOverlay selection = this.editorOverlay.getSelection();

    final int start = selection.getStart();
    final int end = selection.getEnd();

    if (start < 0 || end > this.editorOverlay.getModel().getCharCount() || start > end) {
      throw new RuntimeException("Invalid selection");
    }
    return new RegionImpl(start, end - start);
  }

  @Override
  public void setSelectedRange(final Region selection, final boolean show) {
    this.editorOverlay.setSelection(selection.getOffset(), selection.getLength(), show);
  }

  @Override
  public void setDisplayRange(final Region range) {
    // show the line at the head of the range
    final int headOffset = range.getOffset() + range.getLength();
    if (range.getLength() < 0) {
      this.editorOverlay.getTextView().setTopIndex(headOffset);
    } else {
      this.editorOverlay.getTextView().setBottomIndex(headOffset);
    }
  }

  @Override
  public int getTabSize() {
    return this.editorOverlay.getTextView().getOptions().getTabSize();
  }

  @Override
  public void setTabSize(int tabSize) {
    this.editorOverlay.getTextView().getOptions().setTabSize(tabSize);
  }

  @Override
  public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
    if (!changeHandlerAdded) {
      changeHandlerAdded = true;
      final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
      textView.addEventListener(
          OrionEventConstants.MODEL_CHANGED_EVENT,
          new OrionTextViewOverlay.EventHandlerNoParameter() {

            @Override
            public void onEvent() {
              fireChangeEvent();
            }
          });
    }
    return addHandler(handler, ChangeEvent.getType());
  }

  private void fireChangeEvent() {
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
  }

  @Override
  public HandlerRegistration addCursorActivityHandler(CursorActivityHandler handler) {
    if (!cursorHandlerAdded) {
      cursorHandlerAdded = true;
      final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
      textView.addEventListener(
          OrionEventConstants.SELECTION_EVENT,
          new OrionTextViewOverlay.EventHandlerNoParameter() {

            @Override
            public void onEvent() {
              fireCursorActivityEvent();
            }
          });
    }
    return addHandler(handler, CursorActivityEvent.TYPE);
  }

  private void fireCursorActivityEvent() {
    fireEvent(new CursorActivityEvent());
  }

  @Override
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    if (!focusHandlerAdded) {
      focusHandlerAdded = true;
      final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
      textView.addEventListener(
          OrionEventConstants.FOCUS_EVENT,
          new OrionTextViewOverlay.EventHandlerNoParameter() {

            @Override
            public void onEvent() {
              fireFocusEvent();
            }
          });
    }
    return addHandler(handler, FocusEvent.getType());
  }

  private void fireFocusEvent() {
    DomEvent.fireNativeEvent(Document.get().createFocusEvent(), this);
  }

  @Override
  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    if (!blurHandlerAdded) {
      blurHandlerAdded = true;
      final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
      textView.addEventListener(
          OrionEventConstants.BLUR_EVENT,
          new OrionTextViewOverlay.EventHandlerNoParameter() {

            @Override
            public void onEvent() {
              fireBlurEvent();
            }
          });
    }
    return addHandler(handler, BlurEvent.getType());
  }

  private void fireBlurEvent() {
    DomEvent.fireNativeEvent(Document.get().createBlurEvent(), this);
  }

  @Override
  public HandlerRegistration addScrollHandler(final ScrollHandler handler) {
    if (!scrollHandlerAdded) {
      scrollHandlerAdded = true;
      final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
      textView.addEventListener(
          OrionEventConstants.SCROLL_EVENT,
          new OrionTextViewOverlay.EventHandlerNoParameter() {

            @Override
            public void onEvent() {
              fireScrollEvent();
            }
          });
    }
    return addHandler(handler, ScrollEvent.TYPE);
  }

  private void fireScrollEvent() {
    fireEvent(new ScrollEvent());
  }

  private void setupKeymode() {
    final String propertyValue =
        preferencesManager.getValue(KeyMapsPreferencePresenter.KEYMAP_PREF_KEY);

    Keymap keymap;
    try {
      keymap = Keymap.fromKey(propertyValue);
    } catch (final IllegalArgumentException e) {
      LOG.log(Level.WARNING, "Unknown value in keymap preference.", e);
      return;
    }
    selectKeyMode(keymap);
  }

  @Override
  public Keymap getKeymap() {
    return this.keymap;
  }

  @Override
  public PositionConverter getPositionConverter() {
    return embeddedDocument.getPositionConverter();
  }

  @Override
  public void setFocus() {
    this.editorOverlay.focus();
  }

  @Override
  public void showMessage(final String message) {
    this.editorOverlay.reportStatus(message);
  }

  @Override
  protected void onLoad() {
    // fix for native editor height
    if (panel.getElement().getChildCount() > 0) {
      final Element child = panel.getElement().getFirstChildElement();
      child.setId("orion-editor-" + Document.get().createUniqueId());
      child.getStyle().clearHeight();

    } else {
      LOG.severe("Orion insertion failed.");
    }
  }

  @Override
  public void onResize() {
    // redraw text and rulers
    // maybe just redrawing the text would be enough
    this.editorOverlay.getTextView().redraw();
  }

  @Override
  public HandlesUndoRedo getUndoRedo() {
    return this.undoRedo;
  }

  @Override
  public void addKeyBinding(final KeyBinding keyBinding) {
    addKeyBinding(keyBinding, "");
  }

  @Override
  public void addKeyBinding(final KeyBinding keyBinding, String actionDescription) {
    OrionKeyStrokeOverlay strokeOverlay;
    JavaScriptObject keyBindingModule = moduleHolder.getModule("OrionKeyBinding").cast();
    String type = keyBinding.getType();
    boolean modifier1 = UserAgent.isMac() ? keyBinding.isCmd() : keyBinding.isControl();
    boolean modifier2 = keyBinding.isShift();
    boolean modifier3 = keyBinding.isAlt();
    boolean modifier4 = UserAgent.isMac() ? keyBinding.isControl() : false;
    if (keyBinding.isCharacterBinding()) {
      strokeOverlay =
          OrionKeyStrokeOverlay.create(
              keyBinding.getCharacter(),
              modifier1,
              modifier2,
              modifier3,
              modifier4,
              type,
              keyBindingModule);
    } else {
      strokeOverlay =
          OrionKeyStrokeOverlay.create(
              keyBinding.getKeyCodeNumber(),
              modifier1,
              modifier2,
              modifier3,
              modifier4,
              type,
              keyBindingModule);
    }
    String actionId = "che-action-" + keyBinding.getAction().toString();
    editorOverlay.getTextView().setKeyBinding(strokeOverlay, actionId);
    editorOverlay
        .getTextView()
        .setAction(
            actionId,
            new Action() {
              @Override
              public boolean onAction() {
                return keyBinding.getAction().action();
              }
            },
            actionDescription);
  }

  @Override
  public List<HotKeyItem> getHotKeys() {
    OrionTextViewOverlay orionTextViewOverlay = editorOverlay.getTextView();
    List<HotKeyItem> hotKeyItems = new ArrayList<>();
    JsArray<OrionKeyBindingsRelationOverlay> keyBindings =
        OrionKeyModeOverlay.getKeyBindings_(orionTextViewOverlay);
    for (int i = 0; i < keyBindings.length(); i++) {
      OrionKeyBindingsRelationOverlay key = keyBindings.get(i);

      String actionId = key.getActionId();
      String actionDescription = orionTextViewOverlay.getActionDescription(actionId);
      String hotKey = UiUtilsOverlay.getUserKeyString(uiUtilsOverlay, key.getKeyBindings());

      if (actionDescription != null) {
        hotKeyItems.add(new HotKeyItem(actionDescription, hotKey));
      }
    }
    return hotKeyItems;
  }

  @Override
  public void hideTooltip() {
    getEditor().hideTooltip();
  }

  @Override
  public MarkerRegistration addMarker(final TextRange range, final String className) {
    final OrionAnnotationOverlay annotation = OrionAnnotationOverlay.create();

    OrionStyleOverlay styleOverlay = OrionStyleOverlay.create();
    styleOverlay.setStyleClass(className);

    int start = embeddedDocument.getIndexFromPosition(range.getFrom());
    int end = embeddedDocument.getIndexFromPosition(range.getTo());

    annotation.setStart(start);
    annotation.setEnd(end);
    annotation.setRangeStyle(styleOverlay);
    annotation.setType("che-marker");

    editorOverlay.getAnnotationModel().addAnnotation(annotation);
    return new MarkerRegistration() {
      @Override
      public void clearMark() {
        editorOverlay.getAnnotationModel().removeAnnotation(annotation);
      }
    };
  }

  @Override
  public void showCompletionsProposals(final List<CompletionProposal> proposals) {
    if (proposals == null || proposals.isEmpty()) {
      /** Hide autocompletion when it's visible and it is nothing to propose */
      if (assistWidget.isVisible()) {
        assistWidget.hide();
      }

      return;
    }

    assistWidget.show(proposals);
  }

  @Override
  public void showCompletionProposals(final CompletionsSource completionsSource) {
    completionsSource.computeCompletions(
        new CompletionReadyCallback() {
          @Override
          public void onCompletionReady(List<CompletionProposal> proposals) {
            showCompletionsProposals(proposals);
          }
        });
  }

  @Override
  public LineStyler getLineStyler() {
    return lineStyler;
  }

  @Override
  public HandlerRegistration addGutterClickHandler(final GutterClickHandler handler) {
    if (!gutterClickHandlerAdded) {
      gutterClickHandlerAdded = true;
      orionLineNumberRuler.addEventListener(
          OrionEventConstants.RULER_CLICK_EVENT,
          new OrionExtRulerOverlay.EventHandler<OrionRulerClickEventOverlay>() {
            @Override
            public void onEvent(OrionRulerClickEventOverlay parameter) {
              final int lineIndex = parameter.getLineIndex();
              fireGutterClickEvent(lineIndex);
            }
          },
          false);
    }
    return addHandler(handler, GutterClickEvent.TYPE);
  }

  private void fireGutterClickEvent(final int line) {
    final GutterClickEvent gutterEvent =
        new GutterClickEvent(line, Gutters.BREAKPOINTS_GUTTER, null);
    fireEvent(gutterEvent);
    this.embeddedDocument.getDocEventBus().fireEvent(gutterEvent);
  }

  @Override
  public void showCompletionProposals() {
    editorOverlay.getContentAssist().activate();
  }

  @Override
  public void showCompletionProposals(
      final CompletionsSource completionsSource,
      final AdditionalInfoCallback additionalInfoCallback) {
    showCompletionProposals(completionsSource);
  }

  @Override
  public void refresh() {
    this.editorOverlay.getTextView().redraw();
  }

  @Override
  public boolean isCompletionProposalsShowing() {
    return assistWidget.isVisible();
  }

  public void scrollToLine(int line) {
    this.editorOverlay.getTextView().setTopIndex(line);
  }

  public void showErrors(AnnotationModelEvent event) {
    AnnotationModel annotationModel = event.getAnnotationModel();
    OrionAnnotationSeverityProvider severityProvider = null;
    if (annotationModel instanceof OrionAnnotationSeverityProvider) {
      severityProvider = (OrionAnnotationSeverityProvider) annotationModel;
    }

    for (OrionAnnotationOverlay annotationOverlay : problems) {
      editorOverlay.getAnnotationModel().removeAnnotation(annotationOverlay);
    }

    Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
    while (annotationIterator.hasNext()) {
      Annotation annotation = annotationIterator.next();
      OrionAnnotationOverlay problem =
          getOrionAnnotationOverlay(annotationModel, severityProvider, annotation);
      editorOverlay.getAnnotationModel().addAnnotation(problem);
      problems.add(problem);
    }
  }

  private OrionAnnotationOverlay getOrionAnnotationOverlay(
      AnnotationModel annotationModel,
      OrionAnnotationSeverityProvider severityProvider,
      Annotation annotation) {
    Position position = annotationModel.getPosition(annotation);

    return annotationType.createAnnotation(
        getSeverity(annotation.getType(), severityProvider),
        position.getOffset(),
        position.getOffset() + position.getLength(),
        annotation.getText());
  }

  private String getSeverity(String type, OrionAnnotationSeverityProvider provider) {
    if (provider != null) {
      return provider.getSeverity(type);
    } else {
      return "orion.annotation.error";
    }
  }

  public void clearErrors() {
    OrionAnnotationModelOverlay annotationModelOverlay = editorOverlay.getAnnotationModel();
    problems.forEach(annotationModelOverlay::removeAnnotation);
    problems.clear();
  }

  public OrionTextViewOverlay getTextView() {
    return editorOverlay.getTextView();
  }

  public LinkedMode getLinkedMode() {
    return editorOverlay.getLinkedMode(editorOverlay.getAnnotationModel());
  }

  public void showCompletionInformation() {
    if (assistWidget.isVisible()) {
      assistWidget.showCompletionInfo();
    }
  }

  public OrionAnnotationModelOverlay getAnnotationModel() {
    return editorOverlay.getAnnotationModel();
  }

  /** Returns {@link OrionEditorOverlay}. */
  public OrionEditorOverlay getEditor() {
    return editorOverlay;
  }

  @Override
  public Gutter getGutter() {
    return gutter;
  }

  public int getTopVisibleLine() {
    return editorOverlay.getTextView().getTopIndex();
  }

  public void setTopLine(int line) {
    editorOverlay.getTextView().setTopIndex(line);
  }

  public void destroy() {
    editorOverlay.getTextView().destroy();
  }

  /**
   * UI binder interface for this component.
   *
   * @author "Mickaël Leduque"
   */
  interface OrionEditorWidgetUiBinder extends UiBinder<SimplePanel, OrionEditorWidget> {}

  /**
   * CSS style for the orion native editor element.
   *
   * @author "Mickaël Leduque"
   */
  public interface EditorElementStyle extends CssResource {

    @ClassName("editor-parent")
    String editorParent();
  }

  private class EditorViewCreatedOperation implements Operation<OrionEditorViewOverlay> {
    private final WidgetInitializedCallback widgetInitializedCallback;

    private EditorViewCreatedOperation(WidgetInitializedCallback widgetInitializedCallback) {
      this.widgetInitializedCallback = widgetInitializedCallback;
    }

    @Override
    public void apply(OrionEditorViewOverlay arg) throws OperationException {
      editorViewOverlay = arg;
      editorOverlay = arg.getEditor();
      orionSettingsController.setEditorViewOverlay(arg);

      final OrionContentAssistOverlay contentAssist = editorOverlay.getContentAssist();
      eventBus.addHandler(
          SelectionChangedEvent.TYPE,
          new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
              if (contentAssist.isActive()) {
                contentAssist.deactivate();
              }
            }
          });

      lineStyler = new OrionLineStyler(editorOverlay);

      final OrionTextViewOverlay textView = editorOverlay.getTextView();
      keyModeInstances.add(
          VI, OrionKeyModeOverlay.getViKeyMode(moduleHolder.getModule("OrionVi"), textView));
      keyModeInstances.add(
          EMACS,
          OrionKeyModeOverlay.getEmacsKeyMode(moduleHolder.getModule("OrionEmacs"), textView));

      setupKeymode();
      eventBus.addHandler(
          KeymapChangeEvent.TYPE,
          new KeymapChangeHandler() {

            @Override
            public void onKeymapChanged(final KeymapChangeEvent event) {
              setupKeymode();
            }
          });
      undoRedo = new OrionUndoRedo(editorOverlay.getUndoStack());
      editorOverlay.setZoomRulerVisible(true);
      editorOverlay.getAnnotationStyler().addAnnotationType("che-marker", 100);
      cheContentAssistMode =
          OrionKeyModeOverlay.getCheCodeAssistMode(
              moduleHolder.getModule("CheContentAssistMode"), editorOverlay.getTextView());
      assistWidget =
          contentAssistWidgetFactory.create(OrionEditorWidget.this, cheContentAssistMode);
      gutter = initBreakpointRuler(moduleHolder);

      widgetInitializedCallback.initialized(OrionEditorWidget.this);
    }
  }

  /**
   * Registers global prompt function to be accessible directly from JavaScript.
   *
   * <p>Function promptIDE(title, text, defaultValue, callback) title Dialog title text The text to
   * display in the dialog box defaultValue The default value callback function(value) clicking "OK"
   * will return input value clicking "Cancel" will return null
   */
  private native void registerPromptFunction() /*-{
        if (!$wnd["promptIDE"]) {
            var instance = this;
            $wnd["promptIDE"] = function (title, text, defaultValue, callback) {
                instance.@org.eclipse.che.ide.editor.orion.client.OrionEditorWidget::askLineNumber(*)(title, text, defaultValue, callback);
            };
        }
    }-*/;

  /** Custom callback to pass given value to native javascript function. */
  private class InputCallback implements org.eclipse.che.ide.ui.dialogs.input.InputCallback {

    private JavaScriptObject callback;

    public InputCallback(JavaScriptObject callback) {
      this.callback = callback;
    }

    @Override
    public void accepted(String value) {
      acceptedNative(value);
      editorAgent.activateEditor(editorAgent.getActiveEditor());
    }

    private native void acceptedNative(String value) /*-{
            var callback = this.@org.eclipse.che.ide.editor.orion.client.OrionEditorWidget.InputCallback::callback;
            callback(value);
        }-*/;
  }

  private void askLineNumber(
      String title, String text, String defaultValue, final JavaScriptObject callback) {
    if (defaultValue == null) {
      defaultValue = "";
    } else {
      // It's strange situation defaultValue.length() returns 'undefined' but must return a number.
      // Reinitialise the variable resolves the problem.
      defaultValue = "" + defaultValue;
    }

    dialogFactory
        .createInputDialog(
            title, text, defaultValue, 0, defaultValue.length(), new InputCallback(callback), null)
        .show();
  }
}
