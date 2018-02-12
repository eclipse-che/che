/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.search.selectpath;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.List;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.search.FullTextSearchView;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.SettingsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Tests for {@link FindResultPresenter}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class SelectPathPresenterTest {
  @Mock private SelectPathView view;
  @Mock private FullTextSearchView.ActionDelegate searcher;
  @Mock private AppContext appContext;
  @Mock private ResourceNode.NodeFactory nodeFactory;
  @Mock private SettingsProvider settingsProvider;

  @InjectMocks SelectPathPresenter selectPathPresenter;

  @Test
  public void windowShouldBeShown() throws Exception {
    when(appContext.getProjects()).thenReturn(new Project[0]);
    selectPathPresenter.show(searcher);
    verify(view).setStructure(org.mockito.ArgumentMatchers.<List<Node>>any());
    verify(view).showDialog();
  }

  @Test
  public void pathShouldBeSelected() throws Exception {
    when(appContext.getProjects()).thenReturn(new Project[0]);
    selectPathPresenter.show(searcher);
    selectPathPresenter.setSelectedPath("path");

    verify(searcher).setPathDirectory("path");
  }
}
