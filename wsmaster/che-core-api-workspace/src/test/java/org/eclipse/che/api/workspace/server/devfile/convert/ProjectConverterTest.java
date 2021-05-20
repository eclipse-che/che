/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile.convert;

import static org.eclipse.che.api.core.model.workspace.config.SourceStorage.BRANCH_PARAMETER_NAME;
import static org.eclipse.che.api.core.model.workspace.config.SourceStorage.COMMIT_ID_PARAMETER_NAME;
import static org.eclipse.che.api.core.model.workspace.config.SourceStorage.SPARSE_CHECKOUT_DIR_PARAMETER_NAME;
import static org.eclipse.che.api.core.model.workspace.config.SourceStorage.START_POINT_PARAMETER_NAME;
import static org.eclipse.che.api.core.model.workspace.config.SourceStorage.TAG_PARAMETER_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.model.workspace.devfile.Project;
import org.eclipse.che.api.core.model.workspace.devfile.Source;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class ProjectConverterTest {

  private ProjectConverter projectConverter;

  @BeforeMethod
  public void setUp() {
    projectConverter = new ProjectConverter();
  }

  @Test
  public void testConvertingDevfileProjectToProjectConfig() throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", "master", "3434d", null, null, "core"),
            null);

    ProjectConfigImpl workspaceProject = projectConverter.toWorkspaceProject(devfileProject);

    assertEquals(workspaceProject.getName(), "myProject");
    assertEquals(workspaceProject.getPath(), "/myProject");
    SourceStorageImpl source = workspaceProject.getSource();
    assertEquals(source.getType(), "git");
    assertEquals(source.getLocation(), "https://github.com/eclipse/che.git");
    assertEquals(source.getParameters().get(BRANCH_PARAMETER_NAME), "master");
    assertEquals(source.getParameters().get(START_POINT_PARAMETER_NAME), "3434d");
    assertEquals(source.getParameters().get("keepDir"), "core");
  }

  @Test
  public void testConvertingProjectConfigToDevfileProject() {
    ProjectConfigImpl workspaceProject = new ProjectConfigImpl();
    workspaceProject.setName("myProject");
    workspaceProject.setPath("/clone/path");
    SourceStorageImpl sourceStorage = new SourceStorageImpl();
    sourceStorage.setType("git");
    sourceStorage.setLocation("https://github.com/eclipse/che.git");
    sourceStorage.setParameters(
        ImmutableMap.of(TAG_PARAMETER_NAME, "v1.0", BRANCH_PARAMETER_NAME, "develop"));
    workspaceProject.setSource(sourceStorage);

    Project devfileProject = projectConverter.toDevfileProject(workspaceProject);

    assertEquals(devfileProject.getName(), "myProject");
    Source source = devfileProject.getSource();
    assertEquals(source.getType(), "git");
    assertEquals(source.getLocation(), "https://github.com/eclipse/che.git");
    assertEquals(source.getBranch(), "develop");
    assertEquals(source.getTag(), "v1.0");
    assertEquals(devfileProject.getClonePath(), "clone/path");
  }

  @Test
  public void testClonePathSetWhenConvertingDevfileToProjectConfig() throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", "master", null, null, null, null),
            "down/the/rabbit/hole/myProject");

    ProjectConfigImpl workspaceProject = projectConverter.toWorkspaceProject(devfileProject);

    assertEquals(workspaceProject.getName(), "myProject");
    assertEquals(workspaceProject.getPath(), "/down/the/rabbit/hole/myProject");
    SourceStorageImpl source = workspaceProject.getSource();
    assertEquals(source.getType(), "git");
    assertEquals(source.getLocation(), "https://github.com/eclipse/che.git");
  }

  @Test(expectedExceptions = DevfileException.class)
  public void testClonePathCannotEscapeProjectsRoot() throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", "master", null, null, null, null),
            "cant/hack/../../../usr/bin");

    projectConverter.toWorkspaceProject(devfileProject);
  }

  @Test(expectedExceptions = DevfileException.class)
  public void testClonePathCannotBeAbsolute() throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", "master", null, null, null, null),
            "/usr/bin");

    projectConverter.toWorkspaceProject(devfileProject);
  }

  @Test
  public void testUpDirOkInClonePathAsLongAsItDoesntEscapeProjectsRoot() throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", "master", null, null, null, null),
            "cant/hack/../../usr/bin");

    ProjectConfigImpl workspaceProject = projectConverter.toWorkspaceProject(devfileProject);
    // this is OK, because the absolute-looking path is applied to the projects root
    assertEquals(workspaceProject.getPath(), "/usr/bin");
  }

  @Test(expectedExceptions = DevfileException.class)
  public void testCloningIntoProjectsRootFails() throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", "master", null, null, null, null),
            "not/../in/root/../..");

    projectConverter.toWorkspaceProject(devfileProject);
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Only one of '"
              + START_POINT_PARAMETER_NAME
              + "', '"
              + TAG_PARAMETER_NAME
              + "', '"
              + COMMIT_ID_PARAMETER_NAME
              + "' can be specified\\.",
      dataProvider = "invalidStartPointOrTagOrCommitIdCombinations")
  public void testOnlyOneOfStartPointAttributesAllowed(
      String startPoint, String tag, String commitId) throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", null, startPoint, tag, commitId, null),
            null);

    projectConverter.toWorkspaceProject(devfileProject);
  }

  @DataProvider
  public static Object[][] invalidStartPointOrTagOrCommitIdCombinations() {
    return new Object[][] {
      new Object[] {"a", "b", null},
      new Object[] {"a", null, "b"},
      new Object[] {null, "a", "b"},
      new Object[] {"a", "b", "c"}
    };
  }

  @Test
  public void testUndefinedCloneParametersNotTransferredToWorkspaceConfig() throws Exception {
    ProjectImpl devfileProject =
        new ProjectImpl(
            "myProject",
            new SourceImpl(
                "git", "https://github.com/eclipse/che.git", null, null, null, null, null),
            null);

    ProjectConfigImpl wsProject = projectConverter.toWorkspaceProject(devfileProject);
    SourceStorageImpl wsSource = wsProject.getSource();

    assertFalse(wsSource.getParameters().containsKey(BRANCH_PARAMETER_NAME));
    assertFalse(wsSource.getParameters().containsKey(START_POINT_PARAMETER_NAME));
    assertFalse(wsSource.getParameters().containsKey(TAG_PARAMETER_NAME));
    assertFalse(wsSource.getParameters().containsKey(COMMIT_ID_PARAMETER_NAME));
    assertFalse(wsSource.getParameters().containsKey(SPARSE_CHECKOUT_DIR_PARAMETER_NAME));
  }
}
