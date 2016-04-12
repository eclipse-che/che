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
package org.eclipse.che.plugin.svn.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedEvent;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedHandler;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.resolve.ResolvePresenter;
import org.eclipse.che.plugin.svn.ide.update.SubversionProjectUpdatedEvent;
import org.eclipse.che.plugin.svn.ide.update.SubversionProjectUpdatedHandler;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn resolved" command.
 *
 * @author vzhukovskii@codenvy.com
 */
@Singleton
public class ResolveAction extends SubversionAction {

    private final ResolvePresenter    presenter;

    private String  currentProjectPath;

    @Inject
    public ResolveAction(final AppContext appContext,
                         final SubversionExtensionLocalizationConstants constants,
                         final SubversionExtensionResources resources,
                         final ProjectExplorerPresenter projectExplorerPresenter,
                         final ResolvePresenter presenter,
                         final EventBus eventBus) {
        super(constants.resolvedTitle(), constants.resolvedDescription(), resources.resolved(),
              appContext, constants, resources, projectExplorerPresenter);
        this.presenter = presenter;

        eventBus.addHandler(SubversionProjectUpdatedEvent.TYPE, new SubversionProjectUpdatedHandler() {
            @Override
            public void onProjectUpdated(SubversionProjectUpdatedEvent event) {
                fetchConflicts();
            }
        });
        eventBus.addHandler(CurrentProjectChangedEvent.TYPE, new CurrentProjectChangedHandler() {
            @Override
            public void onCurrentProjectChanged(CurrentProjectChangedEvent event) {
                if (currentProjectPath == null || !currentProjectPath.equals(event.getProjectConfig().getPath())) {
                    currentProjectPath = event.getProjectConfig().getPath();
                    fetchConflicts();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        presenter.showConflictsDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void updateProjectAction(final ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setEnabled(presenter.containsConflicts());
    }

    private void fetchConflicts() {
        presenter.fetchConflictsList(false);
    }
}
