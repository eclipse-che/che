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
package org.eclipse.che.plugin.jdb.ide.configuration;

import static java.lang.Boolean.TRUE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Artem Zatsarynnyi */
@Listeners(MockitoTestNGListener.class)
public class JavaDebugConfigurationPagePresenterTest {

  private static final String HOST = "localhost";
  private static final int PORT = 8000;

  @Mock private JavaDebugConfigurationPageView pageView;
  @Mock private WsAgentServerUtil wsAgentServerUtil;
  @Mock private MachineImpl devMachine;

  @Mock private DebugConfiguration configuration;

  @InjectMocks private JavaDebugConfigurationPagePresenter pagePresenter;

  @BeforeMethod
  public void setUp() {
    when(configuration.getHost()).thenReturn(HOST);
    when(configuration.getPort()).thenReturn(PORT);

    ServerImpl server = mock(ServerImpl.class);
    when(server.getUrl()).thenReturn("http://preview.com");
    Map<String, ServerImpl> servers = new HashMap<>();
    servers.put("8000/tcp", server);
    when(devMachine.getServers()).thenReturn(servers);

    when(wsAgentServerUtil.getWsAgentServerMachine()).thenReturn(Optional.of(devMachine));
    when(devMachine.getName()).thenReturn("devMachine");

    pagePresenter.resetFrom(configuration);
  }

  @Test
  public void testResetting() throws Exception {
    verify(configuration).getHost();
    verify(configuration).getPort();
  }

  @Test
  public void testGo() throws Exception {
    AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

    pagePresenter.go(container);

    verify(container).setWidget(eq(pageView));
    verify(configuration, times(2)).getHost();
    verify(configuration, times(2)).getPort();
    verify(pageView).setHost(eq(HOST));
    verify(pageView).setPort(eq(PORT));
    verify(pageView).setDevHost(eq(TRUE));
  }

  @Test
  public void testOnHostChanged() throws Exception {
    String host = "localhost";
    when(pageView.getHost()).thenReturn(host);

    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);

    pagePresenter.onHostChanged();

    verify(pageView).getHost();
    verify(configuration).setHost(eq(host));
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnPortChanged() throws Exception {
    int port = 8000;
    when(pageView.getPort()).thenReturn(port);

    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);

    pagePresenter.onPortChanged();

    verify(pageView).getPort();
    verify(configuration).setPort(eq(port));
    verify(listener).onDirtyStateChanged();
  }
}
