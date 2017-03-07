/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.junit.ide.action;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import java.util.Map;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;

/**
 * @author Mirage Abeysekara
 * @author David Festal
 */
public class RunTestActionDelegate {
    private final Source source;

    public interface Source {
        NotificationManager getNotificationManager();

        AppContext getAppContext();

        TestServiceClient getService();

        TestResultPresenter getPresenter();
    }

    public RunTestActionDelegate(Source source) {
        this.source = source;
    }

    public void doRunTests(ActionEvent e, Map<String, String> parameters) {
        final StatusNotification notification = new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
        source.getNotificationManager().notify(notification);
        final Project project = source.getAppContext().getRootProject();
        parameters.put("updateClasspath", "true");
        Promise<TestResult> testResultPromise = source.getService().getTestResult(project.getPath(), "junit", parameters, notification);
        testResultPromise.then(new Operation<TestResult>() {
            @Override
            public void apply(TestResult result) throws OperationException {
                notification.setStatus(SUCCESS);
                if (result.isSuccess()) {
                    notification.setTitle("Test runner executed successfully");
                    notification.setContent("All tests are passed");
                } else {
                    notification.setTitle("Test runner executed successfully with test failures.");
                    notification.setContent(result.getFailureCount() + " test(s) failed.\n");
                }
                source.getPresenter().handleResponse(result);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError exception) throws OperationException {
                final String errorMessage = (exception.getMessage() != null) ? exception.getMessage()
                    : "Failed to run test cases";
                notification.setContent(errorMessage);
                notification.setStatus(FAIL);
            }
        });
    }
}
