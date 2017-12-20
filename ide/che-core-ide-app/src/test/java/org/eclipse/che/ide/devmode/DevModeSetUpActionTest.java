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
package org.eclipse.che.ide.devmode;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for the {@link DevModeSetUpAction}. */
@RunWith(MockitoJUnitRunner.class)
public class DevModeSetUpActionTest {

  @Mock CoreLocalizationConstant messages;
  @Mock GWTDevMode gwtDevMode;

  @InjectMocks DevModeSetUpAction action;

  @Test
  public void shouldSetUpDevModeOnPerformingAction() throws Exception {
    action.actionPerformed(mock(ActionEvent.class));

    verify(gwtDevMode).setUp();
  }
}
