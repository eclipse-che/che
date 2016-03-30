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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.event.project.ProjectReadyEvent;
import org.eclipse.che.ide.api.event.project.ProjectReadyHandler;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.resolve.ResolvePresenter;
import org.eclipse.che.plugin.svn.ide.update.SubversionProjectUpdatedEvent;
import org.eclipse.che.plugin.svn.ide.update.SubversionProjectUpdatedHandler;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import java.util.List;

/**
 * Extension of {@link SubversionAction} for implementing the "svn resolved" command.
 *
 * @author vzhukovskii@codenvy.com
 */
@Singleton
public class ResolveAction extends SubversionAction {

    private       ProjectExplorerPresenter projectExplorerPresenter;
    private final ResolvePresenter         presenter;

    private List<String> conflictsList;
    private boolean enable = false;

    @Inject
    public ResolveAction(final AppContext appContext,
                         final SubversionExtensionLocalizationConstants constants,
                         final SubversionExtensionResources resources,
                         final ProjectExplorerPresenter projectExplorerPresenter,
                         final ResolvePresenter presenter,
                         final EventBus eventBus) {
        super(constants.resolvedTitle(), constants.resolvedDescription(), resources.resolved(),
              appContext, constants, resources, projectExplorerPresenter);
        this.projectExplorerPresenter = projectExplorerPresenter;
        this.presenter = presenter;

        eventBus.addHandler(SubversionProjectUpdatedEvent.TYPE, new SubversionProjectUpdatedHandler() {
            @Override
            public void onProjectUpdated(SubversionProjectUpdatedEvent event) {fetchConflicts();}
        });
        eventBus.addHandler(ProjectReadyEvent.TYPE, new ProjectReadyHandler() {
            @Override
            public void onProjectReady(ProjectReadyEvent projectReadyEvent) {fetchConflicts();}
        });
        eventBus.addHandler(SelectionChangedEvent.TYPE, new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                enable = false;

                HasStorablePath selectedNode = ResolveAction.this.getStorableNodeFromSelection(event.getSelection());

                if (selectedNode == null || conflictsList == null) {
                    return;
                }

                for (String conflictPath : conflictsList) {
                    final String absPath = (appContext.getCurrentProject().getRootProject().getPath() + "/" + conflictPath.trim());

                    if (absPath.startsWith(selectedNode.getStorablePath())) {
                        enable = true;
                        break;
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        presenter.showConflictsDialog(conflictsList);
    }

    /** {@inheritDoc} */
    @Override
    public void updateProjectAction(final ActionEvent e) {
        super.updateProjectAction(e);

        e.getPresentation().setEnabled(enable);
    }

    @Nullable
    private HasStorablePath getStorableNodeFromSelection(Selection<?> selection) {
        if (selection == null) {
            return null;
        }

        return projectExplorerPresenter.getSelection().getHeadElement() instanceof HasStorablePath ? (HasStorablePath)selection.getHeadElement() : null;
    }

    private void fetchConflicts() {
        presenter.fetchConflictsList(false, new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                enable = false;
            }

            @Override
            public void onSuccess(List<String> result) {
                conflictsList = result;
            }
        });
    }
}
