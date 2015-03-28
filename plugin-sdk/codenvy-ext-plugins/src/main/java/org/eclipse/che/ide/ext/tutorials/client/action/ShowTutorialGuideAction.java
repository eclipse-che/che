/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.client.action;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.tutorials.client.GuidePageController;
import org.eclipse.che.ide.ext.tutorials.client.TutorialsLocalizationConstant;
import org.eclipse.che.ide.ext.tutorials.client.TutorialsResources;
import org.eclipse.che.ide.ext.tutorials.shared.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to open a tutorial guide.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class ShowTutorialGuideAction extends Action {

    private AppContext           appContext;
    private GuidePageController  guidePageController;
    private AnalyticsEventLogger eventLogger;

    @Inject
    public ShowTutorialGuideAction(GuidePageController guidePageController, TutorialsResources resources,
                                   AppContext appContext,
                                   TutorialsLocalizationConstant localizationConstants,
                                   AnalyticsEventLogger eventLogger) {
        super(localizationConstants.showTutorialGuideActionText(),
              localizationConstants.showTutorialGuideActionDescription(), resources.guide());
        this.guidePageController = guidePageController;
        this.appContext = appContext;
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log("IDE: Show tutorial");
        guidePageController.openTutorialGuide();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        CurrentProject activeProject = appContext.getCurrentProject();
        if (activeProject != null) {
            e.getPresentation().setEnabledAndVisible(activeProject.getRootProject().getType().equals(Constants.TUTORIAL_ID));
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}
