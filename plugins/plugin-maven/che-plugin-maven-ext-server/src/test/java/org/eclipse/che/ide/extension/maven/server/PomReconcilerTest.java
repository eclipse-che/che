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
package org.eclipse.che.ide.extension.maven.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.extension.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.che.ide.extension.maven.server.core.MavenProjectManager;
import org.eclipse.che.ide.extension.maven.server.rest.MavenServerService;
import org.eclipse.che.ide.extension.maven.server.rmi.MavenServerManagerTest;
import org.eclipse.che.maven.server.MavenTerminal;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.rmi.RemoteException;
import java.util.List;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
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
        projectManager = new MavenProjectManager(mavenServerManager, terminal, mavenNotifier, new EclipseWorkspaceProvider());

    }

    @Test
    public void testProblemPosition() throws Exception {
        MavenServerService serverService = new MavenServerService(null, projectRegistry, pm, projectManager);
        String pom = createProjectWithPom("A", "<ss");
        List<Problem> problems = serverService.reconsilePom("/A/pom.xml");
        assertThat(problems).isNotEmpty();
        Problem problem = problems.get(0);

        assertThat(problem.getSourceStart()).isEqualTo(pom.indexOf("<ss") + 3);
        assertThat(problem.getSourceEnd()).isEqualTo(pom.indexOf("<ss") + 4);

    }

    private String createProjectWithPom(String name, String pomContent)
            throws ServerException, ConflictException, ForbiddenException, NotFoundException {
        FolderEntry folder = pm.getProjectsRoot().createFolder(name);
        String content = getPomContent(pomContent);
        folder.createFile("pom.xml", content.getBytes());
        projectRegistry.setProjectType(folder.getPath().toString(),MAVEN_ID, false);
        return content;
    }
}
