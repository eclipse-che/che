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
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.askcredentials.AskCredentialsPresenter;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.plugin.svn.shared.SubversionTypeConstant.SUBVERSION_ATTRIBUTE_REPOSITORY_URL;

/**
 * Extension of {@link SubversionAction} for changing username/password.
 */
@Singleton
public class ChangeCredentialsAction extends SubversionAction {

    private final AskCredentialsPresenter presenter;

    @Inject
    public ChangeCredentialsAction(AppContext appContext,
                                   SubversionExtensionLocalizationConstants constants,
                                   SubversionExtensionResources resources,
                                   AskCredentialsPresenter presenter) {
        super(constants.changeCredentialsTitle(), constants.changeCredentialsDescription(), resources.add(), appContext, constants,
              resources);

        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Project project = appContext.getRootProject();

        checkState(project != null, "Null project occurred");
        checkState(project.getAttributes().containsKey(SUBVERSION_ATTRIBUTE_REPOSITORY_URL), "Project doesn't have svn url property");

        final List<String> values = project.getAttributes().get(SUBVERSION_ATTRIBUTE_REPOSITORY_URL);

        checkState(!values.isEmpty(), "Project doesn't have any bound svn url");

        presenter.askCredentials(values.get(0));
    }
}
