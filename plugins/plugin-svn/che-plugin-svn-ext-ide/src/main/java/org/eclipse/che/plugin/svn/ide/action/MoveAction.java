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

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.move.MovePresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Extension of {@link SubversionAction} for implementing the "svn move" command.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class MoveAction extends SubversionAction {

    private MovePresenter presenter;

    @Inject
    public MoveAction(AppContext appContext,
                      SubversionExtensionLocalizationConstants constants,
                      SubversionExtensionResources resources,
                      final ProjectExplorerPresenter projectExplorerPresenter,
                      MovePresenter presenter) {
        super(constants.moveActionTitle(), constants.moveActionDescription(), resources.move(), appContext, constants,
              resources, projectExplorerPresenter);
        this.presenter = presenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        presenter.showMove();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isSelectionRequired() {
        return true;
    }
}
