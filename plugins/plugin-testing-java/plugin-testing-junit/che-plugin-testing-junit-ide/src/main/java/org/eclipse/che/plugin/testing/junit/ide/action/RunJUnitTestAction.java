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
package org.eclipse.che.plugin.testing.junit.ide.action;

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
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestResources;

/** The action for running JUnit test. */
public class RunJUnitTestAction extends AbstractJUnitTestAction {

  private TestDetector testDetector;
  private AppContext appContext;

  @Inject
  public RunJUnitTestAction(
      JUnitTestResources resources,
      TestDetector testDetector,
      TestingHandler testingHandler,
      TestResultPresenter testResultPresenter,
      DebugConfigurationsManager debugConfigurationsManager,
      TestServiceClient client,
      AppContext appContext,
      DtoFactory dtoFactory,
      NotificationManager notificationManager,
      JUnitTestLocalizationConstant localization) {
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
    this.testDetector = testDetector;
    this.appContext = appContext;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Pair<String, String> frameworkAndTestName = Pair.of(JUNIT_FRAMEWORK_NAME, null);
    actionPerformed(frameworkAndTestName, false);
  }
}
