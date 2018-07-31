/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.devmode;

import static com.google.common.base.Strings.isNullOrEmpty;

import elemental.client.Browser;
import elemental.html.Storage;
import elemental.html.Window;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialog;

/**
 * Helps to set-up Super DevMode for the current IDE GWT app.
 *
 * <p>It does not communicate with a GWT CodeServer directly in any way but relies on the {@code
 * dev_mode_on.js} script functionality from the {@code gwt-dev} library.
 */
@Singleton
class GWTDevMode {

  public static final String LOCAL_CODE_SERVER_ADDRESS = "http://127.0.0.1:9876/";
  public static final String INT_CODE_SERVER_REF = "GWT-CodeServer";
  public static final String IDE_GWT_APP_SHORT_NAME = "_app";

  private final WsAgentServerUtil wsAgentServerUtil;
  private final DevModeScriptInjector devModeScriptInjector;
  private final BookmarkletParams bookmarkletParams;
  private final DialogFactory dialogFactory;
  private final CoreLocalizationConstant messages;

  @Inject
  GWTDevMode(
      WsAgentServerUtil wsAgentServerUtil,
      DevModeScriptInjector devModeScriptInjector,
      BookmarkletParams bookmarkletParams,
      DialogFactory dialogFactory,
      CoreLocalizationConstant messages) {
    this.wsAgentServerUtil = wsAgentServerUtil;
    this.devModeScriptInjector = devModeScriptInjector;
    this.bookmarkletParams = bookmarkletParams;
    this.dialogFactory = dialogFactory;
    this.messages = messages;
  }

  /**
   * Sets-up Super DevMode for the current IDE GWT app. Tries to use Code Server launched in a
   * dev-machine or at a localhost depending on which one is launched.
   */
  void setUp() {
    String codeServerURL = getInternalCodeServerURL().orElse(LOCAL_CODE_SERVER_ADDRESS);

    setUpSuperDevModeWithUI(codeServerURL);
  }

  /** Turn off Super DevMode for the current IDE GWT app. */
  void off() {
    Window window = Browser.getWindow();
    Storage sessionStorage = window.getSessionStorage();

    for (int i = 0; i < sessionStorage.getLength(); i++) {
      String key = sessionStorage.key(i);

      if (key.equals("__gwtDevModeHook:" + IDE_GWT_APP_SHORT_NAME)) {
        sessionStorage.removeItem(key);
        break;
      }
    }

    window.getLocation().reload();
  }

  /**
   * Returns a top-level URL of the GWT Code Server which is declared in the machine that contains
   * the "wsagent" server.
   *
   * @return {@code Optional} with a top-level URL of the GWT Code Server or an empty {@code
   *     Optional} if none
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
          return codeServerUrl.endsWith("/")
              ? Optional.of(codeServerUrl)
              : Optional.of(codeServerUrl + '/');
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Tries to set-up Super DevMode for the current IDE GWT app and shows an appropriate message to
   * the user.
   */
  private void setUpSuperDevModeWithUI(String codeServerURL) {
    setUpSuperDevMode(codeServerURL)
        .then(showSuccessMessage(codeServerURL))
        .catchError(handleStartRecompilationError(codeServerURL));
  }

  /**
   * Tries to set-up Super DevMode for the current IDE GWT app.
   *
   * @param codeServerURL URL of the Code Server URL to use
   * @return promise that may be resolved if Super DevMode has been set up successfully or rejected
   *     in case of any error while setting up Super DevMode
   */
  private Promise<Void> setUpSuperDevMode(String codeServerURL) {
    bookmarkletParams.setParams(codeServerURL, IDE_GWT_APP_SHORT_NAME);

    return devModeScriptInjector.inject(codeServerURL);
  }

  private Operation<Void> showSuccessMessage(String codeServerURL) {
    boolean isLocalhost = codeServerURL.equals(LOCAL_CODE_SERVER_ADDRESS);

    String message =
        isLocalhost
            ? messages.gwtRecompileDialogRecompilingMessage("localhost")
            : messages.gwtRecompileDialogRecompilingMessage("dev-machine");

    MessageDialog dialog =
        dialogFactory.createMessageDialog(messages.gwtRecompileDialogTitle(), message, null);

    return aVoid -> dialog.show();
  }

  private Operation<PromiseError> handleStartRecompilationError(String codeServerURL) {
    boolean isLocalhost = codeServerURL.equals(LOCAL_CODE_SERVER_ADDRESS);

    return err -> {
      if (!isLocalhost) {
        setUpSuperDevModeWithUI(LOCAL_CODE_SERVER_ADDRESS);
      } else {
        dialogFactory
            .createMessageDialog(
                messages.gwtRecompileDialogTitle(),
                messages.gwtRecompileDialogNoServerMessage(),
                null)
            .show();
      }
    };
  }
}
