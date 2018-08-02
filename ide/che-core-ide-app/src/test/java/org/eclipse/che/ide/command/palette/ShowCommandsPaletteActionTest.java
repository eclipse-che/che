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
package org.eclipse.che.ide.command.palette;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link ShowCommandsPaletteAction}. */
@RunWith(MockitoJUnitRunner.class)
public class ShowCommandsPaletteActionTest {

  @Mock private PaletteMessages messages;
  @Mock private CommandsPalettePresenter presenter;

  @InjectMocks private ShowCommandsPaletteAction action;

  @Test
  public void shouldInitializeAction() {
    verify(messages).actionShowPaletteTitle();
    verify(messages).actionShowPaletteDescription();
  }

  @Test
  public void shouldShowDialog() {
    action.actionPerformed(mock(ActionEvent.class));

    verify(presenter).showDialog();
  }
}
