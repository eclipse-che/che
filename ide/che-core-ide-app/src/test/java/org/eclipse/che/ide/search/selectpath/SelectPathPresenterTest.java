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
package org.eclipse.che.ide.search.selectpath;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.search.FullTextSearchView;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * Tests for {@link FindResultPresenter}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class SelectPathPresenterTest {
    @Mock
    private SelectPathView                    view;
    @Mock
    private ProjectExplorerPresenter          projectExplorerPresenter;
    @Mock
    private FullTextSearchView.ActionDelegate searcher;

    @InjectMocks
    SelectPathPresenter selectPathPresenter;

    @Test
    public void windowShouldBeShown() throws Exception {
        selectPathPresenter.show(searcher);
        verify(view).setStructure(Matchers.<List<Node>>any());
    }

    @Test
    public void pathShouldBeSelected() throws Exception {
        selectPathPresenter.show(searcher);
        selectPathPresenter.setSelectedPath("path");

        verify(searcher).setPathDirectory("path");
    }
}