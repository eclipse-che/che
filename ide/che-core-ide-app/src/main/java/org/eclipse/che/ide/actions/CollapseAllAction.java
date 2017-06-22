/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Action to collapse all opened nodes in Project Explorer.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CollapseAllAction extends Action implements ActivePartChangedHandler {

    private ProjectExplorerPresenter projectExplorer;

    private PartPresenter activePart;

    @Inject
    public CollapseAllAction(ProjectExplorerPresenter projectExplorer,
                             EventBus eventBus,
                             CoreLocalizationConstant localizationConstant) {
        super(localizationConstant.collapseAllActionTitle(), localizationConstant.collapseAllActionDescription());
        this.projectExplorer = projectExplorer;

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(activePart instanceof ProjectExplorerPresenter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        projectExplorer.collapseAll();
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        activePart = event.getActivePart();
    }

}
