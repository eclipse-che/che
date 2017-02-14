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
package org.eclipse.che.ide.resources.action;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Scrolls from resource in the context to the stored location in the Project View.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
@Singleton
public class RevealResourceAction extends AbstractPerspectiveAction {

    private final AppContext               appContext;
    private final EventBus eventBus;

    @Inject
    public RevealResourceAction(AppContext appContext,
                                EventBus eventBus) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), "Reveal Resource", null, null, null);
        this.appContext = appContext;
        this.eventBus = eventBus;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final Resource[] resources = appContext.getResources();

        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(resources != null && resources.length == 1);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource[] resources = appContext.getResources();

        checkState(resources != null && resources.length == 1);

        eventBus.fireEvent(new RevealResourceEvent(resources[0]));
    }
}
