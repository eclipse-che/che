/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.search.presentation;

import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.search.FullTextSearchPresenter.SEARCH_RESULT_ITEMS;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.project.ProjectServiceClient;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for the searching some text in the workspace.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class FindResultPresenter extends BasePresenter
    implements FindResultView.ActionDelegate, ResourceChangedHandler {
  private final WorkspaceAgent workspaceAgent;
  private ProjectServiceClient projectServiceClient;
  private final CoreLocalizationConstant localizationConstant;
  private final Resources resources;
  private final FindResultView view;

  private int skipCount = 0;
  private QueryExpression queryExpression;
  private String requestedString;

  @Inject
  public FindResultPresenter(
      WorkspaceAgent workspaceAgent,
      ProjectServiceClient projectServiceClient,
      CoreLocalizationConstant localizationConstant,
      Resources resources,
      FindResultView view,
      EventBus eventBus) {
    this.workspaceAgent = workspaceAgent;
    this.projectServiceClient = projectServiceClient;
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
   * @param result search result of requested text
   * @param request requested text
   */
  public void handleResponse(SearchResult result, QueryExpression queryExpression, String request) {
    this.queryExpression = queryExpression;
    this.requestedString = request;
    workspaceAgent.openPart(this, PartStackType.INFORMATION);
    workspaceAgent.setActivePart(this);

    view.setPreviousBtnActive(false);
    view.setNextBtnActive(result.getItemReferences().size() == SEARCH_RESULT_ITEMS);
    view.showResults(result, request);
  }

  @Override
  public void onSelectionChanged(List<Node> selection) {
    setSelection(new Selection<>(selection));
  }

  @Override
  public void onNextButtonClicked() {
    queryExpression.setSkipCount(skipCount + SEARCH_RESULT_ITEMS);
    projectServiceClient
        .search(queryExpression)
        .then(
            result -> {
              List<SearchItemReference> itemReferences = result.getItemReferences();
              skipCount += itemReferences.size();
              view.setPreviousBtnActive(true);
              if (itemReferences.isEmpty()) {
                view.setNextBtnActive(false);
                return;
              }
              if (itemReferences.size() % SEARCH_RESULT_ITEMS == 0) {
                view.setNextBtnActive(true);
              } else {
                skipCount += SEARCH_RESULT_ITEMS;
                view.setNextBtnActive(false);
              }
              view.showResults(result, requestedString);
            });
  }

  @Override
  public void onPreviousButtonClicked() {
    skipCount -= skipCount % SEARCH_RESULT_ITEMS + SEARCH_RESULT_ITEMS;
    queryExpression.setSkipCount(skipCount);
    projectServiceClient
        .search(queryExpression)
        .then(
            result -> {
              List<SearchItemReference> itemReferences = result.getItemReferences();
              view.setNextBtnActive(true);
              boolean hasPreviousResults =
                  itemReferences.size() % SEARCH_RESULT_ITEMS == 0 && skipCount != 0;
              view.setPreviousBtnActive(hasPreviousResults);
              view.showResults(result, requestedString);
            });
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onResourceChanged(ResourceChangedEvent event) {
    final ResourceDelta delta = event.getDelta();
    final Tree tree = view.getTree();

    if (delta.getKind() == REMOVED) {
      for (Node node : tree.getNodeStorage().getAll()) {
        if (node instanceof ResourceNode
            && ((ResourceNode) node)
                .getData()
                .getLocation()
                .equals(delta.getResource().getLocation())) {
          tree.getNodeStorage().remove(node);
          return;
        }
      }
    }
  }
}
