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
package org.eclipse.che.ide.ext.java.testing.core.client.view;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.ext.java.testing.core.client.TestLocalizationConstant;
import org.eclipse.che.ide.ext.java.testing.core.client.TestResources;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for the displaying the test results on the workspace.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestResultPresenter extends BasePresenter implements TestResultView.ActionDelegate {
    private final WorkspaceAgent           workspaceAgent;
    private final TestLocalizationConstant localizationConstant;
    private final TestResources                resources;
    private final TestResultView           view;

    @Inject
    public TestResultPresenter(WorkspaceAgent workspaceAgent,
                               TestLocalizationConstant localizationConstant,
                               TestResources resources,
                               TestResultView view) {
        this.workspaceAgent = workspaceAgent;
        this.localizationConstant = localizationConstant;
        this.resources = resources;
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public String getTitle() {
        return localizationConstant.titleTestResultPresenter();
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstant.titleTestResultPresenterToolTip();
    }

    @Override
    public SVGResource getTitleImage() {
        return resources.testIcon();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /**
     * Activate Test results part and showing the test results.
     *
     * @param response result of the test runner
     */
    public void handleResponse(TestResult response) {
        workspaceAgent.openPart(this, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(this);
        view.showResults(response);
    }
}
