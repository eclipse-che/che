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
package org.eclipse.che.plugin.testing.testng.ide.action;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.detector.TestDetector;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.testng.ide.TestNgLocalizationConstant;
import org.eclipse.che.plugin.testing.testng.ide.TestNgResources;

/** Action for running TestNg test. */
public class RunTestNgTestAction extends AbstractTestNgTestAction {
  @Inject
  public RunTestNgTestAction(
      TestNgResources resources,
      TestDetector testDetector,
      DebugConfigurationsManager debugConfigurationsManager,
      TestServiceClient client,
      TestingHandler testingHandler,
      DtoFactory dtoFactory,
      NotificationManager notificationManager,
      AppContext appContext,
      TestResultPresenter testResultPresenter,
      TestNgLocalizationConstant localization) {
    super(
        testDetector,
        testResultPresenter,
        testingHandler,
        debugConfigurationsManager,
        client,
        dtoFactory,
        appContext,
        notificationManager,
        singletonList(PROJECT_PERSPECTIVE_ID),
        localization.actionRunTestDescription(),
        localization.actionRunTestTitle(),
        resources.testIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Pair<String, String> frameworkAndTestName = Pair.of(TESTNG_FRAMEWORK_NAME, null);
    actionPerformed(frameworkAndTestName, false);
  }
}
