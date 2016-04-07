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

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.projecttype.wizard.presenter.ProjectWizardPresenter;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The special action which allows call business logic which can convert folder to project.
 *
 * @author Dmitry Shnurenko
 */
public class ConvertFolderToProjectAction extends AbstractPerspectiveAction {
    private final SelectionAgent         selectionAgent;
    private final ProjectWizardPresenter projectConfigWizard;
    private final DtoFactory             dtoFactory;

    @Inject
    public ConvertFolderToProjectAction(CoreLocalizationConstant locale,
                                        SelectionAgent selectionAgent,
                                        ProjectWizardPresenter projectConfigWizard,
                                        DtoFactory dtoFactory) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID),
              locale.actionConvertFolderToProject(),
              locale.actionConvertFolderToProjectDescription(),
              null,
              null);
        this.selectionAgent = selectionAgent;
        this.projectConfigWizard = projectConfigWizard;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        boolean isVisible = getSelectedItem() != null;

        event.getPresentation().setEnabledAndVisible(isVisible);
    }

    private ItemReference getSelectedItem() {
        Selection<?> selection = selectionAgent.getSelection();
        if (!selection.isEmpty() && selection.getHeadElement() instanceof FolderReferenceNode) {
            return ((FolderReferenceNode)selection.getHeadElement()).getData();
        }

        return null;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        ItemReference itemReference = getSelectedItem();
        ProjectConfigDto configDto = dtoFactory.createDto(ProjectConfigDto.class)
                                               .withPath(itemReference.getPath())
                                               .withName(itemReference.getName())
                                               .withLinks(itemReference.getLinks())
                                               .withType(itemReference.getType());

        projectConfigWizard.show(configDto, UPDATE);
    }
}
