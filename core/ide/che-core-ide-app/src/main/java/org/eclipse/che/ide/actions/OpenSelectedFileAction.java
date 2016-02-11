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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class OpenSelectedFileAction extends Action {

    private final AppContext               appContext;
    private final ProjectExplorerPresenter projectExplorer;
    private final EventBus                 eventBus;
    private final AnalyticsEventLogger     eventLogger;

    @Inject
    public OpenSelectedFileAction(AppContext appContext,
                                  ProjectExplorerPresenter projectExplorer,
                                  EventBus eventBus,
                                  AnalyticsEventLogger eventLogger,
                                  Resources resources) {
        super("Open", null, null, resources.defaultFile());
        this.appContext = appContext;
        this.projectExplorer = projectExplorer;
        this.eventBus = eventBus;
        this.eventLogger = eventLogger;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        Selection<?> selection = projectExplorer.getSelection();
        Object headElement = selection.getHeadElement();

        if (headElement instanceof VirtualFile) {
            eventBus.fireEvent(new FileEvent((VirtualFile)headElement, FileEvent.FileOperation.OPEN));
        }
    }

    @Override
    public void update(ActionEvent e) {
        if (appContext.getCurrentProject() == null) {
            e.getPresentation().setVisible(false);
            return;
        }

        Selection<?> selection = projectExplorer.getSelection();
        e.getPresentation().setVisible(selection.getAllElements().size() == 1 && selection.getHeadElement() instanceof VirtualFile);
    }
}
