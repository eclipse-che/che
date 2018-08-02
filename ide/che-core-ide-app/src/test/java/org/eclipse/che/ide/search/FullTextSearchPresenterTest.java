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
package org.eclipse.che.ide.search;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Tests for {@link FullTextSearchPresenter}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class FullTextSearchPresenterTest {
  private final String SEARCHED_TEXT = "to be or not to be";

  @Mock private FullTextSearchView view;
  @Mock private FindResultPresenter findResultPresenter;
  @Mock private AppContext appContext;
  @Mock private Container workspaceRoot;
  @Mock private Container searchContainer;
  @Mock private SearchResult searchResult;
  @Mock private QueryExpression queryExpression;
  @Mock private Promise<Optional<Container>> optionalContainerPromise;
  @Captor private ArgumentCaptor<Operation<Optional<Container>>> optionalContainerCaptor;
  @Mock private Promise<SearchResult> searchResultPromise;
  @Captor private ArgumentCaptor<Operation<SearchResult>> searchResultCaptor;
  @Captor private ArgumentCaptor<Operation<PromiseError>> operationErrorCapture;
  @Captor private ArgumentCaptor<Operation<List<SearchItemReference>>> operationSuccessCapture;

  FullTextSearchPresenter fullTextSearchPresenter;

  @Before
  public void setUp() throws Exception {
    fullTextSearchPresenter = new FullTextSearchPresenter(view, findResultPresenter, appContext);
  }

  @Test
  public void viewShouldBeShowed() {
    final Path path = Path.valueOf("/search");
    fullTextSearchPresenter.showDialog(path);

    verify(view).showDialog();
    verify(view).clearInput();
    verify(view).setPathDirectory(eq(path.toString()));
  }

  @Test
  public void searchShouldBeSuccessfullyFinished() throws Exception {
    when(view.getPathToSearch()).thenReturn("/search");
    when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    when(workspaceRoot.getContainer(nullable(Path.class))).thenReturn(optionalContainerPromise);
    when(searchContainer.search(nullable(QueryExpression.class))).thenReturn(searchResultPromise);
    when(searchContainer.createSearchQueryExpression(
            nullable(String.class), nullable(String.class)))
        .thenReturn(queryExpression);
    fullTextSearchPresenter.search(SEARCHED_TEXT);

    verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    optionalContainerCaptor.getValue().apply(Optional.of(searchContainer));

    verify(searchResultPromise).then(searchResultCaptor.capture());
    searchResultCaptor.getValue().apply(searchResult);

    verify(view, never()).showErrorMessage(anyString());
    verify(view).close();
    verify(findResultPresenter)
        .handleResponse(eq(searchResult), eq(queryExpression), eq(SEARCHED_TEXT));
  }

  @Test
  public void searchWholeWordUnSelect() throws Exception {
    when(view.getPathToSearch()).thenReturn("/search");
    when(view.isWholeWordsOnly()).thenReturn(false);
    when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    when(workspaceRoot.getContainer(nullable(Path.class))).thenReturn(optionalContainerPromise);
    when(searchContainer.search(nullable(QueryExpression.class))).thenReturn(searchResultPromise);
    when(searchContainer.createSearchQueryExpression(
            nullable(String.class), nullable(String.class)))
        .thenReturn(queryExpression);

    final String search = NameGenerator.generate("test", 10);
    fullTextSearchPresenter.search(search);

    verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    optionalContainerCaptor.getValue().apply(Optional.of(searchContainer));

    verify(searchResultPromise).then(searchResultCaptor.capture());
    searchResultCaptor.getValue().apply(searchResult);

    verify(searchContainer).search(queryExpression);
    verify(view).isWholeWordsOnly();
    verify(view, never()).showErrorMessage(nullable(String.class));
    verify(view).close();
    verify(findResultPresenter).handleResponse(eq(searchResult), eq(queryExpression), eq(search));
  }

  @Test
  public void searchWholeWordSelect() throws Exception {
    when(view.getPathToSearch()).thenReturn("/search");
    when(view.isWholeWordsOnly()).thenReturn(true);
    when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    when(workspaceRoot.getContainer(nullable(Path.class))).thenReturn(optionalContainerPromise);
    when(searchContainer.search(nullable(QueryExpression.class))).thenReturn(searchResultPromise);
    when(searchContainer.createSearchQueryExpression(
            nullable(String.class), nullable(String.class)))
        .thenReturn(queryExpression);

    final String search = NameGenerator.generate("test", 10);
    fullTextSearchPresenter.search(search);

    verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    optionalContainerCaptor.getValue().apply(Optional.of(searchContainer));

    verify(searchResultPromise).then(searchResultCaptor.capture());
    searchResultCaptor.getValue().apply(searchResult);

    verify(searchContainer).search(queryExpression);
    verify(view).isWholeWordsOnly();
    verify(view, never()).showErrorMessage(anyString());
    verify(view).close();
    verify(findResultPresenter).handleResponse(eq(searchResult), eq(queryExpression), eq(search));
  }

  @Test
  public void searchHasDoneWithSomeError() throws Exception {
    when(view.getPathToSearch()).thenReturn("/search");
    when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    when(workspaceRoot.getContainer(nullable(Path.class))).thenReturn(optionalContainerPromise);

    fullTextSearchPresenter.search(SEARCHED_TEXT);

    verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    optionalContainerCaptor.getValue().apply(Optional.absent());

    verify(view).showErrorMessage(anyString());
    verify(view, never()).close();
    verify(findResultPresenter, never())
        .handleResponse(anyObject(), eq(queryExpression), anyString());
  }

  @Test
  public void onEnterClickedWhenAcceptButtonInFocus() throws Exception {
    when(view.getSearchText()).thenReturn(SEARCHED_TEXT);
    when(view.isAcceptButtonInFocus()).thenReturn(true);

    when(view.getPathToSearch()).thenReturn("/search");
    when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
    when(workspaceRoot.getContainer(nullable(Path.class))).thenReturn(optionalContainerPromise);
    when(searchContainer.search(nullable(QueryExpression.class))).thenReturn(searchResultPromise);
    when(searchContainer.createSearchQueryExpression(
            nullable(String.class), nullable(String.class)))
        .thenReturn(queryExpression);
    List<SearchItemReference> result = Collections.emptyList();

    fullTextSearchPresenter.onEnterClicked();

    verify(optionalContainerPromise).then(optionalContainerCaptor.capture());
    optionalContainerCaptor.getValue().apply(Optional.of(searchContainer));

    verify(searchResultPromise).then(searchResultCaptor.capture());
    searchResultCaptor.getValue().apply(searchResult);

    verify(view, never()).showErrorMessage(anyString());
    verify(view).close();
    verify(findResultPresenter)
        .handleResponse(eq(searchResult), eq(queryExpression), eq(SEARCHED_TEXT));
  }

  @Test
  public void onEnterClickedWhenCancelButtonInFocus() throws Exception {
    when(view.isCancelButtonInFocus()).thenReturn(true);

    fullTextSearchPresenter.onEnterClicked();

    verify(view).close();
    verify(view, never()).getSearchText();
  }

  @Test
  public void onEnterClickedWhenSelectPathButtonInFocus() throws Exception {
    when(view.isSelectPathButtonInFocus()).thenReturn(true);

    fullTextSearchPresenter.onEnterClicked();

    verify(view).showSelectPathDialog();
    verify(view, never()).getSearchText();
  }
}
