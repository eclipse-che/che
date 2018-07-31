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
package org.eclipse.che.ide.command.type.custom;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.machine.MachineResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class CustomCommandTypeTest {

  @Mock private MachineResources machineResources;
  @Mock private IconRegistry iconRegistry;
  @Mock private CustomPagePresenter arbitraryPagePresenter;

  @InjectMocks private CustomCommandType arbitraryCommandType;

  @Test
  public void shouldReturnPages() throws Exception {
    Collection<CommandPage> pages = arbitraryCommandType.getPages();

    assertTrue(pages.contains(arbitraryPagePresenter));
  }
}
