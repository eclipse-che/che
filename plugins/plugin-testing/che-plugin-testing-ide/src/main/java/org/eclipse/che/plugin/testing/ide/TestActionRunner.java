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
package org.eclipse.che.plugin.testing.ide;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;

/**
 * Test action runner.
 *
 * @author Bartlomiej Laczkowski
 */
public class TestActionRunner {

  private final TestServiceClient service;
  private final NotificationManager notificationManager;
  private final TestResultPresenter presenter;

  @Inject
  public TestActionRunner(
      TestServiceClient service,
      NotificationManager notificationManager,
      TestResultPresenter presenter) {
    this.service = service;
    this.notificationManager = notificationManager;
    this.presenter = presenter;
  }

  public void run(String testFramework, String projectPath, Map<String, String> parameters) {
    presenter.clear();
    final StatusNotification notification =
        new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
    notificationManager.notify(notification);
    Promise<TestResultRootDto> testResultPromise =
        service.runTests(testFramework, projectPath, parameters);
    testResultPromise
        .then(
            new Operation<TestResultRootDto>() {
              @Override
              public void apply(TestResultRootDto result) throws OperationException {
                if (result.isEmpty()) {
                  notification.setStatus(FAIL);
                  notification.setTitle("No tests could be found");
                  return;
                }
                notification.setStatus(SUCCESS);
                switch (result.getStatus()) {
                  case SUCCESS:
                    {
                      notification.setTitle("Test runner executed successfully");
                      notification.setContent("All tests passed.");
                      break;
                    }
                  case FAILURE:
                    {
                      notification.setTitle("Test runner executed successfully with test failures");
                      notification.setContent("Some test(s) failed.");
                      break;
                    }
                  case ERROR:
                    {
                      notification.setTitle("Test runner executed successfully with test errors");
                      notification.setContent("Some test(s) failed with errors.");
                      break;
                    }
                  case WARNING:
                    {
                      notification.setTitle("Test runner executed successfully with test warnings");
                      notification.setContent("Some test(s) passed with warnings.");
                      break;
                    }
                  case SKIPPED:
                    {
                      notification.setTitle(
                          "Test runner executed successfully with some tests skipped");
                      notification.setContent("Some test(s) were skipped.");
                      break;
                    }
                }
                presenter.handleResponse(result);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError exception) throws OperationException {
                notification.setTitle("Failed to execute test runner");
                notification.setContent("Please see dev-machine log for more details.");
                notification.setStatus(FAIL);
              }
            });
  }
}
