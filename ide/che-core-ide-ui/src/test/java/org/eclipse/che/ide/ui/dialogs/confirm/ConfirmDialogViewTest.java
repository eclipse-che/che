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
package org.eclipse.che.ide.ui.dialogs.confirm;

import static org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogView.ActionDelegate;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.eclipse.che.ide.ui.dialogs.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Testing {@link ConfirmDialogViewImpl} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class ConfirmDialogViewTest extends BaseTest {
  @Mock private ActionDelegate actionDelegate;
  @Mock private ConfirmDialogFooter footer;
  private ConfirmDialogViewImpl view;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    view = new ConfirmDialogViewImpl(footer);
  }

  @Test
  public void shouldSetDelegateOnFooter() throws Exception {
    view.setDelegate(actionDelegate);

    verify(footer).setDelegate(eq(actionDelegate));
  }
}
