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
package org.eclipse.che.ide.command.editor.page.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.command.editor.page.project.ProjectsPageView.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Tests for {@link ProjectsPageViewImpl}. */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectsPageViewImplTest {

  @Mock private ActionDelegate actionDelegate;

  @InjectMocks private ProjectsPageViewImpl view;

  @Before
  public void setUp() throws Exception {
    view.setDelegate(actionDelegate);
  }

  @Test
  public void shouldSetProjects() throws Exception {
    // given
    Project p1 = mock(Project.class);
    Project p2 = mock(Project.class);

    Map<Project, Boolean> projects = new HashMap<>();
    projects.put(p1, true);
    projects.put(p2, true);

    // when
    view.setProjects(projects);

    // then
    verify(view.projectsPanel).clear();
    verify(view.mainPanel).setVisible(eq(Boolean.TRUE));
    verify(view.projectsPanel, times(projects.size())).add(any(ProjectSwitcher.class));
  }

  @Test
  public void shouldHidePanelWhenNoProject() throws Exception {
    view.setProjects(new HashMap<>());

    verify(view.projectsPanel).clear();
    verify(view.mainPanel, times(2)).setVisible(eq(Boolean.FALSE));
    verify(view.projectsPanel, never()).add(any(ProjectSwitcher.class));
  }
}
