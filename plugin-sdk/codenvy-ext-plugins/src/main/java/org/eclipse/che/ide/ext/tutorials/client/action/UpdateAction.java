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

import org.eclipse.che.ide.Constants;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.ext.runner.client.selection.SelectionManager;
import org.eclipse.che.ide.ext.tutorials.client.TutorialsLocalizationConstant;
import org.eclipse.che.ide.ext.tutorials.client.TutorialsResources;
import org.eclipse.che.ide.ext.tutorials.client.update.ExtensionUpdater;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.eclipse.che.ide.ext.runner.client.models.Runner.Status.RUNNING;

/**
 * Action to update Codenvy Extension project on SDK runner.
 *
 * @author Artem Zatsarynnyy
 * @author Valeriy Svydenko
 */
@Singleton
public class UpdateAction extends Action {
    private ExtensionUpdater extensionsUpdater;
    private SelectionManager selectionManager;
    private AppContext       appContext;

    @Inject
    public UpdateAction(ExtensionUpdater extensionsUpdater,
                        TutorialsResources resources,
                        TutorialsLocalizationConstant localizationConstants,
                        SelectionManager selectionManager,
                        AppContext appContext) {
        super(localizationConstants.updateExtensionText(), localizationConstants.updateExtensionDescription(),
              resources.updateApp());
        this.extensionsUpdater = extensionsUpdater;
        this.selectionManager = selectionManager;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        extensionsUpdater.updateExtension();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        Runner selectionRunner = selectionManager.getRunner();
        CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject != null) {
            // this action is specific for the Codenvy Extension project only
            e.getPresentation()
             .setVisible(Constants.CODENVY_PLUGIN_ID.equals(currentProject.getProjectDescription().getType()));
            e.getPresentation().setEnabled(selectionRunner != null && RUNNING.equals(selectionRunner.getStatus()));
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}