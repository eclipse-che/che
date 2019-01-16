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
package org.eclipse.che.plugin.languageserver.ide;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_ASSISTANT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyTextEditAction;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.declaration.FindDefinitionAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.references.FindReferencesAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.GoToSymbolAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.workspace.FindSymbolAction;
import org.eclipse.che.plugin.languageserver.ide.registry.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.ide.rename.LSRenameAction;
import org.eclipse.che.plugin.languageserver.ide.service.ExecuteClientCommandReceiver;
import org.eclipse.che.plugin.languageserver.ide.service.PublishDiagnosticsReceiver;
import org.eclipse.che.plugin.languageserver.ide.service.ShowMessageJsonRpcReceiver;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;

@Extension(title = "LanguageServer")
@Singleton
public class LanguageServerExtension {
  private final String GROUP_ASSISTANT_REFACTORING = "assistantRefactoringGroup";

  @Inject
  public LanguageServerExtension(
      LanguageRegexesInitializer languageRegexesInitializer,
      LanguageDescriptionInitializer languageDescriptionInitializer,
      DefaultHoverProviderInitializer defaultHoverProviderInitializer,
      DefaultOccurrencesProviderInitializer defaultOccurrencesProviderInitializer,
      EventBus eventBus,
      AppContext appContext,
      ShowMessageJsonRpcReceiver showMessageJsonRpcReceiver,
      PublishDiagnosticsReceiver publishDiagnosticsReceiver,
      ExecuteClientCommandReceiver executeClientCommandReceiver) {
    eventBus.addHandler(
        WsAgentServerRunningEvent.TYPE,
        e -> {
          languageRegexesInitializer.initialize();
          languageDescriptionInitializer.initialize();
          defaultOccurrencesProviderInitializer.initialize();
          defaultHoverProviderInitializer.initialize();
          showMessageJsonRpcReceiver.subscribe();
          publishDiagnosticsReceiver.subscribe();
          executeClientCommandReceiver.subscribe();
        });

    if (appContext.getWorkspace().getStatus() == RUNNING) {
      languageRegexesInitializer.initialize();
      languageDescriptionInitializer.initialize();
      defaultOccurrencesProviderInitializer.initialize();
      defaultHoverProviderInitializer.initialize();
      showMessageJsonRpcReceiver.subscribe();
      publishDiagnosticsReceiver.subscribe();
      executeClientCommandReceiver.subscribe();
    }
  }

  @Inject
  protected void injectCss(LanguageServerResources resources) {
    // we need to call this method one time
    resources.css().ensureInjected();
    resources.quickOpenListCss().ensureInjected();
  }

