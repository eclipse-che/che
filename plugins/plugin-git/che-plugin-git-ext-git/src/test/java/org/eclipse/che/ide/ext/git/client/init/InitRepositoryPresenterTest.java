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
package org.eclipse.che.ide.ext.git.client.init;

import static org.eclipse.che.ide.ext.git.client.init.InitRepositoryPresenter.INIT_COMMAND_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;

/**
 * Testing {@link InitRepositoryPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
public class InitRepositoryPresenterTest extends BaseTest {

  private InitRepositoryPresenter presenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        new InitRepositoryPresenter(
            constant,
            notificationManager,
            gitOutputConsoleFactory,
            processesPanelPresenter,
            service,
            appContext);

    when(service.init(any(Path.class), anyBoolean())).thenReturn(voidPromise);
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
    when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);
  }

  @Test
  public void testOnOkClickedInitWSRequestAndGetProjectIsSuccessful() throws Exception {
    presenter.initRepository(project);

    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);

    verify(gitOutputConsoleFactory).create(eq(INIT_COMMAND_NAME));
    verify(console).print(eq(constant.initSuccess()));
    verify(processesPanelPresenter).addCommandOutput(eq(console));
    verify(notificationManager).notify(anyString());

    verify(project).synchronize();
  }

  @Test
  public void testOnOkClickedInitWSRequestIsFailed() throws Exception {
    presenter.initRepository(project);

    verify(voidPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);

    verify(constant).initFailed();
    verify(gitOutputConsoleFactory).create(INIT_COMMAND_NAME);
    verify(console).printError(anyObject());
    verify(processesPanelPresenter).addCommandOutput(eq(console));
    verify(notificationManager)
        .notify(anyString(), any(StatusNotification.Status.class), anyObject());
  }
}
