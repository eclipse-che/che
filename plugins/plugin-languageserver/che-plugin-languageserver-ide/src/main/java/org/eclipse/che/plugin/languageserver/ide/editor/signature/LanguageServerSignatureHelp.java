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
package org.eclipse.che.plugin.languageserver.ide.editor.signature;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangedHandler;
import org.eclipse.che.ide.api.editor.signature.SignatureHelp;
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;
import org.eclipse.che.ide.api.editor.texteditor.HandlesTextOperations;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorOperations;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * LS implementation for Signature help feature
 *
 * @author Evgen Vidolob
 */
public class LanguageServerSignatureHelp implements SignatureHelpProvider {

  private final TextDocumentServiceClient client;
  private final DtoBuildHelper helper;
  private final NotificationManager notificationManager;
  private final ServerCapabilities capabilities;
  private HandlerRegistration handlerRegistration;

  @Inject
  public LanguageServerSignatureHelp(
      TextDocumentServiceClient client,
      DtoBuildHelper helper,
      NotificationManager notificationManager,
      @Assisted ServerCapabilities capabilities) {
    this.client = client;
    this.helper = helper;
    this.notificationManager = notificationManager;
    this.capabilities = capabilities;
  }

  @Override
  public Promise<Optional<SignatureHelp>> signatureHelp(Document document, int offset) {
    TextDocumentPositionParams paramsDTO = helper.createTDPP(document, offset + 1);
    Promise<org.eclipse.lsp4j.SignatureHelp> promise = client.signatureHelp(paramsDTO);
    return promise
        .then(
            new Function<org.eclipse.lsp4j.SignatureHelp, Optional<SignatureHelp>>() {
              @Override
              public Optional<SignatureHelp> apply(org.eclipse.lsp4j.SignatureHelp arg)
                  throws FunctionException {
                if (arg == null) {
                  return Optional.absent();
                }

                return Optional.<SignatureHelp>of(new SignatureHelpImpl(arg));
              }
            })
        .catchError(
            new Function<PromiseError, Optional<SignatureHelp>>() {
              @Override
              public Optional<SignatureHelp> apply(PromiseError arg) throws FunctionException {
                notificationManager.notify(
                    arg.getMessage(),
                    StatusNotification.Status.FAIL,
                    StatusNotification.DisplayMode.EMERGE_MODE);
                return Optional.absent();
              }
            });
  }

  @Override
  public void install(final TextEditor editor) {
    if (capabilities.getSignatureHelpProvider() != null
        && capabilities.getSignatureHelpProvider().getTriggerCharacters() != null) {
      final List<String> triggerCharacters =
          capabilities.getSignatureHelpProvider().getTriggerCharacters();
      handlerRegistration =
          editor
              .getDocument()
              .getDocumentHandle()
              .getDocEventBus()
              .addHandler(
                  DocumentChangedEvent.TYPE,
                  new DocumentChangedHandler() {
                    @Override
                    public void onDocumentChanged(DocumentChangedEvent event) {
                      String eventText = event.getText();

                      if (isNullOrEmpty(eventText)) {
                        return;
                      }

                      String candidate = String.valueOf(eventText.charAt(0));
                      if (triggerCharacters.contains(candidate)) {
                        ((HandlesTextOperations) editor)
                            .doOperation(TextEditorOperations.SIGNATURE_HELP);
                      }
                    }
                  });
    }
  }

  @Override
  public void uninstall() {
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
    }
  }
}
