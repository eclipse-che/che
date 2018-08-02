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

import static java.util.Collections.emptySet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Implementation for FindResult view. Uses tree for presenting search results.
 *
 * @author Valeriy Svydenko
 */
@Singleton
class FindResultViewImpl extends BaseView<FindResultView.ActionDelegate> implements FindResultView {

  private final Tree tree;
  private final FindResultNodeFactory findResultNodeFactory;
  @UiField FlowPanel paginationPanel;
  @UiField Button nextBtn;
  @UiField Button previousBtn;
  @UiField Label resultLabel;
  @UiField Label requestedLabel;

  @Inject
  public FindResultViewImpl(
      FindResultViewImplUiBinder uiBinder,
      FindResultNodeFactory findResultNodeFactory,
      CoreLocalizationConstant localizationConstant) {
    NodeStorage nodeStorage = new NodeStorage();
    NodeLoader loader = new NodeLoader(emptySet());
    tree = new Tree(nodeStorage, loader);

    Widget contentWidget = uiBinder.createAndBindUi(this);
    setContentWidget(contentWidget);

    setTitle(localizationConstant.actionFullTextSearch());
    this.findResultNodeFactory = findResultNodeFactory;

    nextBtn.setHTML("<i class=\"fa fa-angle-right\" aria-hidden=\"true\"></i>");
    previousBtn.setHTML("<i class=\"fa fa-angle-left\" aria-hidden=\"true\"></i>");

    // do not remove debug id; it's needed for selenium tests
    tree.ensureDebugId("result-search-tree");
    ensureDebugId("find-info-panel");

    DockLayoutPanel dockLayoutPanel = (DockLayoutPanel) contentWidget;
    dockLayoutPanel.add(tree);

    tree.getSelectionModel()
        .addSelectionChangedHandler(event -> delegate.onSelectionChanged(event.getSelection()));

    tree.setAutoSelect(true);
  }

  /** {@inheritDoc} */
  @Override
  protected void focusView() {
    tree.setFocus(true);
  }

  @Override
  public void setPreviousBtnActive(boolean enable) {
    previousBtn.setEnabled(enable);
  }

  @Override
  public void setNextBtnActive(boolean enable) {
    nextBtn.setEnabled(enable);
  }

  /** {@inheritDoc} */
  @Override
  public void showResults(SearchResult result, String request) {
    StringBuilder resultTitle = new StringBuilder();
    List<SearchItemReference> resources = result.getItemReferences();
    if (resources.isEmpty()) {
      resultTitle.append("No results found for ");
      resultLabel.setText(resultTitle.toString());
      requestedLabel.setText("\'" + request + "\'");
      tree.getNodeStorage().clear();
      return;
    }

    requestedLabel.setText("");

    int total = 0;
    for (SearchItemReference searchItemReference : resources) {
      total += searchItemReference.getOccurrences().size();
    }
    resultTitle.append(total).append(" occurrence");
    if (total > 1) {
      resultTitle.append('s');
    }
    resultTitle.append(" found in ").append(resources.size()).append(" file");
    if (resources.size() > 1) {
      resultTitle.append('s');
    }
    resultTitle.append(" (per page results) for '");
    resultTitle.append(request);
    resultTitle.append("'. Total file count - ");
    resultTitle.append(result.getTotalHits());

    resultLabel.setText(resultTitle.toString());

    tree.getNodeStorage().clear();
    for (SearchItemReference item : resources) {
      tree.getNodeStorage().add(findResultNodeFactory.newFoundItemNode(item, request));
    }
    Node rootNode = tree.getRootNodes().get(0);

    tree.getSelectionModel().select(rootNode, false);
    focusView();
  }

  @Override
  public Tree getTree() {
    return tree;
  }

  @SuppressWarnings("unused")
  @UiHandler("nextBtn")
  public void nextBtnClick(ClickEvent event) {
    delegate.onNextButtonClicked();
  }

  @SuppressWarnings("unused")
  @UiHandler("previousBtn")
  public void previousBtnClick(ClickEvent event) {
    delegate.onPreviousButtonClicked();
  }

  interface FindResultViewImplUiBinder extends UiBinder<Widget, FindResultViewImpl> {}
}
