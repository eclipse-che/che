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
package org.eclipse.che.ide.ext.git.client.add;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Testing {@link AddToIndexPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 */
public class AddToIndexPresenterTest extends BaseTest {
  private static final String MESSAGE = "message";
  private static final String FOLDER_NAME = "folder name";
  private static final String FILE_NAME = "file name";

  @Mock private AddToIndexView view;
  @Mock private GitOutputConsole console;

  private AddToIndexPresenter presenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        new AddToIndexPresenter(
            view,
            appContext,
            constant,
            gitOutputConsoleFactory,
            processesPanelPresenter,
            service,
            notificationManager);

    when(appContext.getResources()).thenReturn(new Resource[] {});
    when(appContext.getRootProject()).thenReturn(mock(Project.class));
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
    when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);
    when(service.add(any(Path.class), anyBoolean(), any(Path[].class))).thenReturn(voidPromise);
    when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);
  }

  @Test
  public void shouldSetFolderMessageToViewAndShowDialog() throws Exception {
    Container folder = mock(Container.class);
    when(folder.getName()).thenReturn(FOLDER_NAME);
    when(appContext.getResource()).thenReturn(folder);
    when(appContext.getResources()).thenReturn(new Resource[] {folder});
    when(constant.addToIndexFolder(FOLDER_NAME)).thenReturn(MESSAGE);

    presenter.showDialog();

    verify(constant).addToIndexFolder(eq(FOLDER_NAME));
    verify(view).setMessage(eq(MESSAGE));
    verify(view).setUpdated(eq(false));
    verify(view).showDialog();
  }

  @Test
  public void shouldSetFileMessageToViewAndShowDialog() throws Exception {
    File file = mock(File.class);
    when(file.getName()).thenReturn(FILE_NAME);
    when(appContext.getResource()).thenReturn(file);
    when(appContext.getResources()).thenReturn(new Resource[] {file});
    when(constant.addToIndexFile(FILE_NAME)).thenReturn(MESSAGE);

    presenter.showDialog();

    verify(constant).addToIndexFile(eq(FILE_NAME));
    verify(view).setMessage(eq(MESSAGE));
    verify(view).setUpdated(eq(false));
    verify(view).showDialog();
  }

  @Test
  public void shouldSetMultiSelectionMessageToViewAndShowDialog() throws Exception {
    File file1 = mock(File.class);
    File file2 = mock(File.class);
    when(file1.getName()).thenReturn(FILE_NAME + "1");
    when(file2.getName()).thenReturn(FILE_NAME + "2");
    when(appContext.getResource()).thenReturn(file2);
    when(appContext.getResources()).thenReturn(new Resource[] {file1, file2});
    when(constant.addToIndexMultiSelect()).thenReturn(MESSAGE);

    presenter.showDialog();

    verify(constant).addToIndexMultiSelect();
    verify(view).setMessage(eq(MESSAGE));
    verify(view).setUpdated(eq(false));
    verify(view).showDialog();
  }

  @Test
  public void shouldAddToIndexWhenAddButtonClicked() throws Exception {
    when(constant.addSuccess()).thenReturn(MESSAGE);

    presenter.onAddClicked();
    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);

    verify(console).print(eq(MESSAGE));
    verify(notificationManager).notify(MESSAGE);
  }

  @Test
  public void shouldPrintErrorWhenFailedAddToIndex() throws Exception {
    when(constant.addFailed()).thenReturn(MESSAGE);

    presenter.onAddClicked();
    verify(voidPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(null);

    verify(console).printError(eq(MESSAGE));
    verify(notificationManager).notify(eq(MESSAGE), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void testOnCancelClicked() throws Exception {
    presenter.onCancelClicked();

    verify(view).close();
  }
}
