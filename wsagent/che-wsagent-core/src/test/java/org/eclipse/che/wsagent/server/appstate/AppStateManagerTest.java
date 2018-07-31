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
package org.eclipse.che.wsagent.server.appstate;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.fs.server.FsManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link AppStateManager}
 *
 * @author Roman Nikitenko
 */
@Listeners(MockitoTestNGListener.class)
public class AppStateManagerTest {

  private static final String USER_ID = "userId";
  private static final String APP_STATE =
      "{\"projectExplorer\":{\"revealPath\":[\"/spring\"],\"showHiddenFiles\":false}}";

  @Mock private FsManager fsManager;
  @InjectMocks private AppStateManager appStateManager;

  @Test(expectedExceptions = ValidationException.class)
  public void shouldThrowExceptionAtGettingAppState() throws Exception {
    // user id is not valid
    appStateManager.loadAppState("");
  }

  @Test
  public void shouldReadState() throws Exception {
    when(fsManager.existsAsFile(anyString())).thenReturn(true);

    appStateManager.loadAppState(USER_ID);

    verify(fsManager).existsAsFile(anyString());
    verify(fsManager).readAsString(anyString());
  }

  @Test
  public void shouldReturnEmptyStringWhenStateNotFound() throws Exception {
    when(fsManager.existsAsFile(anyString())).thenReturn(false);

    String appState = appStateManager.loadAppState(USER_ID);

    verify(fsManager).existsAsFile(anyString());
    verify(fsManager, never()).readAsString(anyString());
    assertEquals(appState, "");
  }

  @Test
  public void shouldSaveState() throws Exception {
    when(fsManager.existsAsFile(anyString())).thenReturn(true);

    appStateManager.saveState(USER_ID, APP_STATE);

    verify(fsManager, never()).createFile(anyString(), anyBoolean(), anyBoolean());
    verify(fsManager).update(anyString(), eq(APP_STATE));
  }

  @Test
  public void shouldCreateFileWithPatents() throws Exception {
    when(fsManager.existsAsFile(anyString())).thenReturn(false);

    appStateManager.saveState(USER_ID, APP_STATE);

    verify(fsManager, never()).update(anyString(), eq(APP_STATE));
    verify(fsManager).createFile(anyString(), eq(APP_STATE), eq(false), eq(true));
  }

  @Test(expectedExceptions = ValidationException.class)
  public void shouldThrowExceptionAtSavingAppState() throws Exception {
    // user id is not valid
    appStateManager.saveState("", APP_STATE);
  }
}
