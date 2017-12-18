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
package org.eclipse.che.ide.ext.git.client.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Testing {@link StatusCommandPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class StatusCommandPresenterTest extends BaseTest {
  @InjectMocks private StatusCommandPresenter presenter;

  @Mock private GitOutputConsoleFactory gitOutputConsoleFactory;

  @Mock private ProcessesPanelPresenter processesPanelPresenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        new StatusCommandPresenter(
            service,
            gitOutputConsoleFactory,
            processesPanelPresenter,
            constant,
            notificationManager);

    when(service.statusText(any(Path.class))).thenReturn(stringPromise);
    when(stringPromise.then(any(Operation.class))).thenReturn(stringPromise);
    when(stringPromise.catchError(any(Operation.class))).thenReturn(stringPromise);
  }

  @Test
  public void testShowStatusWhenStatusTextRequestIsSuccessful() throws Exception {
    when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);

    presenter.showStatus(project);

    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply("");

    verify(console, times(2)).print(anyString());
    verify(processesPanelPresenter).addCommandOutput(anyObject());
  }

  @Test
  public void testShowStatusWhenStatusTextRequestIsFailed() throws Exception {
    presenter.showStatus(project);

    verify(stringPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);

    verify(notificationManager)
        .notify(anyString(), any(StatusNotification.Status.class), anyObject());
    verify(constant).statusFailed();
  }
}
