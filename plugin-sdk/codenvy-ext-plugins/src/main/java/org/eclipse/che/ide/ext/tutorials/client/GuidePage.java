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
package org.eclipse.che.ide.ext.tutorials.client;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.parts.AbstractPartPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;

import static org.eclipse.che.ide.ext.tutorials.client.TutorialsExtension.DEFAULT_GUIDE_FILE_NAME;

/**
 * Displays a page that contains a tutorial guide.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class GuidePage extends AbstractPartPresenter {

    private AppContext           appContext;
    private TutorialsResources   resources;
    private ProjectServiceClient projectServiceClient;

    @Inject
    public GuidePage(AppContext appContext, TutorialsResources resources, ProjectServiceClient projectServiceClient) {
        this.appContext = appContext;
        this.resources = resources;
        this.projectServiceClient = projectServiceClient;
    }

    /** {@inheritDoc} */
    @Override
    public void go(final AcceptsOneWidget container) {
        CurrentProject activeProject = appContext.getCurrentProject();
        if (activeProject != null) {
            projectServiceClient.getFileContent(activeProject.getRootProject().getPath() + '/' + DEFAULT_GUIDE_FILE_NAME,
                                                new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                                    @Override
                                                    protected void onSuccess(String result) {
                                                        final HTMLPanel htmlPanel = new HTMLPanel("");
                                                        container.setWidget(htmlPanel);
                                                        htmlPanel.getElement().setInnerHTML(result);
                                                        htmlPanel.setStyleName(resources.tutorialsCss().scrollPanel());
                                                    }

                                                    @Override
                                                    protected void onFailure(Throwable exception) {
                                                        Log.error(GuidePage.class, exception);
                                                    }
                                                });
        }
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getTitle() {
        return "Tutorial Guide";
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getTitleImage() {
        return resources.guide();
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getTitleSVGImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return "This view displays a tutorial guide";
    }
}
