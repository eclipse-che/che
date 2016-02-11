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
package org.eclipse.che.git.impl;

import com.google.common.io.Files;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RemoteListRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Eugene Voevodin
 */
public class RemoteDeleteTest {

    private File repository;

    @BeforeMethod
    public void setUp() {
        repository = Files.createTempDir();
    }

    @AfterMethod
    public void cleanUp() {
        cleanupTestRepo(repository);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testRemoteDelete(GitConnectionFactory connectionFactory) throws GitException, IOException {
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        connection.remoteAdd(newDto(RemoteAddRequest.class).withName("origin").withUrl("host.com:username/Repo.git"));
        //now it is 1 remote
        assertEquals(connection.remoteList(newDto(RemoteListRequest.class)).size(), 1);
        //try delete not existing remote
        try {
            connection.remoteDelete("donotexists");
            fail("should be exception");
        } catch (GitException ignored) {
        }
        connection.remoteDelete("origin");
        //now it is 0 remotes
        assertEquals(connection.remoteList(newDto(RemoteListRequest.class)).size(), 0);
    }
}