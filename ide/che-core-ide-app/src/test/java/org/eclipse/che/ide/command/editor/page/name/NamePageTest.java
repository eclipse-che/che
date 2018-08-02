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
package org.eclipse.che.ide.command.editor.page.name;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage.DirtyStateListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link NamePage}. */
@RunWith(MockitoJUnitRunner.class)
public class NamePageTest {

  private static final String COMMAND_NAME = "build";

  @Mock private NamePageView view;
  @Mock private EditorMessages messages;
  @Mock private CommandExecutor commandExecutor;

  @InjectMocks private NamePage page;

  @Mock private DirtyStateListener dirtyStateListener;
  @Mock private CommandImpl editedCommand;

  @Before
  public void setUp() throws Exception {
    when(editedCommand.getName()).thenReturn(COMMAND_NAME);

    page.setDirtyStateListener(dirtyStateListener);
    page.edit(editedCommand);
  }

  @Test
  public void shouldSetViewDelegate() throws Exception {
    verify(view).setDelegate(page);
  }

  @Test
  public void shouldInitializeView() throws Exception {
    verify(view).setCommandName(eq(COMMAND_NAME));
  }

  @Test
  public void shouldReturnView() throws Exception {
    assertEquals(view, page.getView());
  }

  @Test
  public void shouldNotifyListenerWhenNameChanged() throws Exception {
    page.onNameChanged("mvn");

    verify(dirtyStateListener, times(2)).onDirtyStateChanged();
  }

  @Test
  public void shouldExecuteCommandWhenTestingRequested() throws Exception {
    page.onCommandRun();

    verify(commandExecutor).executeCommand(editedCommand);
  }
}
