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

import static java.util.Collections.emptyList;
import static org.eclipse.che.ide.search.FullTextSearchPresenter.SEARCH_RESULT_ITEMS;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.project.ProjectServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Tests for {@link FindResultPresenter}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class FindResultPresenterTest {
  @Mock private CoreLocalizationConstant localizationConstant;
  @Mock private FindResultView view;
  @Mock private WorkspaceAgent workspaceAgent;
  @Mock private Resources resources;
  @Mock private ProjectServiceClient projectServiceClient;
  @Mock private EventBus eventBus;

  @Mock private QueryExpression queryExpression;
  @Mock private Promise<SearchResult> searchResultPromise;
  @Mock private SearchItemReference searchItemReference;
  @Captor private ArgumentCaptor<Operation<SearchResult>> argumentCaptor;
  @Mock private SearchResult result;

  @InjectMocks FindResultPresenter findResultPresenter;
  private ArrayList<SearchItemReference> items = new ArrayList<>(SEARCH_RESULT_ITEMS);

  @Before
  public void setUp() throws Exception {
    for (int i = 0; i < SEARCH_RESULT_ITEMS; i++) {
      items.add(searchItemReference);
    }

    when(projectServiceClient.search(queryExpression)).thenReturn(searchResultPromise);
    when(searchResultPromise.then(org.mockito.ArgumentMatchers.<Operation<SearchResult>>any()))
        .thenReturn(searchResultPromise);
    when(result.getItemReferences()).thenReturn(items);
  }

  @Test
  public void titleShouldBeReturned() {
    findResultPresenter.getTitle();

    verify(localizationConstant).actionFullTextSearch();
  }

  @Test
  public void viewShouldBeReturned() {
    assertEquals(findResultPresenter.getView(), view);
  }

  @Test
  public void imageShouldBeReturned() {
    findResultPresenter.getTitleImage();

    verify(resources).find();
  }

  @Test
  public void methodGoShouldBePerformed() {
    AcceptsOneWidget container = mock(AcceptsOneWidget.class);
    findResultPresenter.go(container);

    verify(container).setWidget(view);
  }

  @Test
  public void responseShouldBeHandled() throws Exception {
    QueryExpression queryExpression = mock(QueryExpression.class);
    findResultPresenter.handleResponse(result, queryExpression, "request");

    verify(workspaceAgent).openPart(findResultPresenter, PartStackType.INFORMATION);
    verify(workspaceAgent).setActivePart(findResultPresenter);
    verify(view).showResults(result, "request");
    verify(view).setPreviousBtnActive(false);
    verify(view).setNextBtnActive(true);
  }

  @Test
  public void nextPageShouldNotBeShownIfNoResults() throws Exception {
    findResultPresenter.handleResponse(result, queryExpression, "request");
    reset(view);
    findResultPresenter.onNextButtonClicked();

    verify(queryExpression).setSkipCount(SEARCH_RESULT_ITEMS);

    verify(searchResultPromise).then(argumentCaptor.capture());
    argumentCaptor.getValue().apply(new SearchResult(emptyList(), 0));

    verify(view).setPreviousBtnActive(true);
    verify(view).setNextBtnActive(false);
    verify(view, never()).showResults(anyObject(), anyString());
  }

  @Test
  public void nextButtonShouldBeActiveIfResultHasMaxValueElements() throws Exception {
    findResultPresenter.handleResponse(result, queryExpression, "request");

    findResultPresenter.handleResponse(result, queryExpression, "request");
    reset(view);
    findResultPresenter.onNextButtonClicked();

    verify(queryExpression).setSkipCount(SEARCH_RESULT_ITEMS);

    verify(searchResultPromise).then(argumentCaptor.capture());

    SearchResult searchResult = new SearchResult(items, 0);
    argumentCaptor.getValue().apply(searchResult);

    verify(view).setPreviousBtnActive(true);
    verify(view).setNextBtnActive(true);
    verify(view).showResults(searchResult, "request");
  }

  @Test
  public void nextButtonShouldBeDisableIfResultHasLessThanMaxValue() throws Exception {
    items.remove(0);
    findResultPresenter.handleResponse(result, queryExpression, "request");
    reset(view);
    findResultPresenter.onNextButtonClicked();

    verify(queryExpression).setSkipCount(SEARCH_RESULT_ITEMS);

    verify(searchResultPromise).then(argumentCaptor.capture());

    SearchResult searchResult = new SearchResult(items, 0);
    argumentCaptor.getValue().apply(searchResult);

    verify(view).setPreviousBtnActive(true);
    verify(view).setNextBtnActive(false);
    verify(view).showResults(searchResult, "request");
  }

  @Test
  public void previousButtonShouldBeActiveIfResultHasLessThanMaxValue() throws Exception {
    items.remove(0);
    findResultPresenter.handleResponse(result, queryExpression, "request");
    reset(view);
    findResultPresenter.onPreviousButtonClicked();

    verify(queryExpression).setSkipCount(-SEARCH_RESULT_ITEMS);

    verify(searchResultPromise).then(argumentCaptor.capture());

    SearchResult searchResult = new SearchResult(items, 0);
    argumentCaptor.getValue().apply(searchResult);

    verify(view).setNextBtnActive(true);
    verify(view).setPreviousBtnActive(false);
    verify(view).showResults(searchResult, "request");
  }

  @Test
  public void previousButtonShouldBeActiveIfResultHasMaxValueElements() throws Exception {
    findResultPresenter.handleResponse(result, queryExpression, "request");
    reset(view);
    findResultPresenter.onPreviousButtonClicked();

    verify(queryExpression).setSkipCount(-SEARCH_RESULT_ITEMS);

    verify(searchResultPromise).then(argumentCaptor.capture());

    SearchResult searchResult = new SearchResult(items, 0);
    argumentCaptor.getValue().apply(searchResult);

    verify(view).setNextBtnActive(true);
    verify(view).setPreviousBtnActive(true);
    verify(view).showResults(searchResult, "request");
  }
}
