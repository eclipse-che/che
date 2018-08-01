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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import static org.eclipse.che.plugin.nodejsdbg.ide.NodeJsDebugger.ConnectionProperties.SCRIPT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.Map;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class NodeJsDebuggerConfigurationPagePresenterTest {

  private static final String HOST = "localhost";
  private static final int PORT = 8000;

  @Mock private NodeJsDebuggerConfigurationPageView pageView;
  @Mock private DebugConfiguration configuration;
  @Mock private CurrentProjectPathMacro currentProjectPathMacro;
  @Mock private AppContext appContext;

  @InjectMocks private NodeJsDebuggerConfigurationPagePresenter pagePresenter;

  @Before
  public void setUp() {
    pagePresenter.resetFrom(configuration);
  }

  @Test
  public void testResetting() throws Exception {
    verify(configuration, atLeastOnce()).getConnectionProperties();
    verify(currentProjectPathMacro).getName();
  }

  @Test
  public void testGo() throws Exception {
    AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

    pagePresenter.go(container);

    verify(container).setWidget(eq(pageView));
    verify(configuration, atLeastOnce()).getConnectionProperties();
    verify(pageView).setScriptPath(nullable(String.class));
  }

  @Test
  public void testOnBinaryPathChanged() throws Exception {
    String binPath = "/path";
    when(pageView.getScriptPath()).thenReturn(binPath);

    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);

    pagePresenter.onScriptPathChanged();

    verify(pageView).getScriptPath();
    ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);

    verify(configuration).setConnectionProperties(argumentCaptor.capture());
    Map argumentCaptorValue = argumentCaptor.getValue();
    assertEquals(binPath, argumentCaptorValue.get(SCRIPT.toString()));

    verify(listener).onDirtyStateChanged();
  }
}
