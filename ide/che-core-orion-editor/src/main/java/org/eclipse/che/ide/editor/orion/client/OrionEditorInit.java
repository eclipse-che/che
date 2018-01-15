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
package org.eclipse.che.ide.editor.orion.client;

import elemental.events.KeyboardEvent.KeyCode;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.annotation.HasAnnotationRendering;
import org.eclipse.che.ide.api.editor.annotation.QueryAnnotationsEvent;
import org.eclipse.che.ide.api.editor.autosave.AutoSaveMode;
import org.eclipse.che.ide.api.editor.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.api.editor.changeintercept.TextChange;
import org.eclipse.che.ide.api.editor.changeintercept.TextChangeInterceptor;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistant;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistantFactory;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.codeassist.CompletionReadyCallback;
import org.eclipse.che.ide.api.editor.codeassist.CompletionsSource;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.events.CompletionRequestEvent;
import org.eclipse.che.ide.api.editor.events.CompletionRequestHandler;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.TextChangeEvent;
import org.eclipse.che.ide.api.editor.events.TextChangeHandler;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.keymap.KeyBinding;
import org.eclipse.che.ide.api.editor.keymap.KeyBindingAction;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistAssistant;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TypedRegion;
import org.eclipse.che.ide.api.editor.texteditor.HasKeyBindings;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.util.browser.UserAgent;

/**
 * Initialization controller for the text editor. Sets-up (when available) the different components
 * that depend on the document being ready.
 */
public class OrionEditorInit {

  /** The logger. */
  private static final Logger LOG = Logger.getLogger(OrionEditorInit.class.getName());

  private static final String CONTENT_ASSIST = "Content assist";
  private static final String QUICK_FIX = "Quick fix";

  private final AutoSaveMode autoSaveMode;
  private final TextEditorConfiguration configuration;
  private final CodeAssistantFactory codeAssistantFactory;
  private final OrionEditorPresenter textEditor;
  private final QuickAssistAssistant quickAssist;

  /** The quick assist assistant. */
  public OrionEditorInit(
      final AutoSaveMode autoSaveMode,
      final TextEditorConfiguration configuration,
      final CodeAssistantFactory codeAssistantFactory,
      final QuickAssistAssistant quickAssist,
      final OrionEditorPresenter textEditor) {
    this.autoSaveMode = autoSaveMode;
    this.configuration = configuration;
    this.codeAssistantFactory = codeAssistantFactory;
    this.quickAssist = quickAssist;
    this.textEditor = textEditor;
  }

  /**
   * Initialize the text editor.
   *
   * @param document to initialise with
   */
  public void init(Document document) {
    DocumentHandle documentHandle = document.getDocumentHandle();
    configurePartitioner(documentHandle);
    configureReconciler(documentHandle);
    configureAnnotationModel(documentHandle);
    configureCodeAssist(documentHandle);
    configureChangeInterceptors(documentHandle);
    configureFormatter(textEditor);
    configureSignatureHelp(textEditor);
    addQuickAssistKeyBinding();
    configureAutoSaveMode(documentHandle);
  }

  public void uninstall() {
    Reconciler reconciler = configuration.getReconciler();
    if (reconciler != null) {
      reconciler.uninstall();
    }
    SignatureHelpProvider signatureHelpProvider = configuration.getSignatureHelpProvider();
    if (signatureHelpProvider != null) {
      signatureHelpProvider.uninstall();
    }
    autoSaveMode.uninstall();
  }

  private void configureAutoSaveMode(final DocumentHandle documentHandle) {
    autoSaveMode.install(textEditor);
    autoSaveMode.setDocumentHandle(documentHandle);
  }

  private void configureSignatureHelp(TextEditor textEditor) {
    SignatureHelpProvider signatureHelpProvider = configuration.getSignatureHelpProvider();
    if (signatureHelpProvider != null) {
      signatureHelpProvider.install(textEditor);
    }
  }

  private void configureFormatter(OrionEditorPresenter textEditor) {
    ContentFormatter formatter = configuration.getContentFormatter();
    if (formatter != null) {
      formatter.install(textEditor);
    }
  }

