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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.projecttype.wizard.presenter.ProjectWizardPresenter;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@Singleton
public class CreateModuleAction extends AbstractPerspectiveAction {

    private final AppContext               appContext;
    private final ProjectWizardPresenter   wizard;
    private final AnalyticsEventLogger     eventLogger;
    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public CreateModuleAction(AppContext appContext,
                              NodesResources resources,
                              ProjectWizardPresenter wizard,
                              AnalyticsEventLogger eventLogger,
                              ProjectExplorerPresenter projectExplorer) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), "Create Module...", "Create module from existing folder", null, resources.moduleFolder());
        this.appContext = appContext;
        this.wizard = wizard;
        this.eventLogger = eventLogger;
        this.projectExplorer = projectExplorer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        FolderReferenceNode folderNode = getResourceBasedNode();
        if (folderNode != null) {
            wizard.show(folderNode.getData());
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        if (appContext.getCurrentProject() == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        event.getPresentation().setEnabledAndVisible(getResourceBasedNode() != null);
    }

    @Nullable
    protected FolderReferenceNode getResourceBasedNode() {
        List<?> selection = projectExplorer.getSelection().getAllElements();
        //we should be sure that user selected single element to work with it
        if (selection.isEmpty() || selection.size() > 1) {
            return null;
        }

        Object o = selection.get(0);

        if (o instanceof FolderReferenceNode) {
            return (FolderReferenceNode)o;
        }

        return null;
    }
}
