/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.search.FullTextSearchPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Tests for {@link FullTextSearchAction}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class FullTextSearchActionTest {
  @Mock private ActionEvent actionEvent;
  @Mock private FullTextSearchPresenter fullTextSearchPresenter;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private CoreLocalizationConstant locale;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private Resources resources;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private AppContext appContext;

  @Mock private Project project;

  @InjectMocks FullTextSearchAction fullTextSearchAction;

  @Test
  public void actionShouldBePerformed() {
    fullTextSearchAction.actionPerformed(actionEvent);

    verify(fullTextSearchPresenter).showDialog(any(Path.class));
  }

  @Test
  public void actionShouldBeEnabled() {
    Presentation presentation = Mockito.mock(Presentation.class);
    when(actionEvent.getPresentation()).thenReturn(presentation);
    when(appContext.getRootProject()).thenReturn(project);

    fullTextSearchAction.updateInPerspective(actionEvent);

    verify(presentation).setVisible(true);
    verify(presentation).setEnabled(true);
  }

  @Test
  public void actionShouldBeDisabled() {
    Presentation presentation = Mockito.mock(Presentation.class);
    when(actionEvent.getPresentation()).thenReturn(presentation);
    when(appContext.getRootProject()).thenReturn(null);

    fullTextSearchAction.updateInPerspective(actionEvent);

    verify(presentation).setVisible(true);
    verify(presentation).setEnabled(false);
  }
}
