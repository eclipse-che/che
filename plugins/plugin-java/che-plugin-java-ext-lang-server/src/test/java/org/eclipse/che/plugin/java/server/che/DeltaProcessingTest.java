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
package org.eclipse.che.plugin.java.server.che;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.jdt.core.resources.ResourceChangedEvent;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/** @author Evgen Vidolob */
// TODO: rework after new Project API
@Ignore
public class DeltaProcessingTest extends BaseTest {

  @After
  public void tearDown() throws Exception {
    File workspace = new File(BaseTest.class.getResource("/projects").getFile());
    File newFile = new File(workspace, "/test/src/main/java/org/eclipse/che/test/NewClass.java");
    if (newFile.exists()) {
      newFile.delete();
    }
  }

  @Test
  public void testRemoveClass() throws Exception {
    ResourceChangedEvent event =
        new ResourceChangedEvent(
            new File(BaseTest.class.getResource("/projects").getFile()),
            new ProjectItemModifiedEvent(
                ProjectItemModifiedEvent.EventType.DELETED,
                "test",
                "/test/src/main/java/org/eclipse/che/test/MyClass.java",
                false));
    NameEnvironmentAnswer answer =
        project
            .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY)
            .findType(CharOperation.splitOn('.', "org.eclipse.che.test.MyClass".toCharArray()));

    assertThat(answer).isNotNull();

    JavaModelManager.getJavaModelManager().deltaState.resourceChanged(event);

    answer =
        project
            .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY)
            .findType(CharOperation.splitOn('.', "org.eclipse.che.test.MyClass".toCharArray()));
    assertThat(answer).isNull();
  }

  @Test
  public void testRemoveFolder() throws Exception {
    ResourceChangedEvent event =
        new ResourceChangedEvent(
            new File(BaseTest.class.getResource("/projects").getFile()),
            new ProjectItemModifiedEvent(
                ProjectItemModifiedEvent.EventType.DELETED,
                "test",
                "/test/src/main/java/org/eclipse/che/test",
                true));
    NameEnvironmentAnswer answer =
        project
            .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY)
            .findType(CharOperation.splitOn('.', "org.eclipse.che.test.MyClass".toCharArray()));

    assertThat(answer).isNotNull();
    JavaModelManager.getJavaModelManager().deltaState.resourceChanged(event);
    answer =
        project
            .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY)
            .findType(CharOperation.splitOn('.', "org.eclipse.che.test.MyClass".toCharArray()));
    assertThat(answer).isNull();
  }

  @Test
  public void testAddClass() throws Exception {

    File workspace = new File(BaseTest.class.getResource("/projects").getFile());
    ResourceChangedEvent event =
        new ResourceChangedEvent(
            workspace,
            new ProjectItemModifiedEvent(
                ProjectItemModifiedEvent.EventType.CREATED,
                "test",
                "/test/src/main/java/org/eclipse/che/test/NewClass.java",
                false));

    NameEnvironmentAnswer answer =
        project
            .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY)
            .findType(CharOperation.splitOn('.', "org.eclipse.che.test.NewClass".toCharArray()));

    assertThat(answer).isNull();

    FileOutputStream outputStream =
        new FileOutputStream(
            new File(workspace, "/test/src/main/java/org/eclipse/che/test/NewClass.java"));
    outputStream.write("package org.eclipse.che.test;\n public class NewClass{}\n".getBytes());
    outputStream.close();
    JavaModelManager.getJavaModelManager().deltaState.resourceChanged(event);
    answer =
        project
            .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY)
            .findType(CharOperation.splitOn('.', "org.eclipse.che.test.NewClass".toCharArray()));
    assertThat(answer).isNotNull();
  }
}
