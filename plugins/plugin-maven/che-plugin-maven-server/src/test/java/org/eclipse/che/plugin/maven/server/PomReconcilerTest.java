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
package org.eclipse.che.plugin.maven.server;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.plugin.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.rest.MavenServerService;
import org.eclipse.che.plugin.maven.server.rmi.MavenServerManagerTest;
import org.eclipse.che.maven.server.MavenTerminal;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.rmi.RemoteException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class PomReconcilerTest extends BaseTest {


    private MavenProjectManager projectManager;

    @BeforeMethod
    public void setUp() throws Exception {
        MavenServerManagerTest.MyMavenServerProgressNotifier mavenNotifier = new MavenServerManagerTest.MyMavenServerProgressNotifier();
        MavenTerminal terminal = new MavenTerminal() {
            @Override
            public void print(int level, String message, Throwable throwable) throws RemoteException {
                System.out.println(message);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        };
        MavenWrapperManager wrapperManager = new MavenWrapperManager(mavenServerManager);
        projectManager = new MavenProjectManager(wrapperManager, mavenServerManager, terminal, mavenNotifier, new EclipseWorkspaceProvider());

    }

    @Test
    public void testProblemPosition() throws Exception {
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager, null, null);
        FolderEntry testProject = createTestProject("A", "");
        VirtualFileEntry child = testProject.getChild("pom.xml");
        String newContent = getPomContent("<ss");
        child.getVirtualFile().updateContent(newContent);

        List<Problem> problems = serverService.reconsilePom("/A/pom.xml");
        assertThat(problems).isNotEmpty();
        Problem problem = problems.get(0);

        assertThat(problem.getSourceStart()).isEqualTo(newContent.indexOf("<ss") + 3);
        assertThat(problem.getSourceEnd()).isEqualTo(newContent.indexOf("<ss") + 4);

    }
}
