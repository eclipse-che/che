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
package org.eclipse.che.ide.ext.java.testing.client.view;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.ext.java.testing.client.TestResources;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

/**
 * Presenter for the searching some text in the workspace.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class TestResultPresenter extends BasePresenter implements TestResultView.ActionDelegate {
    private final WorkspaceAgent           workspaceAgent;
    private final CoreLocalizationConstant localizationConstant;
    private final TestResources                resources;
    private final TestResultView           view;

    @Inject
    public TestResultPresenter(WorkspaceAgent workspaceAgent,
                               CoreLocalizationConstant localizationConstant,
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
        return "Test Results";
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public String getTitleToolTip() {
        return "Test Runner Results";
    }

    @Override
    public SVGResource getTitleImage() {
        return resources.TestIcon();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /**
     * Activate Find results part and showing all occurrences.
     *
     * @param response
     *         list of files which contains requested text
     * @param request
     *         requested text
     */
    public void handleResponse(TestResult response) {
        workspaceAgent.openPart(this, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(this);
        view.showResults(response);
    }
}
