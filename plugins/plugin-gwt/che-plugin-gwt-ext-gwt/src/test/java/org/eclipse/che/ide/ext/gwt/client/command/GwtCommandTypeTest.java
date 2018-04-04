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
package org.eclipse.che.ide.ext.gwt.client.command;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;
import org.eclipse.che.ide.macro.DevMachineHostNameMacro;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class GwtCommandTypeTest {

  @Mock private GwtResources gwtResources;
  @Mock private GwtCommandPagePresenter gwtCommandPagePresenter;
  @Mock private CurrentProjectPathMacro currentProjectPathMacro;
  @Mock private DevMachineHostNameMacro devMachineHostNameMacro;
  @Mock private IconRegistry iconRegistry;

  @InjectMocks private GwtCommandType gwtCommandType;

  @Test
  public void shouldReturnPages() throws Exception {
    final Collection<CommandPage> pages = gwtCommandType.getPages();

    assertTrue(pages.contains(gwtCommandPagePresenter));
  }

  @Test
  public void shouldReturnCommandTemplate() throws Exception {
    gwtCommandType.getCommandLineTemplate();

    verify(currentProjectPathMacro).getName();
    verify(devMachineHostNameMacro).getName();
  }
}
