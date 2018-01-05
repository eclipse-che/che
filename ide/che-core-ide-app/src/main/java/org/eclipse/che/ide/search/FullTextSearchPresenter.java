/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.search;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;

/**
 * Presenter for full text search.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 * @author Roman Nikitenko
 */
@Singleton
public class FullTextSearchPresenter implements FullTextSearchView.ActionDelegate {
  public static final int SEARCH_RESULT_ITEMS = 30;

  private static final String AND_OPERATOR = "AND";

  private final FullTextSearchView view;
  private final FindResultPresenter findResultPresenter;
  private final AppContext appContext;
  private Path defaultStartPoint;

  @Inject
  public FullTextSearchPresenter(
      FullTextSearchView view, FindResultPresenter findResultPresenter, AppContext appContext) {
    this.view = view;
    this.findResultPresenter = findResultPresenter;
    this.appContext = appContext;

    this.view.setDelegate(this);
  }

  /** Show dialog with view for searching. */
  public void showDialog(Path path) {
    this.defaultStartPoint = path;

    view.showDialog();
    view.clearInput();
    view.setPathDirectory(path.toString());
  }

  @Override
  public void search(final String text) {
    final Path startPoint =
        isNullOrEmpty(view.getPathToSearch())
            ? defaultStartPoint
            : Path.valueOf(view.getPathToSearch());

    appContext
        .getWorkspaceRoot()
        .getContainer(startPoint)
        .then(
            optionalContainer -> {
              if (!optionalContainer.isPresent()) {
                view.showErrorMessage("Path '" + startPoint + "' doesn't exists");
                return;
              }

              final Container container = optionalContainer.get();
              QueryExpression queryExpression =
                  container.createSearchQueryExpression(view.getFileMask(), prepareQuery(text));
              queryExpression.setMaxItems(SEARCH_RESULT_ITEMS);
              container
                  .search(queryExpression)
                  .then(
                      result -> {
                        view.close();
                        findResultPresenter.handleResponse(result, queryExpression, text);
                      });
            });
  }

  // todo move this to the core part of resource manager
  private String prepareQuery(String text) {
    StringBuilder sb = new StringBuilder();
    for (char character : text.toCharArray()) {
      if (character == '\\'
          || character == '+'
          || character == '-'
          || character == '!'
          || character == '('
          || character == ')'
          || character == ':'
          || character == '^'
          || character == '['
          || character == ']'
          || character == '\"'
          || character == '{'
          || character == '}'
          || character == '~'
          || character == '*'
          || character == '?'
          || character == '|'
          || character == '&'
          || character == '/') {
        sb.append("\\");
      }
      sb.append(character);
    }
    String escapedText;
    if (view.isWholeWordsOnly()) {
      escapedText = sb.toString();
    } else {
      sb.append('*');
      escapedText = '*' + sb.toString();
    }

    String[] items = escapedText.trim().split("\\s+");
    int numberItem = items.length;
    if (numberItem == 1) {
      return items[0];
    }

    String lastItem = items[numberItem - 1];
    sb = new StringBuilder();
    sb.append('"');
    sb.append(escapedText.substring(0, escapedText.lastIndexOf(lastItem)));
    sb.append("\" " + AND_OPERATOR + " ");
    sb.append(lastItem);
    return sb.toString();
  }

  @Override
  public void setPathDirectory(String path) {
    view.setPathDirectory(path);
  }

  @Override
  public void setFocus() {
    view.setFocus();
  }

  @Override
  public void onEnterClicked() {
    if (view.isCancelButtonInFocus()) {
      view.close();
      return;
    }

    if (view.isSelectPathButtonInFocus()) {
      view.showSelectPathDialog();
      return;
    }

    // start search if Enter is pressed anywhere else in the dialog
    String searchText = view.getSearchText();
    if (!searchText.isEmpty()) {
      search(searchText);
    }
  }
}
