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
package org.eclipse.che.ide.ui.dialogs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** @author Artem Zatsarynnyi */
@RunWith(GwtMockitoTestRunner.class)
public abstract class BaseTest {
  protected static String TITLE = "title";
  protected static String MESSAGE = "message";
  protected static String CONFIRM_BUTTON_TEXT = "text";
  @Mock protected CancelCallback cancelCallback;
  @Mock protected ConfirmCallback confirmCallback;
  @Mock protected InputCallback inputCallback;
  @Mock protected IsWidget isWidget;

  @Before
  public void setUp() {}
}
