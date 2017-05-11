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
package org.eclipse.che.ide.workspace.start;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;

import java.util.List;

@Singleton
public class StartWorkspaceAction extends Action {

    private final StartWorkspacePresenter presenter;
    private final WorkspaceServiceClient  workspaceServiceClient;

    @Inject
    public StartWorkspaceAction(StartWorkspacePresenter presenter, WorkspaceServiceClient workspaceServiceClient) {
        super("create ws...", "");

        this.presenter = presenter;
        this.workspaceServiceClient = workspaceServiceClient;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        workspaceServiceClient.getWorkspaces(0, 30).then(new Operation<List<WorkspaceDto>>() {
            @Override
            public void apply(List<WorkspaceDto> arg) throws OperationException {
                presenter.show(arg, null);
            }
        });
    }
}
