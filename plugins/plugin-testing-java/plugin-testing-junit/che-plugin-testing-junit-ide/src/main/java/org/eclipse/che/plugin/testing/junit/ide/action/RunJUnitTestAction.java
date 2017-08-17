/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.junit.ide.action;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunDebugTestAbstractAction;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.view2.TestResultPresenter;
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestResources;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The action for running JUnit test.
 */
public class RunJUnitTestAction extends RunDebugTestAbstractAction {

    @Inject
    public RunJUnitTestAction(JUnitTestResources resources,
                              EventBus eventBus,
                              TestingHandler testingHandler,
                              TestResultPresenter testResultPresenter,
                              DebugConfigurationsManager debugConfigurationsManager,
                              TestServiceClient client,
                              AppContext appContext,
                              DtoFactory dtoFactory,
                              NotificationManager notificationManager,
                              JUnitTestLocalizationConstant localization) {
        super(eventBus,
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
        Pair<String, String> frameworkAndTestName = Pair.of(JUNIT_FRAMEWORK_NAME, null);
        actionPerformed(frameworkAndTestName, false);
    }

}
