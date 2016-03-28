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
package org.eclipse.che.ide.search;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.gwt.client.QueryExpression;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FullTextSearchPresenter}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class FullTextSearchPresenterTest {
    private final String SEARCHED_TEXT = "to be or not to be";

    @Mock
    private FullTextSearchView           view;
    @Mock
    private FindResultPresenter          findResultPresenter;
    @Mock
    private DtoFactory                   dtoFactory;
    @Mock
    private DtoUnmarshallerFactory       dtoUnmarshallerFactory;
    @Mock
    private ProjectServiceClient         projectServiceClient;
    @Mock
    private Promise<List<ItemReference>> promise;
    @Mock
    private PromiseError                 promiseError;

    @Mock
    private AppContext   appContext;
    @Mock
    private WorkspaceDto workspaceDto;

    @Captor
    private ArgumentCaptor<Operation<PromiseError>>        operationErrorCapture;
    @Captor
    private ArgumentCaptor<Operation<List<ItemReference>>> operationSuccessCapture;

    FullTextSearchPresenter fullTextSearchPresenter;

    @Before
    public void setUp() throws Exception {
        when(appContext.getWorkspace()).thenReturn(workspaceDto);
        when(workspaceDto.getId()).thenReturn("id");
        String MASK = "mask";
        when(view.getFileMask()).thenReturn(MASK);
        String PATH = "path";
        when(view.getPathToSearch()).thenReturn(PATH);
        when(projectServiceClient.search(anyString(), Matchers.<QueryExpression>any())).thenReturn(promise);
        when(promise.then(operationSuccessCapture.capture())).thenReturn(promise);

        fullTextSearchPresenter = new FullTextSearchPresenter(view,
                                                              findResultPresenter,
                                                              dtoFactory,
                                                              appContext,
                                                              projectServiceClient);
    }

    @Test
    public void viewShouldBeShowed() {
        fullTextSearchPresenter.showDialog();

        verify(view).showDialog();
        verify(view).clearInput();
    }

    @Test
    public void pathOfDirectoryToSearchShouldBeSet() {
        fullTextSearchPresenter.setPathDirectory(anyString());

        verify(view).setPathDirectory(anyString());
    }

    @Test
    public void searchShouldBeSuccessfullyFinished() throws Exception {
        List<ItemReference> result = new ArrayList<>();

        fullTextSearchPresenter.search(SEARCHED_TEXT);

        verify(view, times(2)).getPathToSearch();
        verify(view, times(2)).getFileMask();

        verify(promise).then(operationSuccessCapture.capture());
        operationSuccessCapture.getValue().apply(result);

        verify(view).close();
        verify(findResultPresenter).handleResponse(result, SEARCHED_TEXT);
    }

    @Test
    public void searchHasDoneWithSomeError() throws Exception {
        ServiceError serviceError = Mockito.mock(ServiceError.class);
        when(promiseError.getMessage()).thenReturn(SEARCHED_TEXT);
        when(dtoFactory.createDtoFromJson(SEARCHED_TEXT, ServiceError.class)).thenReturn(serviceError);
        when(serviceError.getMessage()).thenReturn(SEARCHED_TEXT);

        fullTextSearchPresenter.search(SEARCHED_TEXT);

        verify(view, times(2)).getPathToSearch();
        verify(view, times(2)).getFileMask();

        verify(promise).catchError(operationErrorCapture.capture());
        operationErrorCapture.getValue().apply(promiseError);

        verify(view).showErrorMessage(SEARCHED_TEXT);
    }

    @Test
    public void onEnterClickedWhenAcceptButtonInFocus() throws Exception {
        when(view.getSearchText()).thenReturn(SEARCHED_TEXT);
        when(view.isAcceptButtonInFocus()).thenReturn(true);

        fullTextSearchPresenter.onEnterClicked();

        verify(view).getSearchText();
        verify(projectServiceClient).search(anyString(), (QueryExpression)anyObject());
    }

    @Test
    public void onEnterClickedWhenCancelButtonInFocus() throws Exception {
        when(view.isCancelButtonInFocus()).thenReturn(true);

        fullTextSearchPresenter.onEnterClicked();

        verify(view).close();
        verify(view, never()).getSearchText();
        verify(projectServiceClient, never()).search(anyString(), (QueryExpression)anyObject());
    }

    @Test
    public void onEnterClickedWhenSelectPathButtonInFocus() throws Exception {
        when(view.isSelectPathButtonInFocus()).thenReturn(true);

        fullTextSearchPresenter.onEnterClicked();

        verify(view).showSelectPathDialog();
        verify(view, never()).getSearchText();
        verify(projectServiceClient, never()).search(anyString(), (QueryExpression)anyObject());
    }
}
