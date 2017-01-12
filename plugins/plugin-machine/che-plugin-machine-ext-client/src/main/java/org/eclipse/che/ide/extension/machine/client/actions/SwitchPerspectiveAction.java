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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.ui.radiobuttongroup.RadioButtonGroup;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for switching Project/Machine perspectives.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class SwitchPerspectiveAction extends Action implements CustomComponentAction {

    private final PerspectiveManager          perspectiveManager;
    private final MachineLocalizationConstant localizationConstant;
    private final MachineResources            resources;

    private final RadioButtonGroup radioButtonGroup;

    @Inject
    public SwitchPerspectiveAction(PerspectiveManager perspectiveManager,
                                   MachineLocalizationConstant localizationConstant,
                                   MachineResources resources,
                                   RadioButtonGroup radioButtonGroup) {
        super();
        this.perspectiveManager = perspectiveManager;
        this.localizationConstant = localizationConstant;
        this.resources = resources;
        this.radioButtonGroup = radioButtonGroup;

        createButtons();
    }

    private void createButtons() {
        radioButtonGroup.addButton("", localizationConstant.perspectiveProjectActionTooltip(), resources.projectPerspective(),
                                   new ClickHandler() {
                                       @Override
                                       public void onClick(ClickEvent event) {
                                           perspectiveManager.setPerspectiveId(PROJECT_PERSPECTIVE_ID);
                                       }
                                   });

        radioButtonGroup.selectButton(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        return radioButtonGroup;
    }
}
