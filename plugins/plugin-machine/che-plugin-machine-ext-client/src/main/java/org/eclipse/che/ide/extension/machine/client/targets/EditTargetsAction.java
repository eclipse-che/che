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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;

import javax.validation.constraints.NotNull;
import java.util.Collections;

/**
 * Action to open Targets popup.
 * The action is available only in Project perspective.
 *
 * @author Vitaliy Guliy
 */
public class EditTargetsAction extends AbstractPerspectiveAction {

    private TargetsPresenter targetsPresenter;

    @Inject
    public EditTargetsAction(MachineLocalizationConstant locale,
                             TargetsPresenter targetsPresenter) {
        super(Collections.singletonList(ProjectPerspective.PROJECT_PERSPECTIVE_ID),
                locale.editTargets(),
                locale.editTargetsDescription(),
                null, null);

        this.targetsPresenter = targetsPresenter;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabled(true);
        event.getPresentation().setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        targetsPresenter.edit();
    }

}
