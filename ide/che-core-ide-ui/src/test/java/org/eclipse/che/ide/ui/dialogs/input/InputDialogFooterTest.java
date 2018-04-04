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
package org.eclipse.che.ide.ui.dialogs.input;

import static org.eclipse.che.ide.ui.dialogs.input.InputDialogView.ActionDelegate;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gwt.event.dom.client.ClickEvent;
import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.che.ide.ui.dialogs.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Testing {@link InputDialogFooter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class InputDialogFooterTest extends BaseTest {
  @Mock private UILocalizationConstant uiLocalizationConstant;
  @Mock private ActionDelegate actionDelegate;
  @InjectMocks private InputDialogFooter footer;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    footer.setDelegate(actionDelegate);
  }

  @Test
  public void shouldCallAcceptedOnOkClicked() throws Exception {
    footer.handleOkClick(mock(ClickEvent.class));

    verify(actionDelegate).accepted();
  }

  @Test
  public void shouldCallCancelledOnCancelClicked() throws Exception {
    footer.handleCancelClick(mock(ClickEvent.class));

    verify(actionDelegate).cancelled();
  }
}
