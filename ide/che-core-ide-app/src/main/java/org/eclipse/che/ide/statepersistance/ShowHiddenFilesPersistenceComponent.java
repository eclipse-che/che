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
package org.eclipse.che.ide.statepersistance;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.actions.ShowHiddenFilesAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.api.statepersistance.dto.ActionDescriptor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.ide.actions.ShowHiddenFilesAction.SHOW_HIDDEN_FILES_PARAM_ID;

/**
 * Component provides sequence of actions which should be performed
 * in order to restore hidden files visibility in project explorer.
 *
 * @author Andrienko Alexander
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ShowHiddenFilesPersistenceComponent implements PersistenceComponent {

    private final ActionManager            actionManager;
    private final ShowHiddenFilesAction    showHiddenFilesAction;
    private final DtoFactory               dtoFactory;
    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public ShowHiddenFilesPersistenceComponent(ActionManager actionManager,
                                               ShowHiddenFilesAction showHiddenFilesAction,
                                               DtoFactory dtoFactory,
                                               ProjectExplorerPresenter projectExplorer) {
        this.actionManager = actionManager;
        this.showHiddenFilesAction = showHiddenFilesAction;
        this.dtoFactory = dtoFactory;
        this.projectExplorer = projectExplorer;
    }

    @Override
    public List<ActionDescriptor> getActions() {
        List<ActionDescriptor> actions = new ArrayList<>();

        actions.add(dtoFactory.createDto(ActionDescriptor.class)
                              .withId(actionManager.getId(showHiddenFilesAction))
                              .withParameters(singletonMap(SHOW_HIDDEN_FILES_PARAM_ID,
                                                           String.valueOf(projectExplorer.isShowHiddenFiles()))));

        return actions;
    }
}
