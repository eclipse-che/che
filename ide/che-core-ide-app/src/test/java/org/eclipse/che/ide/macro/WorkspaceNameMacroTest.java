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
package org.eclipse.che.ide.macro;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.WorkspaceConfigImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the {@link WorkspaceNameMacro}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkspaceNameMacroTest {

  public static final String WS_NAME = "workspace";

  @Mock AppContext appContext;

  @Mock PromiseProvider promiseProvider;

  @Mock CoreLocalizationConstant localizationConstants;

  private WorkspaceNameMacro provider;

  @Before
  public void init() throws Exception {
    WorkspaceImpl workspace = mock(WorkspaceImpl.class);
    WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);
    when(workspaceConfig.getName()).thenReturn(WS_NAME);
    when(workspace.getConfig()).thenReturn(workspaceConfig);
    when(appContext.getWorkspace()).thenReturn(workspace);

    provider = new WorkspaceNameMacro(appContext, promiseProvider, localizationConstants);
  }

  @Test
  public void getKey() throws Exception {
    assertSame(provider.getName(), WorkspaceNameMacro.KEY);
  }

  @Test
  public void getValue() throws Exception {
    provider.expand();

    verify(promiseProvider).resolve(eq(WS_NAME));
  }
}
