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
package org.eclipse.che.ide.ext.gwt.client.command;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class GwtPagePresenterTest {

  private static final String WORK_DIR = "project";
  private static final String GWT_MODULE = "org.eclipse.CHE";
  private static final String CODE_SERVER_ADDRESS = "127.0.0.1";
  private static final String COMMAND_LINE =
      "mvn clean gwt:run-codeserver -f "
          + WORK_DIR
          + " -Dgwt.module="
          + GWT_MODULE
          + " -Dgwt.bindAddress="
          + CODE_SERVER_ADDRESS;

  @Mock private GwtCommandPageView view;
  @Mock private CommandImpl command;

  @InjectMocks private GwtCommandPagePresenter presenter;

  @Before
  public void setUp() {
    when(command.getCommandLine()).thenReturn(COMMAND_LINE);

    presenter.resetFrom(command);
  }

  @Test
  public void testResetting() throws Exception {
    verify(command).getCommandLine();
  }

  @Test
  public void testGo() throws Exception {
    AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

    presenter.go(container);

    verify(container).setWidget(eq(view));
    verify(view).setWorkingDirectory(eq(WORK_DIR));
    verify(view).setGwtModule(eq(GWT_MODULE));
    verify(view).setCodeServerAddress(eq(CODE_SERVER_ADDRESS));
  }

  @Test
  public void testOnWorkingDirectoryChanged() throws Exception {
    when(view.getWorkingDirectory()).thenReturn(WORK_DIR);

    final CommandPage.DirtyStateListener listener = mock(CommandPage.DirtyStateListener.class);
    presenter.setDirtyStateListener(listener);

    presenter.onWorkingDirectoryChanged();

    verify(view).getWorkingDirectory();
    verify(command).setCommandLine(eq(COMMAND_LINE));
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnGwtModuleChanged() throws Exception {
    when(view.getGwtModule()).thenReturn(GWT_MODULE);

    final CommandPage.DirtyStateListener listener = mock(CommandPage.DirtyStateListener.class);
    presenter.setDirtyStateListener(listener);

    presenter.onGwtModuleChanged();

    verify(view).getGwtModule();
    verify(command).setCommandLine(eq(COMMAND_LINE));
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnCodeServerAddressChanged() throws Exception {
    when(view.getCodeServerAddress()).thenReturn(CODE_SERVER_ADDRESS);

    final CommandPage.DirtyStateListener listener = mock(CommandPage.DirtyStateListener.class);
    presenter.setDirtyStateListener(listener);

    presenter.onCodeServerAddressChanged();

    verify(view).getCodeServerAddress();
    verify(command).setCommandLine(eq(COMMAND_LINE));
    verify(listener).onDirtyStateChanged();
  }
}