  /**
   * Configures the editor's DocumentPartitioner.
   *
   * @param documentHandle the handle to the document
   */
  private void configurePartitioner(final DocumentHandle documentHandle) {
    final DocumentPartitioner partitioner = configuration.getPartitioner();
    if (partitioner != null) {
      partitioner.setDocumentHandle(documentHandle);
      documentHandle.getDocEventBus().addHandler(DocumentChangedEvent.TYPE, partitioner);
      partitioner.initialize();
    }
  }

  /**
   * Configures the editor's Reconciler.
   *
   * @param documentHandle the handle to the document
   */
  private void configureReconciler(final DocumentHandle documentHandle) {
    final Reconciler reconciler = configuration.getReconciler();
    if (reconciler != null) {
      reconciler.setDocumentHandle(documentHandle);
      documentHandle.getDocEventBus().addHandler(DocumentChangedEvent.TYPE, reconciler);
      reconciler.install(textEditor);
    }
  }

  /**
   * Configures the editor's annotation model.
   *
   * @param documentHandle the handle on the editor
   */
  private void configureAnnotationModel(final DocumentHandle documentHandle) {
    final AnnotationModel annotationModel = configuration.getAnnotationModel();
    if (annotationModel == null) {
      return;
    }
    // add the renderers (event handler) before the model (event source)
    if (textEditor instanceof HasAnnotationRendering) {
      ((HasAnnotationRendering) textEditor).configure(annotationModel, documentHandle);
    }
    annotationModel.setDocumentHandle(documentHandle);
    documentHandle.getDocEventBus().addHandler(DocumentChangedEvent.TYPE, annotationModel);

    // the model listens to QueryAnnotation events
    documentHandle.getDocEventBus().addHandler(QueryAnnotationsEvent.TYPE, annotationModel);
  }

  /**
   * Configure the editor's code assistant.
   *
   * @param documentHandle the handle on the document
   */
  private void configureCodeAssist(final DocumentHandle documentHandle) {
    if (this.codeAssistantFactory == null) {
      return;
    }
    final Map<String, CodeAssistProcessor> processors =
        configuration.getContentAssistantProcessors();

    if (processors != null && !processors.isEmpty()) {
      LOG.info("Creating code assistant.");

      final CodeAssistant codeAssistant =
          this.codeAssistantFactory.create(this.textEditor, this.configuration.getPartitioner());
      for (String key : processors.keySet()) {
        codeAssistant.setCodeAssistantProcessor(key, processors.get(key));
      }

      final KeyBindingAction action =
          new KeyBindingAction() {
            @Override
            public boolean action() {
              showCompletion(codeAssistant, true);
              return true;
            }
          };
      final HasKeyBindings hasKeyBindings = this.textEditor.getHasKeybindings();
      hasKeyBindings.addKeyBinding(
          new KeyBinding(true, false, false, false, KeyCode.SPACE, action), CONTENT_ASSIST);

      // handle CompletionRequest events that come from text operations instead of simple key
      // binding
      documentHandle
          .getDocEventBus()
          .addHandler(
              CompletionRequestEvent.TYPE,
              new CompletionRequestHandler() {
                @Override
                public void onCompletionRequest(final CompletionRequestEvent event) {
                  showCompletion(codeAssistant, false);
                }
              });
    } else {
      final KeyBindingAction action =
          new KeyBindingAction() {
            @Override
            public boolean action() {
              showCompletion();
              return true;
            }
          };
      final HasKeyBindings hasKeyBindings = this.textEditor.getHasKeybindings();
      if (UserAgent.isMac()) {
        hasKeyBindings.addKeyBinding(
            new KeyBinding(false, false, false, true, KeyCode.SPACE, action), CONTENT_ASSIST);
        hasKeyBindings.addKeyBinding(
            new KeyBinding(false, false, true, true, KeyCode.SPACE, action), CONTENT_ASSIST);
      } else {
        hasKeyBindings.addKeyBinding(
            new KeyBinding(true, false, false, false, KeyCode.SPACE, action), CONTENT_ASSIST);
      }
      // handle CompletionRequest events that come from text operations instead of simple key
      // binding
      documentHandle
          .getDocEventBus()
          .addHandler(
              CompletionRequestEvent.TYPE,
              new CompletionRequestHandler() {
                @Override
                public void onCompletionRequest(final CompletionRequestEvent event) {
                  showCompletion();
                }
              });
    }
  }

