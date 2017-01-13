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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for editing commands.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class EditCommandsAction extends AbstractPerspectiveAction {

    private final EditCommandsPresenter editCommandsPresenter;

    @Inject
    public EditCommandsAction(EditCommandsPresenter editCommandsPresenter,
                              MachineLocalizationConstant localizationConstant,
                              MachineResources resources) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstant.editCommandsControlTitle(),
              localizationConstant.editCommandsControlDescription(),
              null,
              resources.editCommands());

        this.editCommandsPresenter = editCommandsPresenter;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editCommandsPresenter.show();
    }

}
