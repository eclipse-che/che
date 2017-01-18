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

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Refresh current selected container.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class RefreshPathAction extends AbstractPerspectiveAction {

    private final AppContext appContext;

    @Inject
    public RefreshPathAction(AppContext appContext) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), "Refresh", "Refresh path", null, null);
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setText("Refresh");
        event.getPresentation().setVisible(true);

        final Resource[] resources = appContext.getResources();

        if (resources == null || resources.length != 1) {
            event.getPresentation().setEnabled(false);
            return;
        }

        final Resource resource = resources[0];

        if (resource instanceof Container) {
            event.getPresentation().setText("Refresh '" + resource.getName() + "'");
        } else {
            final Container parent = resource.getParent();

            if (parent != null) {
                event.getPresentation().setText("Refresh '" + parent.getName() + "'");
            } else {
                event.getPresentation().setEnabled(false);
                return;
            }
        }

        event.getPresentation().setEnabled(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource[] resources = appContext.getResources();

        if (resources == null || resources.length != 1) {
            return;
        }

        final Resource resource = resources[0];

        if (resource instanceof Container) {
            ((Container)resource).synchronize();
        } else {
            final Container parent = resource.getParent();

            if (parent != null) {
                parent.synchronize();
            }
        }
    }
}
