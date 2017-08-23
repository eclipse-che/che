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
package org.eclipse.che.ide.ui.dialogs.message;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.ide.ui.dialogs.BaseTest;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Testing {@link MessageDialogPresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class MessageDialogPresenterTest extends BaseTest {
  @Mock private MessageDialogView view;
  @Mock private ConfirmCallback confirmCallback;
  private MessageDialogPresenter presenter;

  @Before
  @Override
  public void setUp() {
    super.setUp();

    when(isWidget.asWidget()).thenReturn(null);

    presenter =
        new MessageDialogPresenter(view, TITLE, isWidget, confirmCallback, CONFIRM_BUTTON_TEXT);
  }

  @Test
  public void shouldCallCallbackOnAccepted() throws Exception {
    presenter.accepted();

    verify(view).closeDialog();
    verify(confirmCallback).accepted();
  }

  @Test
  public void shouldNotCallCallbackOnAccepted() throws Exception {
    presenter = new MessageDialogPresenter(view, TITLE, MESSAGE, null);

    presenter.accepted();

    verify(view).closeDialog();
    verify(confirmCallback, never()).accepted();
  }

  @Test
  public void shouldShowView() throws Exception {
    presenter.show();

    verify(view).showDialog();
  }
}
