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
package org.eclipse.che.plugin.svn.ide.action;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.SvnUtil;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;

/**
 * Extension of {@link ProjectAction} that all Subversion extensions will extend.
 */
public abstract class SubversionAction extends AbstractPerspectiveAction {

    protected final AppContext                               appContext;
    protected final SubversionExtensionLocalizationConstants constants;
    protected final SubversionExtensionResources             resources;
    protected final String                                   title;

    public SubversionAction(final String title,
                            final String description,
                            final SVGResource svgIcon,
                            final AppContext appContext,
                            final SubversionExtensionLocalizationConstants constants,
                            final SubversionExtensionResources resources) {
        super(singletonList(ProjectPerspective.PROJECT_PERSPECTIVE_ID), title, description, null, svgIcon);

        this.constants = constants;
        this.resources = resources;
        this.appContext = appContext;
        this.title = title;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(true);

        final Project project = appContext.getRootProject();

        event.getPresentation().setEnabled(project != null && SvnUtil.isUnderSvn(project));
    }
}
