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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class PushTest {

    private File repository;
    private File remoteRepo;

    @BeforeMethod
    public void setUp() {
        repository = Files.createTempDir();
        remoteRepo = Files.createTempDir();
    }

    @AfterMethod
    public void cleanUp() {
        cleanupTestRepo(repository);
        cleanupTestRepo(remoteRepo);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testSimplePush(GitConnectionFactory connectionFactory)
            throws IOException, ServerException, URISyntaxException, UnauthorizedException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        GitConnection remoteConnection = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
        remoteConnection.clone(newDto(CloneRequest.class).withRemoteUri(connection.getWorkingDir().getAbsolutePath())
                                                         .withWorkingDir(remoteConnection.getWorkingDir().getAbsolutePath()));
        addFile(remoteConnection, "newfile", "content");
        remoteConnection.add(newDto(AddRequest.class).withFilepattern(Collections.singletonList(".")));
        remoteConnection.commit(newDto(CommitRequest.class).withMessage("Fake commit"));
        //when
        remoteConnection.push(newDto(PushRequest.class)
                                      .withRefSpec(Collections.singletonList("refs/heads/master:refs/heads/test"))
                                      .withRemote("origin")
                                      .withTimeout(-1));
        //then
        //check branches in origin repository
        assertEquals(connection.branchList(newDto(BranchListRequest.class)).size(), 1);
        //checkout test branch
        connection.checkout(newDto(CheckoutRequest.class).withName("test"));
        assertTrue(new File(connection.getWorkingDir(), "newfile").exists());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testPushRemote(GitConnectionFactory connectionFactory)
            throws GitException, IOException, URISyntaxException, UnauthorizedException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        GitConnection remoteConnection = connectToInitializedGitRepository(connectionFactory, remoteRepo);
        addFile(connection, "README", "README");
        connection.add(newDto(AddRequest.class).withFilepattern(Collections.singletonList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("Init commit."));
        //make push
        int branchesBefore = remoteConnection.branchList(newDto(BranchListRequest.class)).size();
        //when
        connection.push(newDto(PushRequest.class).withRefSpec(Collections.singletonList("refs/heads/master:refs/heads/test"))
                                                 .withRemote(remoteRepo.getAbsolutePath())
                                                 .withTimeout(-1));
        //then
        int branchesAfter = remoteConnection.branchList(newDto(BranchListRequest.class)).size();
        assertEquals(branchesAfter - 1, branchesBefore);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class,
            expectedExceptions = GitException.class, expectedExceptionsMessageRegExp = "No remote repository specified.  " +
            "Please, specify either a URL or a remote name from which new revisions should be fetched in request.")
    public void testWhenThereAreNoAnyRemotes(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

        //when
        PushRequest request = newDto(PushRequest.class);
        connection.push(request);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testPushWhenLocalRepositoryIsNotSynchronisedWithRemote(GitConnectionFactory connectionFactory)
            throws IOException, ServerException, URISyntaxException, UnauthorizedException {
        //given
        GitConnection remoteConnection = connectToGitRepositoryWithContent(connectionFactory, repository);
        GitConnection localConnection = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
        localConnection.clone(newDto(CloneRequest.class).withRemoteUri(remoteConnection.getWorkingDir().getAbsolutePath()));
        addFile(remoteConnection, "newfile", "content");
        remoteConnection.add(newDto(AddRequest.class).withFilepattern(Collections.singletonList(".")));
        remoteConnection.commit(newDto(CommitRequest.class).withMessage("Fake commit"));

        //when
        String errorMessage = "";
        try {
            localConnection.push(newDto(PushRequest.class).withRemote("origin").withTimeout(-1));
        } catch (GitException exception) {
            errorMessage = exception.getMessage();
        }

        //then
        assertTrue(errorMessage.contains("master -> master"));
        assertTrue(errorMessage.contains(remoteConnection.getWorkingDir().getAbsolutePath()));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testPushWhenLocalRepositoryIsUpToDate(GitConnectionFactory connectionFactory)
            throws IOException, ServerException, URISyntaxException, UnauthorizedException {
        //given
        GitConnection remoteConnection = connectToGitRepositoryWithContent(connectionFactory, repository);
        GitConnection localConnection = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
        localConnection.clone(newDto(CloneRequest.class).withRemoteUri(remoteConnection.getWorkingDir().getAbsolutePath()));

        //when
        PushResponse pushResponse = localConnection.push(newDto(PushRequest.class)
                                                                 .withRemote("origin")
                                                                 .withTimeout(-1));

        //then
        assertEquals(pushResponse.getCommandOutput(), "Everything up-to-date");
    }
}
