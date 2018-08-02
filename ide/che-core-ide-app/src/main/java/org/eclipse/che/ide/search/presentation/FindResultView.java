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

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * View for the result of search.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(FindResultViewImpl.class)
public interface FindResultView extends View<FindResultView.ActionDelegate> {
  /**
   * Sets whether this panel is visible.
   *
   * @param visible visible - true to show the object, false to hide it
   */
  void setVisible(boolean visible);

  /**
   * Sets whether next result button is enable.
   *
   * @param enable visible - true to enable the button, false to disable it
   */
  void setNextBtnActive(boolean enable);

  /**
   * Sets whether previous result button is enable.
   *
   * @param enable visible - true to enable the button, false to disable it
   */
  void setPreviousBtnActive(boolean enable);

  /**
   * Activate Find results part and showing all occurrences.
   *
   * @param result search result of requested text
   * @param request requested text
   */
  void showResults(SearchResult result, String request);

  Tree getTree();

  interface ActionDelegate extends BaseActionDelegate {
    void onSelectionChanged(List<Node> selection);

    void onNextButtonClicked();

    void onPreviousButtonClicked();
  }
}
