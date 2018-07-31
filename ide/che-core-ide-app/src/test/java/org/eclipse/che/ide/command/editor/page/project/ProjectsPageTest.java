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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage.DirtyStateListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Tests for {@link ProjectsPage}. */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectsPageTest {

  private static final String PROJECT_PATH = "/projects/p1";

  @Mock private ProjectsPageView view;
  @Mock private AppContext appContext;
  @Mock private EditorMessages messages;
  @Mock private EventBus eventBus;

  @InjectMocks private ProjectsPage page;

  @Mock private DirtyStateListener dirtyStateListener;
  @Mock private CommandImpl editedCommand;
  @Mock private ApplicableContext applicableContext;
  @Mock private Project project;
  @Captor private ArgumentCaptor<Map<Project, Boolean>> projectsStatesCaptor;

  @Before
  public void setUp() throws Exception {
    when(project.getPath()).thenReturn(PROJECT_PATH);
    when(appContext.getProjects()).thenReturn(new Project[] {project});
    when(editedCommand.getApplicableContext()).thenReturn(applicableContext);

    page.setDirtyStateListener(dirtyStateListener);
    page.edit(editedCommand);
  }

  @Test
  public void shouldSetViewDelegate() throws Exception {
    verify(view).setDelegate(page);
  }

  @Test
  public void shouldReturnView() throws Exception {
    assertEquals(view, page.getView());
  }

  @Test
  public void shouldSetProjects() throws Exception {
    setUpApplicableProjectToContext();

    verifySettingProjects();
  }

  @Test
  public void shouldNotifyListenerWhenApplicableProjectChanged() throws Exception {
    page.onApplicableProjectChanged(mock(Project.class), true);

    verify(dirtyStateListener, times(2)).onDirtyStateChanged();
  }

  @Test
  public void shouldAddApplicableProjectInContext() throws Exception {
    page.onApplicableProjectChanged(project, true);

    verify(applicableContext).addProject(eq(PROJECT_PATH));
  }

  @Test
  public void shouldRemoveApplicableProjectFromContext() throws Exception {
    page.onApplicableProjectChanged(project, false);

    verify(applicableContext).removeProject(eq(PROJECT_PATH));
  }

  @Test
  public void shouldUnsetWorkspaceApplicableWhenAnyApplicableProject() throws Exception {
    page.onApplicableProjectChanged(project, true);

    verify(applicableContext).setWorkspaceApplicable(eq(Boolean.FALSE));
  }

  @Test
  public void shouldSetWorkspaceApplicableWhenNoApplicableProject() throws Exception {
    page.onApplicableProjectChanged(project, false);

    verify(applicableContext).setWorkspaceApplicable(eq(Boolean.TRUE));
  }

  @Test
  public void shouldSetProjectsOnResourceChanged() throws Exception {
    setUpApplicableProjectToContext();

    ResourceDelta resourceDelta = mock(ResourceDelta.class);
    Resource resource = mock(Resource.class);
    when(resourceDelta.getResource()).thenReturn(resource);
    when(resource.isProject()).thenReturn(true);

    page.onResourceChanged(new ResourceChangedEvent(resourceDelta));

    verifySettingProjects();
  }

  private void setUpApplicableProjectToContext() {
    Set<String> applicableProjects = new HashSet<>();
    applicableProjects.add(PROJECT_PATH);
    when(applicableContext.getApplicableProjects()).thenReturn(applicableProjects);
  }

  private void verifySettingProjects() throws Exception {
    verify(view).setProjects(projectsStatesCaptor.capture());

    Map<Project, Boolean> map = projectsStatesCaptor.getValue();
    assertTrue(map.containsKey(project));
  }
}
