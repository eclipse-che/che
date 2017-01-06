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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.download.DownloadContainer;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Download resource which is in context now to the local machine.
 *
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 * @see AppContext#getResource()
 */
@Singleton
public class DownloadResourceAction extends AbstractPerspectiveAction {

    private final AppContext         appContext;
    private final DownloadContainer  downloadContainer;
    private final WsAgentURLModifier urlModifier;

    @Inject
    public DownloadResourceAction(AppContext appContext,
                                  CoreLocalizationConstant locale,
                                  DownloadContainer downloadContainer,
                                  WsAgentURLModifier urlModifier) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), locale.downloadItemName(), locale.downloadItemDescription(), null, null);
        this.appContext = appContext;
        this.downloadContainer = downloadContainer;
        this.urlModifier = urlModifier;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource resource = appContext.getResource();

        checkState(resource != null, "Null resource occurred");

        downloadContainer.setUrl(urlModifier.modify(resource.getURL()));
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        final Resource[] resources = appContext.getResources();

        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(resources != null && resources.length == 1);
    }
}
