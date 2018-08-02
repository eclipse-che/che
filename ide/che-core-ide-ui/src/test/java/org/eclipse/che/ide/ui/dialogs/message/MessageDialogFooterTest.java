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
package org.eclipse.che.ide.ui.dialogs.message;

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
 * Testing {@link MessageDialogFooter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class MessageDialogFooterTest extends BaseTest {
  @Mock private UILocalizationConstant uiLocalizationConstant;
  @Mock private MessageDialogView.ActionDelegate actionDelegate;
  @InjectMocks private MessageDialogFooter footer;

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
}
