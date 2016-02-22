/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java;

import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.jdt.core.resources.ResourceChangedEvent;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
// TODO: rework after new Project API
@Ignore
public class DeltaProcessingTest extends BaseTest {


    @After
    public void tearDown() throws Exception {
        File workspace = new File(BaseTest.class.getResource("/projects").getFile());
        File newFile = new File(workspace, "/test/src/main/java/com/codenvy/test/NewClass.java");
        if(newFile.exists()){
            newFile.delete();
        }

    }

    @Test
    public void testRemoveClass() throws Exception {
        ResourceChangedEvent event = new ResourceChangedEvent(new File(BaseTest.class.getResource("/projects").getFile()),new ProjectItemModifiedEvent(
                ProjectItemModifiedEvent.EventType.DELETED, "projects","test", "/test/src/main/java/com/codenvy/test/MyClass.java", false));
        NameEnvironmentAnswer answer =
                project.newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY).findType(CharOperation.splitOn('.', "com.codenvy.test.MyClass".toCharArray()));

        assertThat(answer).isNotNull();

        JavaModelManager.getJavaModelManager().deltaState.resourceChanged(event);
        project.creteNewNameEnvironment();

        answer =
                project.newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY).findType(CharOperation.splitOn('.', "com.codenvy.test.MyClass".toCharArray()));
        assertThat(answer).isNull();
    }

    @Test
    public void testRemoveFolder() throws Exception {
        ResourceChangedEvent event = new ResourceChangedEvent(new File(BaseTest.class.getResource("/projects").getFile()),new ProjectItemModifiedEvent(


                ProjectItemModifiedEvent.EventType.DELETED, "projects", "test","/test/src/main/java/com/codenvy/test", true));
        NameEnvironmentAnswer answer =
                project.newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY).findType(CharOperation.splitOn('.', "com.codenvy.test.MyClass".toCharArray()));

        assertThat(answer).isNotNull();
        JavaModelManager.getJavaModelManager().deltaState.resourceChanged(event);
        project.creteNewNameEnvironment();
        answer =
                project.newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY).findType(CharOperation.splitOn('.', "com.codenvy.test.MyClass".toCharArray()));
        assertThat(answer).isNull();
    }

    @Test
    public void testAddClass() throws Exception {

        File workspace = new File(BaseTest.class.getResource("/projects").getFile());
        ResourceChangedEvent event = new ResourceChangedEvent(workspace,new ProjectItemModifiedEvent(ProjectItemModifiedEvent.EventType.CREATED,"projects","test", "/test/src/main/java/com/codenvy/test/NewClass.java", false));


        NameEnvironmentAnswer answer =
                project.newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY).findType(CharOperation.splitOn('.', "com.codenvy.test.NewClass".toCharArray()));
        assertThat(answer).isNull();

        FileOutputStream outputStream = new FileOutputStream(new File(workspace, "/test/src/main/java/com/codenvy/test/NewClass.java"));
        outputStream.write("packagecom.codenvy.test;\n public class NewClass{}\n".getBytes());
        outputStream.close();

        JavaModelManager.getJavaModelManager().deltaState.resourceChanged(event);
        project.creteNewNameEnvironment();
        answer =
                project.newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY).findType(CharOperation.splitOn('.', "com.codenvy.test.NewClass".toCharArray()));
        assertThat(answer).isNotNull();
    }


}
