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
package org.eclipse.che.ide.command.editor.page.name;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.command.editor.page.name.NamePageView.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Tests for {@link NamePageViewImpl}. */
@RunWith(GwtMockitoTestRunner.class)
public class NamePageViewImplTest {

  @Mock private ActionDelegate actionDelegate;

  @InjectMocks private NamePageViewImpl view;

  @Before
  public void setUp() throws Exception {
    view.setDelegate(actionDelegate);
  }

  @Test
  public void shouldSetCommandName() throws Exception {
    String newName = "cmd 1";

    view.setCommandName(newName);

    verify(view.commandName).setValue(eq(newName));
  }

  @Test
  public void shouldCallOnNameChanged() throws Exception {
    String commandName = "cmd name";
    when(view.commandName.getValue()).thenReturn(commandName);

    view.onNameChanged(null);

    verify(actionDelegate).onNameChanged(eq(commandName));
  }

  @Test
  public void shouldCallOnCommandRun() throws Exception {
    view.handleRunButton(null);

    verify(actionDelegate).onCommandRun();
  }
}
