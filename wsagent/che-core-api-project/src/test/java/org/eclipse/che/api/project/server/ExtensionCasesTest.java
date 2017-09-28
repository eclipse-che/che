/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server;

import org.junit.Before;

/** @author gazarenkov */
public class ExtensionCasesTest extends WsAgentTestBase {

  @Before
  public void setUp() throws Exception {
    //
    //    super.setUp();
    //
    //    new File(root, "/project1").mkdir();
    //
    //    List<ProjectConfig> projects = new ArrayList<>();
    //    projects.add(
    //        DtoFactory.newDto(ProjectConfigDto.class)
    //            .withPath("/project1")
    //            .withName("project1Name")
    //            .withType("primary1"));
    //
    //    workspaceHolder = new TestWorkspaceHolder(projects);
    //    ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
    //    projectTypeRegistry.registerProjectType(new PT1());
    //    //projectTypeRegistry.registerProjectType(new PT3());
    //
    //    //ProjectHandlerRegistry projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());
    //
    //    projectRegistry =
    //        new ProjectRegistry(
    //            workspaceHolder,
    //            vfsProvider,
    //            projectTypeRegistry,
    //            projectHandlerRegistry,
    //            eventService);
    //    projectRegistry.initProjects();
    //
    //    pm =
    //        new ProjectManager_(
    //            vfsProvider,
    //            projectTypeRegistry,
    //            projectRegistry,
    //            projectHandlerRegistry,
    //            null,
    //            fileWatcherNotificationHandler,
    //            fileTreeWatcher,
    //            workspaceHolder,
    //            fileWatcherManager);
    //    pm.initWatcher();
    //
    //    projectHandlerRegistry.register(
    //        new ProjectInitHandler() {
    //          @Override
    //          public void onProjectInitialized(ProjectRegistry registry, FolderEntry projectFolder)
    //              throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    //
    //            projectFolder.createFile("generated", "test".getBytes());
    //            projectFolder.createFolder("project2");
    //            projectRegistry.setProjectType("/project1/project2", BaseProjectType.ID, false);
    //
    //            //System.out.println(">>S>>> "+projectRegistry);
    //
    //          }
    //
    //          @Override
    //          public String getProjectType() {
    //            return "primary1";
    //          }
    //        });
    //  }
    //
    //  @Test
    //  public void testInitProjectHandler() throws Exception {
    //
    //    projectRegistry.initProjects();
    //
    //    RegisteredProject p = pm.getProject("/project1/project2");
    //    assertEquals(BaseProjectType.ID, p.getType());
  }
}
