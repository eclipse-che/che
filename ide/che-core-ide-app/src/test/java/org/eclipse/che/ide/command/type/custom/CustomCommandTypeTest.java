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
