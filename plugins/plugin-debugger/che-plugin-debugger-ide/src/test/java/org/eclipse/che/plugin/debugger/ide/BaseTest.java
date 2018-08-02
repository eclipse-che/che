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
package org.eclipse.che.plugin.debugger.ide;

import static org.mockito.Mockito.when;

import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Base test for java debugger extension.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class BaseTest {
  public static final String HOST = "some.host";
  public static final int PORT = 8000;
  public static final String NAME = "vm.name";
  public static final String VERSION = "vm.version";
  public static final String DEBUGGER_ID = "debugger_id";
  public static final String DEBUGGER_TYPE = "id";
  public static final boolean DISABLE_BUTTON = false;
  @Mock protected DebuggerInfoDto debuggerInfoDto;
  @Mock protected DebugSessionDto debugSessionDto;
  @Mock protected DebuggerLocalizationConstant constants;
  @Mock protected DtoFactory dtoFactory;

  @Before
  public void setUp() {
    when(debugSessionDto.getDebuggerInfo()).thenReturn(debuggerInfoDto);
    when(debugSessionDto.getId()).thenReturn(DEBUGGER_ID);
    when(debugSessionDto.getType()).thenReturn(DEBUGGER_TYPE);
    when(debuggerInfoDto.getHost()).thenReturn(HOST);
    when(debuggerInfoDto.getPort()).thenReturn(PORT);
    when(debuggerInfoDto.getName()).thenReturn(NAME);
    when(debuggerInfoDto.getVersion()).thenReturn(VERSION);
  }
}
