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
package org.eclipse.che.api.git;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GitStatusProviderTest {

  private static final String PATH = "/project/folder/file";
  private static final String NORMALIZED_PATH = "folder/file";

  private @Mock RegisteredProject registeredProject;
  private @Mock GitConnection gitConnection;
  private @Mock GitConnectionFactory gitConnectionFactory;
  private @Mock PathTransformer pathTransformer;
  private @Mock ProjectManager projectManager;
  private @Mock Status statusDto;
  private @InjectMocks GitStatusProvider gitStatusProvider;

  @BeforeMethod
  public void setup() throws Exception {
    when(projectManager.getClosest(anyString())).thenReturn(Optional.of(registeredProject));
    when(registeredProject.getPath()).thenReturn("/project");
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("/fsPath/project");
    when(pathTransformer.transform("/project")).thenReturn(path);
    when(gitConnectionFactory.getConnection("/fsPath/project")).thenReturn(gitConnection);
    when(gitConnection.status(singletonList(NORMALIZED_PATH))).thenReturn(statusDto);
  }

  @Test
  public void shouldReturnVcsName() {
    assertEquals(GitProjectType.TYPE_ID, gitStatusProvider.getVcsName());
  }

  @Test
  public void shouldReturnUntrackedStatus() throws Exception {
    // given
    when(statusDto.getUntracked()).thenReturn(singletonList(NORMALIZED_PATH));

    // when
    VcsStatus status = gitStatusProvider.getStatus(PATH);

    // then
    assertTrue(status == UNTRACKED);
  }

  @Test
  public void shouldReturnAddedStatus() throws Exception {
    // given
    when(statusDto.getAdded()).thenReturn(singletonList(NORMALIZED_PATH));

    // when
    VcsStatus status = gitStatusProvider.getStatus(PATH);

    // then
    assertTrue(status == ADDED);
  }

  @Test
  public void shouldReturnModifiedStatus() throws Exception {
    // given
    when(statusDto.getModified()).thenReturn(singletonList(NORMALIZED_PATH));

    // when
    VcsStatus status = gitStatusProvider.getStatus(PATH);

    // then
    assertTrue(status == MODIFIED);
  }

  @Test
  public void shouldReturnModifiedStatusIfChanged() throws Exception {
    // given
    when(statusDto.getChanged()).thenReturn(singletonList(NORMALIZED_PATH));

    // when
    VcsStatus status = gitStatusProvider.getStatus(PATH);

    // then
    assertTrue(status == MODIFIED);
  }

  @Test
  public void shouldReturnNotModifiedStatus() throws Exception {
    // when
    VcsStatus status = gitStatusProvider.getStatus(PATH);

    // then
    assertTrue(status == NOT_MODIFIED);
  }

  @Test
  public void shouldReturnStatusMap() throws Exception {
    // given
    when(statusDto.getUntracked()).thenReturn(singletonList(NORMALIZED_PATH + "1"));
    when(statusDto.getAdded()).thenReturn(singletonList(NORMALIZED_PATH + "2"));
    when(statusDto.getModified()).thenReturn(singletonList(NORMALIZED_PATH + "3"));
    when(statusDto.getChanged()).thenReturn(singletonList(NORMALIZED_PATH + "4"));
    when(gitConnection.status(anyList())).thenReturn(statusDto);

    List<String> paths = new ArrayList<>();
    paths.add(NORMALIZED_PATH + "1");
    paths.add(NORMALIZED_PATH + "2");
    paths.add(NORMALIZED_PATH + "3");
    paths.add(NORMALIZED_PATH + "4");
    paths.add(NORMALIZED_PATH + "5");

    // when
    Map<String, VcsStatus> statusMap = gitStatusProvider.getStatus(NORMALIZED_PATH, paths);

    // then
    assertTrue(statusMap.get(PATH + "1") == UNTRACKED);
    assertTrue(statusMap.get(PATH + "2") == ADDED);
    assertTrue(statusMap.get(PATH + "3") == MODIFIED);
    assertTrue(statusMap.get(PATH + "4") == MODIFIED);
    assertTrue(statusMap.get(PATH + "5") == NOT_MODIFIED);
  }
}
