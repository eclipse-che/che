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
package org.eclipse.che.ide.newresource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/**
 * Unit tests for the {@link AbstractNewResourceAction}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class AbstractNewResourceActionTest {

  @Mock DialogFactory dialogFactory;
  @Mock CoreLocalizationConstant coreLocalizationConstant;
  @Mock EventBus eventBus;
  @Mock AppContext appContext;
  @Mock NotificationManager notificationManager;
  @Mock Provider<EditorAgent> editorAgentProvider;

  @Mock Resource file;
  @Mock Container parent;

  @Mock Promise<File> filePromise;

  private AbstractNewResourceAction action;

  @Before
  public void setUp() throws Exception {
    action =
        new AbstractNewResourceAction(
            "",
            "",
            null,
            dialogFactory,
            coreLocalizationConstant,
            eventBus,
            appContext,
            notificationManager,
            editorAgentProvider) {
          //
        };
  }

  @Test
  public void testShouldCreateFileIfSelectedFile() throws Exception {
    when(file.getParent()).thenReturn(parent);
    when(appContext.getResource()).thenReturn(file);
    when(parent.newFile(anyString(), anyString())).thenReturn(filePromise);
    when(filePromise.then(any(Operation.class))).thenReturn(filePromise);
    when(filePromise.catchError(any(Operation.class))).thenReturn(filePromise);

    action.createFile("name");

    verify(parent).newFile(eq("name"), eq(""));
  }

  @Test
  public void testShouldCreateFileIfSelectedContainer() throws Exception {
    when(appContext.getResource()).thenReturn(parent);

    when(parent.newFile(anyString(), anyString())).thenReturn(filePromise);
    when(filePromise.then(any(Operation.class))).thenReturn(filePromise);
    when(filePromise.catchError(any(Operation.class))).thenReturn(filePromise);

    action.createFile("name");

    verify(parent).newFile(eq("name"), eq(""));
  }

  @Test(expected = IllegalStateException.class)
  public void testShouldThrowExceptionIfFileDoesNotContainParent() throws Exception {
    when(appContext.getResource()).thenReturn(file);
    when(file.getParent()).thenReturn(null);

    action.createFile("name");
  }
}
