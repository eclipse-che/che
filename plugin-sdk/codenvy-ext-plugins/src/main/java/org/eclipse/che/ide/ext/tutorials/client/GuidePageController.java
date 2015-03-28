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
package org.eclipse.che.ide.ext.tutorials.client;

import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.ext.tutorials.shared.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Controls a tutorial page state: shows or hides it.
 * Automatically shows a tutorial page when project opening and closes page when project closing.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class GuidePageController {
    private WorkspaceAgent workspaceAgent;
    private GuidePage      guidePage;

    @Inject
    public GuidePageController(EventBus eventBus, WorkspaceAgent workspaceAgent, GuidePage guidePage) {
        this.workspaceAgent = workspaceAgent;
        this.guidePage = guidePage;

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                if (event.getProject().getType().equals(Constants.TUTORIAL_ID)) {
//                    openTutorialGuide();
                }
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
                if (event.getProject().getDescription().equals(Constants.TUTORIAL_ID)) {
//                    closeTutorialGuide();

                }
            }
        });
    }

    /** Open tutorial guide page. */
    public void openTutorialGuide() {
        workspaceAgent.openPart(guidePage, PartStackType.EDITING);
    }

    /** Close tutorial guide page. */
    public void closeTutorialGuide() {
        workspaceAgent.removePart(guidePage);
    }
}
