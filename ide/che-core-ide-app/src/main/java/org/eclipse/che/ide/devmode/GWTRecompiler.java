/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.devmode;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/** relies on dev_mode_on.js functionality */
@Singleton
public class GWTRecompiler {

  private static final String LOCAL_CODE_SERVER_ADDRESS = "http://127.0.0.1:9876/";
  private static final String INT_CODE_SERVER_REF = "GWT-CodeServer";

  private final WsAgentServerUtil wsAgentServerUtil;
  private final DialogFactory dialogFactory;
  private final CoreLocalizationConstant messages;

  @Inject
  public GWTRecompiler(
      WsAgentServerUtil wsAgentServerUtil,
      DialogFactory dialogFactory,
      CoreLocalizationConstant messages) {
    this.wsAgentServerUtil = wsAgentServerUtil;
    this.dialogFactory = dialogFactory;
    this.messages = messages;
  }

  /** Tries to set up IDE GWT app to work in Super DevMode. */
  private Promise<Void> setUpSuperDevMode(String codeServerURL) {
    BookmarkletParamsHelper.setParams(codeServerURL);

    return DevModeScriptInjector.injectScript(codeServerURL);
  }

  /** Invokes IDE GWT app recompilation at the Code Server. */
  void recompile() {
    String codeServerURL = getInternalCodeServerURL().orElse(LOCAL_CODE_SERVER_ADDRESS);

    recompileWithMessages(codeServerURL);
  }

  /** Invokes IDE GWT app recompilation at the specified Code Server. */
  private void recompileWithMessages(String codeServerURL) {
    boolean isLocalhost = codeServerURL.equals(LOCAL_CODE_SERVER_ADDRESS);

    String successMessage =
        isLocalhost
            ? messages.gwtRecompileDialogRecompilingMessage("localhost")
            : messages.gwtRecompileDialogRecompilingMessage("dev-machine");

    setUpSuperDevMode(codeServerURL)
        .then(
            aVoid -> {
              dialogFactory
                  .createMessageDialog(messages.gwtRecompileDialogTitle(), successMessage, null)
                  .show();
            })
        .catchError(
            err -> {
              if (!isLocalhost) {
                recompileWithMessages(LOCAL_CODE_SERVER_ADDRESS);
              } else {
                dialogFactory
                    .createMessageDialog(
                        messages.gwtRecompileDialogTitle(),
                        messages.gwtRecompileDialogNoServerMessage(),
                        null)
                    .show();
              }
            });
  }

  /**
   * Returns the top-level URL of the GWT Code Server which is declared in the machine that contains
   * the "wsagent" server.
   */
  private Optional<String> getInternalCodeServerURL() {
    Optional<MachineImpl> wsAgentServerMachineOpt = wsAgentServerUtil.getWsAgentServerMachine();

    if (wsAgentServerMachineOpt.isPresent()) {
      MachineImpl wsAgentServerMachine = wsAgentServerMachineOpt.get();
      Optional<ServerImpl> codeServerOpt =
          wsAgentServerMachine.getServerByName(INT_CODE_SERVER_REF);

      if (codeServerOpt.isPresent()) {
        ServerImpl codeServer = codeServerOpt.get();
        String codeServerUrl = codeServer.getUrl();

        if (!isNullOrEmpty(codeServerUrl)) {
          return Optional.of(codeServerUrl + '/');
        }
      }
    }

    return Optional.empty();
  }
}
