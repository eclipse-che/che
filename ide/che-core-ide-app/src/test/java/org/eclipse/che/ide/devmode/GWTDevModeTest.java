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

import static org.eclipse.che.ide.devmode.GWTDevMode.IDE_GWT_APP_SHORT_NAME;
import static org.eclipse.che.ide.devmode.GWTDevMode.INT_CODE_SERVER_REF;
import static org.eclipse.che.ide.devmode.GWTDevMode.LOCAL_CODE_SERVER_ADDRESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for the {@link GWTDevMode}. */
@RunWith(MockitoJUnitRunner.class)
public class GWTDevModeTest {

  static final String INT_CODE_SERVER_URL = "http://172.19.20.28:12345/";

  @Mock WsAgentServerUtil wsAgentServerUtil;
  @Mock DevModeScriptInjector devModeScriptInjector;
  @Mock BookmarkletParams bookmarkletParams;
  @Mock DialogFactory dialogFactory;
  @Mock CoreLocalizationConstant messages;

  @Mock Promise<Void> voidPromise;
  @Mock PromiseError promiseError;
  @Captor ArgumentCaptor<Operation<PromiseError>> promiseErrorOperationCapture;

  @Mock MachineImpl wsAgentMachine;
  @Mock ServerImpl codeServer;

  @InjectMocks GWTDevMode devMode;

  @Test
  public void shouldSetUpDevModeForInternalCodeServer() throws Exception {
    mockInternalCodeServer();

    when(devModeScriptInjector.inject(anyString())).thenReturn(voidPromise);
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);

    devMode.setUp();

    verify(bookmarkletParams).setParams(INT_CODE_SERVER_URL, IDE_GWT_APP_SHORT_NAME);
    verify(devModeScriptInjector).inject(INT_CODE_SERVER_URL);
  }

  @Test
  public void shouldSetUpDevModeForLocalCodeServerIfNoInternalOne() throws Exception {
    when(devModeScriptInjector.inject(anyString())).thenReturn(voidPromise);
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);

    devMode.setUp();

    verify(bookmarkletParams).setParams(LOCAL_CODE_SERVER_ADDRESS, IDE_GWT_APP_SHORT_NAME);
    verify(devModeScriptInjector).inject(LOCAL_CODE_SERVER_ADDRESS);
  }

  @Test
  public void shouldSetUpDevModeForLocalCodeServerIfFailedForInternalOne() throws Exception {
    mockInternalCodeServer();

    when(devModeScriptInjector.inject(anyString())).thenReturn(voidPromise);
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);

    devMode.setUp();

    verify(bookmarkletParams, never()).setParams(LOCAL_CODE_SERVER_ADDRESS, IDE_GWT_APP_SHORT_NAME);
    verify(devModeScriptInjector, never()).inject(LOCAL_CODE_SERVER_ADDRESS);

    verify(voidPromise).catchError(promiseErrorOperationCapture.capture());
    promiseErrorOperationCapture.getValue().apply(promiseError);

    verify(bookmarkletParams).setParams(INT_CODE_SERVER_URL, IDE_GWT_APP_SHORT_NAME);
    verify(devModeScriptInjector).inject(INT_CODE_SERVER_URL);
  }

  private void mockInternalCodeServer() {
    when(codeServer.getUrl()).thenReturn(INT_CODE_SERVER_URL);
    when(wsAgentMachine.getServerByName(INT_CODE_SERVER_REF)).thenReturn(Optional.of(codeServer));
    when(wsAgentServerUtil.getWsAgentServerMachine()).thenReturn(Optional.of(wsAgentMachine));
  }
}
