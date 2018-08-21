/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.ide.configuration;

import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE;
import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP;
import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.ATTR_DEBUG_PORT;
import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION;
import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.DEFAULT_BREAK_AT_FIRST_LINE;
import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.DEFAULT_CLIENT_HOST_IP;
import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.DEFAULT_DEBUG_PORT;
import static org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType.DEFAULT_USE_SSL_ENCRYPTION;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Zend dbg configuration page presenter tests.
 *
 * @author Bartlomiej Laczkowski
 */
@RunWith(MockitoJUnitRunner.class)
public class ZendDbgConfigurationPagePresenterTest {

  private static final Map<String, String> CONNECTION_PROPERTIES = new HashMap<>();

  static {
    CONNECTION_PROPERTIES.put(ATTR_CLIENT_HOST_IP, DEFAULT_CLIENT_HOST_IP);
    CONNECTION_PROPERTIES.put(ATTR_DEBUG_PORT, DEFAULT_DEBUG_PORT);
    CONNECTION_PROPERTIES.put(ATTR_BREAK_AT_FIRST_LINE, DEFAULT_BREAK_AT_FIRST_LINE);
    CONNECTION_PROPERTIES.put(ATTR_USE_SSL_ENCRYPTION, DEFAULT_USE_SSL_ENCRYPTION);
  }

  @Mock private ZendDbgConfigurationPageView pageView;
  @Mock private AppContext appContext;
  @Mock private DebugConfiguration configuration;

  @InjectMocks private ZendDbgConfigurationPagePresenter pagePresenter;

  @Before
  public void setUp() {
    when(configuration.getConnectionProperties()).thenReturn(CONNECTION_PROPERTIES);
    pagePresenter.resetFrom(configuration);
  }

  @Test
  public void testResetting() throws Exception {
    verify(configuration, times(2)).getConnectionProperties();
  }

  @Test
  public void testGo() throws Exception {
    AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);
    pagePresenter.go(container);
    verify(container).setWidget(eq(pageView));
    verify(configuration, times(3)).getConnectionProperties();
  }

  @Test
  public void testOnClientHostIPChanged() throws Exception {
    String clientHostIP = "127.0.0.1";
    when(pageView.getClientHostIP()).thenReturn(clientHostIP);
    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);
    pagePresenter.onClientHostIPChanged();
    verify(pageView).getClientHostIP();
    verify(configuration, times(3)).getConnectionProperties();
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnDebugPortChanged() throws Exception {
    int debugPort = 10000;
    when(pageView.getDebugPort()).thenReturn(debugPort);
    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);
    pagePresenter.onDebugPortChanged();
    verify(pageView).getDebugPort();
    verify(configuration, times(3)).getConnectionProperties();
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnBreakAtFirstLineChanged() throws Exception {
    boolean breakAtFirstLine = false;
    when(pageView.getBreakAtFirstLine()).thenReturn(breakAtFirstLine);
    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);
    pagePresenter.onBreakAtFirstLineChanged(breakAtFirstLine);
    verify(pageView).getBreakAtFirstLine();
    verify(configuration, times(3)).getConnectionProperties();
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnUseSslEncryptionChanged() throws Exception {
    boolean useSslEncryption = false;
    when(pageView.getUseSslEncryption()).thenReturn(useSslEncryption);
    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);
    pagePresenter.onUseSslEncryptionChanged(useSslEncryption);
    verify(pageView).getUseSslEncryption();
    verify(configuration, times(3)).getConnectionProperties();
    verify(listener).onDirtyStateChanged();
  }
}
