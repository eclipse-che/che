/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.client.wizard;

import org.eclipse.che.ide.api.project.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.extension.ant.shared.AntAttributes;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringMapListUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.PROJECT_PATH_KEY;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.ide.extension.ant.shared.AntAttributes.ANT_ID;

/**
 * Wizard page for Ant project.
 *
 * @author Vladyslav Zhukovskii
 * @author Artem Zatsarynnyi
 */
public class AntPagePresenter extends AbstractWizardPage<ImportProject> implements AntPageView.ActionDelegate {

    private final AntPageView          view;
    private final ProjectServiceClient projectServiceClient;

    /** Create instance of {@link AntPagePresenter}. */
    @Inject
    public AntPagePresenter(AntPageView view, ProjectServiceClient projectServiceClient) {
        super();
        this.view = view;
        view.setDelegate(this);
        this.projectServiceClient = projectServiceClient;
    }

    @Override
    public void init(ImportProject dataObject) {
        super.init(dataObject);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        if (CREATE == wizardMode) {
            // set default values
            Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
            attributes.put(AntAttributes.SOURCE_FOLDER, Arrays.asList(AntAttributes.DEF_SRC_PATH));
            attributes.put(AntAttributes.TEST_SOURCE_FOLDER, Arrays.asList(AntAttributes.DEF_TEST_SRC_PATH));
            return;
        }

        if (UPDATE == wizardMode) {
            projectServiceClient.estimateProject(context.get(PROJECT_PATH_KEY), ANT_ID, getEstimateProjectCallback());
        }
    }

    public AsyncRequestCallback<Map<String, List<String>>> getEstimateProjectCallback() {
        return new AsyncRequestCallback<Map<String, List<String>>>(new StringMapListUnmarshaller()) {
            @Override
            protected void onSuccess(Map<String, List<String>> result) {
                List<String> srcFolder = result.get(AntAttributes.SOURCE_FOLDER);
                srcFolder = srcFolder != null && !srcFolder.isEmpty() ? srcFolder : Arrays.asList(AntAttributes.DEF_SRC_PATH);
                setAttribute(AntAttributes.SOURCE_FOLDER, srcFolder);


                List<String> testSrcFolder = result.get(AntAttributes.TEST_SOURCE_FOLDER);
                testSrcFolder = testSrcFolder != null && !testSrcFolder.isEmpty() ? testSrcFolder : Arrays.asList(AntAttributes.DEF_TEST_SRC_PATH);
                setAttribute(AntAttributes.TEST_SOURCE_FOLDER, testSrcFolder);
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(getClass(), exception.getMessage());
            }
        };
    }


    /** {@inheritDoc} */
    @Override
    public boolean canSkip() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** Sets single value of attribute of data-object. */
    private void setAttribute(String attrId, List<String> value) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(attrId, value);
    }
}
