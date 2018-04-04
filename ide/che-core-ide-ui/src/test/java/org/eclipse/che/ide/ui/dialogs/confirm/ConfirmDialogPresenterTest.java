/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.dialogs.confirm;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.ide.ui.dialogs.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Testing {@link ConfirmDialogPresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class ConfirmDialogPresenterTest extends BaseTest {
  @Mock private ConfirmDialogView view;
  private ConfirmDialogPresenter presenter;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    presenter = new ConfirmDialogPresenter(view, TITLE, MESSAGE, confirmCallback, cancelCallback);
  }

  @Test
  public void shouldCallCallbackOnCanceled() throws Exception {
    presenter.cancelled();

    verify(view).closeDialog();
    verify(cancelCallback).cancelled();
  }

  @Test
  public void shouldNotCallCallbackOnCanceled() throws Exception {
    presenter = new ConfirmDialogPresenter(view, TITLE, MESSAGE, confirmCallback, null);

    presenter.cancelled();

    verify(view).closeDialog();
    verify(cancelCallback, never()).cancelled();
  }

  @Test
  public void shouldCallCallbackOnAccepted() throws Exception {
    presenter.accepted();

    verify(view).closeDialog();
    verify(confirmCallback).accepted();
  }

  @Test
  public void shouldNotCallCallbackOnAccepted() throws Exception {
    presenter = new ConfirmDialogPresenter(view, TITLE, MESSAGE, null, cancelCallback);

    presenter.accepted();

    verify(view).closeDialog();
    verify(confirmCallback, never()).accepted();
  }

  @Test
  public void shouldShowView() throws Exception {
    presenter.show();

    verify(view).showDialog();
  }

  @Test
  public void onEnterClickedWhenAcceptButtonInFocusTest() throws Exception {
    when(view.isOkButtonInFocus()).thenReturn(true);

    presenter.onEnterClicked();

    verify(view).closeDialog();
    verify(confirmCallback).accepted();
    verify(cancelCallback, never()).cancelled();
  }

  @Test
  public void onEnterClickedWhenCancelButtonInFocusTest() throws Exception {
    when(view.isCancelButtonInFocus()).thenReturn(true);

    presenter.onEnterClicked();

    verify(view).closeDialog();
    verify(confirmCallback, never()).accepted();
    verify(cancelCallback).cancelled();
  }
}
