/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.navigation.NavigateToFilePresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for finding file by name and opening it.
 *
 * @author Ann Shumilova
 * @author Dmitry Shnurenko
 */
@Singleton
public class NavigateToFileAction extends AbstractPerspectiveAction {

    private final NavigateToFilePresenter  presenter;
    private final AnalyticsEventLogger     eventLogger;
    private final ProjectExplorerPresenter projectExplorerPresenter;

    @Inject
    public NavigateToFileAction(NavigateToFilePresenter presenter,
                                AnalyticsEventLogger eventLogger,
                                Resources resources,
                                ProjectExplorerPresenter projectExplorerPresenter,
                                CoreLocalizationConstant localizationConstant) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstant.actionNavigateToFileText(),
              localizationConstant.actionNavigateToFileDescription(),
              null,
              resources.navigateToFile());
        this.presenter = presenter;
        this.eventLogger = eventLogger;
        this.projectExplorerPresenter = projectExplorerPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showDialog();
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabled(!projectExplorerPresenter.getRootNodes().isEmpty());
    }
}