  /**
   * Show the available completions.
   *
   * @param codeAssistant the code assistant
   * @param triggered if triggered by the content assist key binding
   */
  private void showCompletion(final CodeAssistant codeAssistant, final boolean triggered) {
    final int cursor = textEditor.getCursorOffset();
    if (cursor < 0) {
      return;
    }
    final CodeAssistProcessor processor = codeAssistant.getProcessor(cursor);
    if (processor != null) {
      this.textEditor.showCompletionProposals(
          new CompletionsSource() {
            @Override
            public void computeCompletions(final CompletionReadyCallback callback) {
              // cursor must be computed here again so it's original value is not baked in
              // the SMI instance closure - important for completion update when typing
              final int cursor = textEditor.getCursorOffset();
              codeAssistant.computeCompletionProposals(
                  cursor,
                  triggered,
                  new CodeAssistCallback() {
                    @Override
                    public void proposalComputed(final List<CompletionProposal> proposals) {
                      callback.onCompletionReady(proposals);
                    }
                  });
            }
          });
    } else {
      showCompletion();
    }
  }

  /** Show the available completions. */
  private void showCompletion() {
    this.textEditor.showCompletionProposals();
  }

  /** Add key binding to quick assist assistant. */
  private void addQuickAssistKeyBinding() {
    if (this.quickAssist != null) {
      final KeyBindingAction action =
          new KeyBindingAction() {
            @Override
            public boolean action() {
              final PositionConverter positionConverter = textEditor.getPositionConverter();
              if (positionConverter != null) {
                textEditor.showQuickAssist();
              }
              return true;
            }
          };
      final HasKeyBindings hasKeyBindings = this.textEditor.getHasKeybindings();
      hasKeyBindings.addKeyBinding(
          new KeyBinding(false, false, true, false, KeyCode.ENTER, action), QUICK_FIX);
    }
  }

  private void configureChangeInterceptors(final DocumentHandle documentHandle) {
    final ChangeInterceptorProvider interceptors = configuration.getChangeInterceptorProvider();
    if (interceptors != null) {
      documentHandle
          .getDocEventBus()
          .addHandler(
              TextChangeEvent.TYPE,
              new TextChangeHandler() {
                @Override
                public void onTextChange(final TextChangeEvent event) {
                  final TextChange change = event.getChange();
                  if (change == null) {
                    return;
                  }
                  final TextPosition from = change.getFrom();
                  if (from == null) {
                    return;
                  }
                  final int startOffset = documentHandle.getDocument().getIndexFromPosition(from);
                  final TypedRegion region =
                      configuration.getPartitioner().getPartition(startOffset);
                  if (region == null) {
                    return;
                  }
                  final List<TextChangeInterceptor> filteredInterceptors =
                      interceptors.getInterceptors(region.getType());
                  if (filteredInterceptors == null || filteredInterceptors.isEmpty()) {
                    return;
                  }
                  // don't apply the interceptors if the range end doesn't belong to the same
                  // partition
                  final TextPosition to = change.getTo();
                  if (to != null && !from.equals(to)) {
                    final int endOffset = documentHandle.getDocument().getIndexFromPosition(to);
                    if (endOffset < region.getOffset()
                        || endOffset > region.getOffset() + region.getLength()) {
                      return;
                    }
                  }
                  // stop as soon as one interceptors has modified the content
                  for (final TextChangeInterceptor interceptor : filteredInterceptors) {
                    final TextChange result =
                        interceptor.processChange(
                            change, documentHandle.getDocument().getReadOnlyDocument());
                    if (result != null) {
                      event.update(result);
                      break;
                    }
                  }
                }
              });
    }
  }
}
