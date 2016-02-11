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
package org.eclipse.che.ide.search.presentation;

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
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

/**
 * Presenter for the searching some text in the workspace.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class FindResultPresenter extends BasePresenter implements FindResultView.ActionDelegate {
    private final WorkspaceAgent           workspaceAgent;
    private final CoreLocalizationConstant localizationConstant;
    private final Resources                resources;
    private final FindResultView           view;

    @Inject
    public FindResultPresenter(WorkspaceAgent workspaceAgent,
                               CoreLocalizationConstant localizationConstant,
                               Resources resources,
                               FindResultView view) {
        this.workspaceAgent = workspaceAgent;
        this.localizationConstant = localizationConstant;
        this.resources = resources;
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public String getTitle() {
        return localizationConstant.actionFullTextSearch();
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
        return localizationConstant.actionFullTextSearchDescription();
    }

    @Override
    public SVGResource getTitleSVGImage() {
        return (resources.find());
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
    public void handleResponse(List<ItemReference> response, String request) {
        workspaceAgent.openPart(this, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(this);
        view.showResults(response, request);
    }
}
