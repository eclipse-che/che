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
package org.eclipse.che.ide.command.toolbar.commands.button;

import static org.junit.Assert.assertEquals;

import org.eclipse.che.ide.api.command.CommandImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link AbstractMenuItem}. */
@RunWith(MockitoJUnitRunner.class)
public class AbstractMenuItemTest {

  @Mock private CommandImpl command;

  private AbstractMenuItem item;

  @Before
  public void setUp() throws Exception {
    item = new DummyMenuItem(command);
  }

  @Test
  public void testGetCommand() throws Exception {
    assertEquals(command, item.getCommand());
  }

  private static class DummyMenuItem extends AbstractMenuItem {

    DummyMenuItem(CommandImpl command) {
      super(command);
    }

    @Override
    public String getName() {
      return null;
    }
  }
}
