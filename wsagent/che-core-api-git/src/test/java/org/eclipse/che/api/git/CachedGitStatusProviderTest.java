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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.ADDED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.MODIFIED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.UNTRACKED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class CachedGitStatusProviderTest {

  private static final String PATH = "/project/folder/file";
  private static final String NORMALIZED_PATH = "folder/file";

  private @Mock RegisteredProject registeredProject;
  private @Mock GitConnection gitConnection;
  private @Mock GitConnectionFactory gitConnectionFactory;
  private @Mock PathTransformer pathTransformer;
  private @Mock ProjectManager projectManager;
  private @Mock EventService eventService;
  private @Mock RootDirPathProvider rootDirPathProvider;
  private @Mock Status statusDto;

  private Path projectPath;
  private CachedGitStatusProvider gitStatusProvider;

  @BeforeClass
  public void createFiles() throws Exception {
    projectPath = createDirectories(createTempDirectory("").resolve("project"));
    createDirectory(projectPath.resolve("folder"));
    createFile(projectPath.resolve(NORMALIZED_PATH + "1"));
    createFile(projectPath.resolve(NORMALIZED_PATH + "2"));
    createFile(projectPath.resolve(NORMALIZED_PATH + "3"));
    createFile(projectPath.resolve(NORMALIZED_PATH + "4"));
    createFile(projectPath.resolve(NORMALIZED_PATH + "5"));
  }

  @AfterClass
  public void tearDown() throws Exception {
    Files.walk(projectPath.getParent())
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @BeforeMethod
  public void setup() throws Exception {
    when(projectManager.getClosest(anyString())).thenReturn(Optional.of(registeredProject));
    String projectName = projectPath.getName(projectPath.getNameCount() - 1).toString();
    when(registeredProject.getPath()).thenReturn("/" + projectName);
    when(registeredProject.getName()).thenReturn(projectName);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(projectPath.toString());
    when(pathTransformer.transform("/" + projectName)).thenReturn(path);
    when(gitConnectionFactory.getConnection(projectPath.toString())).thenReturn(gitConnection);
    lenient().when(gitConnection.status(singletonList(NORMALIZED_PATH))).thenReturn(statusDto);
    lenient()
        .when(rootDirPathProvider.get())
        .thenReturn("/" + projectPath.subpath(0, 2).toString());

    gitStatusProvider =
        new CachedGitStatusProvider(
            gitConnectionFactory,
            pathTransformer,
            projectManager,
            rootDirPathProvider,
            eventService);
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

  @Test
  public void shouldReturnStatusMapWithUntrackedAfterAddingFile() throws Exception {
    // given
    OngoingStubbing<Status> whenStatusCalled =
        when(gitConnection.status(singletonList(NORMALIZED_PATH)));
    Status status = newDto(Status.class);
    status.setUntracked(new ArrayList<>(singletonList(NORMALIZED_PATH)));
    // Fill the cached status map with actual status, second call will check file changes.
    whenStatusCalled.thenReturn(newDto(Status.class));
    gitStatusProvider.getStatus(NORMALIZED_PATH, singletonList(NORMALIZED_PATH));

    // when
    // Create new file
    Files.write(projectPath.resolve(NORMALIZED_PATH), "content".getBytes());
    whenStatusCalled.thenReturn(status);
    Map<String, VcsStatus> statusMap =
        gitStatusProvider.getStatus(NORMALIZED_PATH, singletonList(NORMALIZED_PATH));

    // then
    assertTrue(statusMap.get(PATH) == UNTRACKED);
  }

  @Test
  public void shouldReturnStatusMapWithModifiedAfterEditingFile() throws Exception {
    // given
    OngoingStubbing<Status> whenStatusCalled =
        lenient().when(gitConnection.status(singletonList(NORMALIZED_PATH + "1")));
    Status status = newDto(Status.class);
    status.setModified(new ArrayList<>(singletonList(NORMALIZED_PATH + "1")));
    // Fill the cached status map with actual status, second call will check file changes.
    whenStatusCalled.thenReturn(newDto(Status.class));
    gitStatusProvider.getStatus(NORMALIZED_PATH, singletonList(NORMALIZED_PATH + "1"));

    // when
    // Make a pause to have time difference in the last modified time value of the file
    Thread.sleep(1000);
    // Edit existing file
    Files.write(projectPath.resolve(NORMALIZED_PATH + "1"), "content".getBytes());
    whenStatusCalled.thenReturn(status);
    Map<String, VcsStatus> statusMap =
        gitStatusProvider.getStatus(NORMALIZED_PATH, singletonList(NORMALIZED_PATH + "1"));

    // then
    assertTrue(statusMap.get(PATH + "1") == MODIFIED);
  }
}
