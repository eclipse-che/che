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

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.askcredentials.AskCredentialsPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import java.util.List;

import static org.eclipse.che.plugin.svn.shared.SubversionTypeConstant.SUBVERSION_ATTRIBUTE_REPOSITORY_URL;

/**
 * Extension of {@link SubversionAction} for changing username/password.
 */
@Singleton
public class ChangeCredentialsAction extends SubversionAction {

    private final AskCredentialsPresenter presenter;

    @Inject
    public ChangeCredentialsAction(final AppContext appContext,
                                   final ProjectExplorerPresenter projectExplorerPresenter,
                                   final SubversionExtensionLocalizationConstants constants,
                                   final SubversionExtensionResources resources,
                                   final AskCredentialsPresenter presenter) {
        super(constants.changeCredentialsTitle(), constants.changeCredentialsDescription(), resources.add(), appContext, constants,
              resources, projectExplorerPresenter);

        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            final ProjectConfigDto rootProjectDescriptor = currentProject.getRootProject();
            if (rootProjectDescriptor != null) {
                String repositoryUrl = null;
                final List<String> attributeValues = rootProjectDescriptor.getAttributes().get(SUBVERSION_ATTRIBUTE_REPOSITORY_URL);
                if (attributeValues != null && !attributeValues.isEmpty()) {
                    repositoryUrl = attributeValues.get(0);
                }

                presenter.askCredentials(repositoryUrl);
            }
        }
    }
}