  @Inject
  protected void registerAction(
      ActionManager actionManager,
      KeyBindingAgent keyBindingManager,
      GoToSymbolAction goToSymbolAction,
      FindSymbolAction findSymbolAction,
      FindDefinitionAction findDefinitionAction,
      FindReferencesAction findReferencesAction,
      ApplyTextEditAction applyTextEditAction,
      ApplyWorkspaceEditAction applyWorkspaceEditAction,
      LSRenameAction renameAction) {
    actionManager.registerAction("LSGoToSymbolAction", goToSymbolAction);
    actionManager.registerAction("LSFindSymbolAction", findSymbolAction);
    actionManager.registerAction("LSFindDefinitionAction", findDefinitionAction);
    actionManager.registerAction("LSFindReferencesAction", findReferencesAction);
    actionManager.registerAction("lsp.applyTextEdit", applyTextEditAction);
    actionManager.registerAction("lsp.applyWorkspaceEdit", applyWorkspaceEditAction);
    actionManager.registerAction("LS.rename", renameAction);

    DefaultActionGroup assistantGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_ASSISTANT);
    assistantGroup.add(
        goToSymbolAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(
        findSymbolAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(
        findDefinitionAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
    assistantGroup.add(
        findReferencesAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));

    DefaultActionGroup refactoringGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_ASSISTANT_REFACTORING);
    if (refactoringGroup == null) {
      refactoringGroup = new DefaultActionGroup("Refactoring", true, actionManager);
      actionManager.registerAction(GROUP_ASSISTANT_REFACTORING, refactoringGroup);
    }

    refactoringGroup.add(renameAction, new Constraints(Anchor.AFTER, "javaRenameRefactoring"));

    if (UserAgent.isMac()) {
      keyBindingManager
          .getGlobal()
          .addKey(
              new KeyBuilder().control().charCode(KeyCodeMap.F12).build(), "LSGoToSymbolAction");
    } else {
      keyBindingManager
          .getGlobal()
          .addKey(new KeyBuilder().action().charCode(KeyCodeMap.F12).build(), "LSGoToSymbolAction");
    }
    keyBindingManager
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode('n').build(), "LSFindSymbolAction");
    keyBindingManager
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F7).build(), "LSFindReferencesAction");
    keyBindingManager
        .getGlobal()
        .addKey(new KeyBuilder().charCode(KeyCodeMap.F4).build(), "LSFindDefinitionAction");

    keyBindingManager
        .getGlobal()
        .addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F6).build(), "LS.rename");

    DefaultActionGroup editorContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction("editorContextMenu");
    editorContextMenuGroup.addSeparator();
    editorContextMenuGroup.add(findDefinitionAction);
    editorContextMenuGroup.add(refactoringGroup);
  }

  @Inject
  protected void registerFileEventHandler(
      final EventBus eventBus,
      final TextDocumentServiceClient serviceClient,
      final DtoFactory dtoFactory,
      final LanguageServerRegistry lsRegistry) {
    eventBus.addHandler(
        FileEvent.TYPE,
        event -> {
          Path location = event.getFile().getLocation();
          if (lsRegistry.getLanguageFilter(event.getFile()) == null) {
            return;
          }
          final TextDocumentIdentifier documentId =
              dtoFactory.createDto(TextDocumentIdentifier.class);
          documentId.setUri(location.toString());
          switch (event.getOperationType()) {
            case OPEN:
              onOpen(event, dtoFactory, serviceClient, lsRegistry);
              break;
            case CLOSE:
              onClose(documentId, dtoFactory, serviceClient);
              break;
            case SAVE:
              onSave(documentId, dtoFactory, serviceClient);
              break;
          }
        });
  }

  private void onSave(
      TextDocumentIdentifier documentId,
      DtoFactory dtoFactory,
      TextDocumentServiceClient serviceClient) {
    DidSaveTextDocumentParams saveEvent = dtoFactory.createDto(DidSaveTextDocumentParams.class);
    saveEvent.setTextDocument(documentId);
    serviceClient.didSave(saveEvent);
  }

  private void onClose(
      TextDocumentIdentifier documentId,
      DtoFactory dtoFactory,
      TextDocumentServiceClient serviceClient) {
    DidCloseTextDocumentParams closeEvent = dtoFactory.createDto(DidCloseTextDocumentParams.class);
    closeEvent.setTextDocument(documentId);
    serviceClient.didClose(closeEvent);
  }

  private void onOpen(
      final FileEvent event,
      final DtoFactory dtoFactory,
      final TextDocumentServiceClient serviceClient,
      final LanguageServerRegistry lsRegistry) {
    event
        .getFile()
        .getContent()
        .then(
            text -> {
              TextDocumentItem documentItem = dtoFactory.createDto(TextDocumentItem.class);
              documentItem.setUri(event.getFile().getLocation().toString());
              documentItem.setVersion(LanguageServerEditorConfiguration.INITIAL_DOCUMENT_VERSION);
              documentItem.setText(text);
              documentItem.setLanguageId(
                  lsRegistry.getLanguageFilter(event.getFile()).getLanguageId());

              DidOpenTextDocumentParams openEvent =
                  dtoFactory.createDto(DidOpenTextDocumentParams.class);
              openEvent.setTextDocument(documentItem);
              openEvent.getTextDocument().setUri(event.getFile().getLocation().toString());
              openEvent.setText(text);

              serviceClient.didOpen(openEvent);
            });
  }
}
