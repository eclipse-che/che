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
package org.eclipse.che.ide.ext.web.html;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.web.WebLocalizationConstant;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for previewing an HTML page.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class PreviewAction extends AbstractPerspectiveAction {

    private final WsAgentURLModifier wsAgentURLModifier;
    private final AppContext         appContext;

    @Inject
    public PreviewAction(WsAgentURLModifier wsAgentURLModifier,
                         AppContext appContext,
                         WebLocalizationConstant localizationConstants) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstants.previewHTMLActionTitle(),
              localizationConstants.previewHTMLActionDescription(),
              null,
              null);
        this.wsAgentURLModifier = wsAgentURLModifier;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(ActionEvent e) {
        e.getPresentation().setVisible(true);

        final Resource[] resources = appContext.getResources();
        if (resources != null && resources.length == 1) {
            final Resource selectedResource = resources[0];
            if (Resource.FILE == selectedResource.getResourceType()) {
                final String fileExtension = ((File)selectedResource).getExtension();
                if ("html".equals(fileExtension)) {
                    e.getPresentation().setEnabled(true);
                    return;
                }
            }
        }

        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource selectedResource = appContext.getResource();
        if (Resource.FILE == selectedResource.getResourceType()) {
            final String contentUrl = ((File)selectedResource).getContentUrl();

            Window.open(wsAgentURLModifier.modify(contentUrl), "_blank", null);
        }
    }
}
