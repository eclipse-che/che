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
import org.eclipse.che.api.git.shared.PullRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

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
public class PullTest {

    public static final String UNKNOWN = "UNKNOWN";
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

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testSimplePull(GitConnectionFactory connectionFactory)
            throws IOException, ServerException, URISyntaxException, UnauthorizedException {
        //given
        //create new repository clone of default
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        GitConnection connection2 = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
        connection2.clone(newDto(CloneRequest.class)
                                  .withRemoteUri(connection.getWorkingDir().getAbsolutePath()));
        addFile(connection, "newfile1", "new file1 content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("Test commit"));
        //when
        connection2.pull(newDto(PullRequest.class).withRemote("origin").withTimeout(-1));
        //then
        assertTrue(new File(remoteRepo.getAbsolutePath(), "newfile1").exists());
    }


    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testPullWithRefSpec(GitConnectionFactory connectionFactory)
            throws ServerException, URISyntaxException, IOException, UnauthorizedException {
        //given
        //create new repository clone of default
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        GitConnection connection2 = connectionFactory.getConnection(remoteRepo.getAbsolutePath());

        connection2.clone(newDto(CloneRequest.class).withRemoteUri(connection.getWorkingDir().getAbsolutePath()));
        //add new branch
        connection.checkout(newDto(CheckoutRequest.class).withName("b1").withCreateNew(true));
        addFile(connection, "newfile1", "new file1 content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("Test commit"));
        int branchesBefore = connection2.branchList(newDto(BranchListRequest.class)).size();
        //when
        connection2.pull(newDto(PullRequest.class)
                                 .withRemote("origin")
                                 .withRefSpec("refs/heads/b1:refs/heads/b1")
                                 .withTimeout(-1));
        int branchesAfter = connection2.branchList(newDto(BranchListRequest.class).withListMode(BranchListRequest.LIST_LOCAL)).size();
        assertEquals(branchesAfter, branchesBefore + 1);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testPullRemote(GitConnectionFactory connectionFactory)
            throws GitException, IOException, URISyntaxException, UnauthorizedException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        String branchName = "remoteBranch";
        connection.checkout(newDto(CheckoutRequest.class).withCreateNew(true).withName(branchName));
        addFile(connection, "remoteFile", "");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("remote test"));

        GitConnection connection2 = connectToInitializedGitRepository(connectionFactory, remoteRepo);
        addFile(connection2, "EMPTY", "");
        connection2.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection2.commit(newDto(CommitRequest.class).withMessage("init"));

        //when
        PullRequest request = newDto(PullRequest.class);
        request.setRemote(connection.getWorkingDir().getAbsolutePath());
        request.setRefSpec(branchName);
        connection2.pull(request);
        //then
        assertTrue(new File(remoteRepo.getAbsolutePath(), "remoteFile").exists());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class,
            expectedExceptions = GitException.class, expectedExceptionsMessageRegExp = "No remote repository specified.  " +
            "Please, specify either a URL or a remote name from which new revisions should be fetched in request.")
    public void testWhenThereAreNoAnyRemotes(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

        //when
        PullRequest request = newDto(PullRequest.class);
        connection.pull(request);
    }
}
