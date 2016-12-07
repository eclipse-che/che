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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;

/**
 * Presenter for the searching some text in the workspace.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class FindResultPresenter extends BasePresenter implements FindResultView.ActionDelegate,
                                                                  ResourceChangedHandler {
    private final WorkspaceAgent           workspaceAgent;
    private final CoreLocalizationConstant localizationConstant;
    private final Resources                resources;
    private final FindResultView           view;

    @Inject
    public FindResultPresenter(WorkspaceAgent workspaceAgent,
                               CoreLocalizationConstant localizationConstant,
                               Resources resources,
                               FindResultView view,
                               EventBus eventBus) {
        this.workspaceAgent = workspaceAgent;
        this.localizationConstant = localizationConstant;
        this.resources = resources;
        this.view = view;

        eventBus.addHandler(ResourceChangedEvent.getType(), this);

        view.setDelegate(this);
    }

    @Override
    public String getTitle() {
        return localizationConstant.actionFullTextSearch();
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
    public SVGResource getTitleImage() {
        return (resources.find());
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /**
     * Activate Find results part and showing all occurrences.
     *
     * @param resources
     *         list of files which contains requested text
     * @param request
     *         requested text
     */
    public void handleResponse(Resource[] resources, String request) {
        workspaceAgent.openPart(this, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(this);
        view.showResults(resources, request);
    }

    @Override
    public void onSelectionChanged(List<Node> selection) {
        setSelection(new Selection<>(selection));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();
        final Tree tree = view.getTree();

        if (delta.getKind() == REMOVED) {
            for (Node node : tree.getNodeStorage().getAll()) {
                if (node instanceof ResourceNode && ((ResourceNode)node).getData().getLocation().equals(delta.getResource().getLocation())) {
                    tree.getNodeStorage().remove(node);
                    return;
                }
            }
        }
    }
}
