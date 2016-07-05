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
package org.eclipse.che.ide.extension.machine.client.actions;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.extension.machine.client.perspective.OperationsPerspective.OPERATIONS_PERSPECTIVE_ID;

/**
 * @author Yevhenii Voevodin
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CreateSnapshotAction extends AbstractPerspectiveAction {

    private final WorkspaceSnapshotCreator snapshotCreator;
    private final AppContext               appContext;

    @Inject
    public CreateSnapshotAction(CoreLocalizationConstant locale, WorkspaceSnapshotCreator snapshotCreator, AppContext appContext) {
        super(singletonList(OPERATIONS_PERSPECTIVE_ID), locale.createSnapshotTitle(), locale.createSnapshotDescription(), null, null);
        this.snapshotCreator = snapshotCreator;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabled(!snapshotCreator.isInProgress());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        snapshotCreator.createSnapshot(appContext.getWorkspace().getId());
    }
}
