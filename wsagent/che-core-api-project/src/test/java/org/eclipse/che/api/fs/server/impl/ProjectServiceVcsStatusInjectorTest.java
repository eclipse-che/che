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
package org.eclipse.che.api.fs.server.impl;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.ADDED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.MODIFIED;
import static org.eclipse.che.api.project.shared.Constants.VCS_PROVIDER_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VcsStatusProvider;
import org.eclipse.che.api.project.server.impl.ProjectServiceVcsStatusInjector;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class ProjectServiceVcsStatusInjectorTest {

  private ProjectServiceVcsStatusInjector vcsStatusInjector;
  @Mock private VcsStatusProvider vcsStatusProvider;
  @Mock private ProjectManager projectManager;
  @Mock private RegisteredProject project;
  @Mock private ItemReference itemReference;
  @Mock private ItemReference itemReference1;
  @Captor private ArgumentCaptor<Map<String, String>> argumentCaptor;

  @BeforeMethod
  public void setUp() throws Exception {
    vcsStatusInjector =
        new ProjectServiceVcsStatusInjector(projectManager, singleton(vcsStatusProvider));

    when(project.getAttributes()).thenReturn(singletonMap(VCS_PROVIDER_NAME, singletonList("git")));
    when(projectManager.get("/project")).thenReturn(Optional.of(project));
    when(itemReference.getPath()).thenReturn("/project/file");
    when(itemReference.getType()).thenReturn("file");
    when(itemReference.getProject()).thenReturn("project");
    when(itemReference1.getPath()).thenReturn("/project/file1");
    when(itemReference1.getType()).thenReturn("file");
    when(itemReference1.getProject()).thenReturn("project");
    when(vcsStatusProvider.getVcsName()).thenReturn("git");
  }

  @Test
  public void shouldInjectVcsStatus() throws Exception {
    // given
    when(vcsStatusProvider.getStatus("/project/file")).thenReturn(ADDED);

    // when
    vcsStatusInjector.injectVcsStatus(itemReference);

    // then
    verify(itemReference).setAttributes(argumentCaptor.capture());
    assertTrue(argumentCaptor.getValue().size() == 1);
    assertEquals(argumentCaptor.getValue().get("vcs.status"), ADDED.toString());
  }

  @Test
  public void shouldInjectVcsStatusIntoListOfItems() throws Exception {
    // given
    List<ItemReference> itemReferences = new ArrayList<>();
    itemReferences.add(itemReference);
    itemReferences.add(itemReference1);

    List<String> itemReferenceFiles = new ArrayList<>();
    itemReferenceFiles.add("file");
    itemReferenceFiles.add("file1");

    Map<String, VcsStatusProvider.VcsStatus> statusMap = new HashMap<>();
    statusMap.put("/project/file", ADDED);
    statusMap.put("/project/file1", MODIFIED);

    when(vcsStatusProvider.getStatus("project", itemReferenceFiles)).thenReturn(statusMap);

    // when
    vcsStatusInjector.injectVcsStatus(itemReferences);

    // then
    verify(itemReference).setAttributes(argumentCaptor.capture());
    assertTrue(argumentCaptor.getValue().size() == 1);
    assertEquals(argumentCaptor.getValue().get("vcs.status"), ADDED.toString());

    verify(itemReference1).setAttributes(argumentCaptor.capture());
    assertTrue(argumentCaptor.getValue().size() == 1);
    assertEquals(argumentCaptor.getValue().get("vcs.status"), MODIFIED.toString());
  }

  @Test
  public void shouldInjectVcsStatusIntoListOfItemsToNestedProject() throws Exception {
    // given
    when(itemReference.getProject()).thenReturn("/project/nestedProject");
    when(itemReference.getPath()).thenReturn("/project/nestedProject/file");
    when(projectManager.get("/project/nestedProject")).thenReturn(Optional.of(project));
    when(vcsStatusProvider.getStatus("/project/nestedProject", singletonList("file")))
        .thenReturn(singletonMap("/project/nestedProject/file", ADDED));

    // when
    vcsStatusInjector.injectVcsStatus(singletonList(itemReference));

    // then
    verify(itemReference).setAttributes(argumentCaptor.capture());
    assertTrue(argumentCaptor.getValue().size() == 1);
    assertEquals(argumentCaptor.getValue().get("vcs.status"), ADDED.toString());
  }

  @Test
  public void shouldInjectVcsStatusIntoTreeElements() throws Exception {
    // given
    TreeElement treeElement = mock(TreeElement.class);
    TreeElement treeElement1 = mock(TreeElement.class);
    when(treeElement.getNode()).thenReturn(itemReference);
    when(treeElement1.getNode()).thenReturn(itemReference1);

    List<TreeElement> treeElements = new ArrayList<>();
    treeElements.add(treeElement);
    treeElements.add(treeElement1);

    List<String> itemReferenceFiles = new ArrayList<>();
    itemReferenceFiles.add("file");
    itemReferenceFiles.add("file1");

    Map<String, VcsStatusProvider.VcsStatus> statusMap = new HashMap<>();
    statusMap.put("/project/file", ADDED);
    statusMap.put("/project/file1", MODIFIED);

    when(vcsStatusProvider.getStatus("project", itemReferenceFiles)).thenReturn(statusMap);

    // when
    vcsStatusInjector.injectVcsStatusTreeElements(treeElements);

    // then
    verify(itemReference).setAttributes(argumentCaptor.capture());
    assertTrue(argumentCaptor.getValue().size() == 1);
    assertEquals(argumentCaptor.getValue().get("vcs.status"), ADDED.toString());

    verify(itemReference1).setAttributes(argumentCaptor.capture());
    assertTrue(argumentCaptor.getValue().size() == 1);
    assertEquals(argumentCaptor.getValue().get("vcs.status"), MODIFIED.toString());
  }
}
