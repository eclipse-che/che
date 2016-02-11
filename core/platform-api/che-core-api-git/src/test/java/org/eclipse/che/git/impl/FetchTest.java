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
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.LogRequest;
import org.eclipse.che.api.git.shared.MergeRequest;
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
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class FetchTest {

    private File fetchTestRepo;
    private File repository;

    @BeforeMethod
    public void setUp() {
        repository = Files.createTempDir();
        fetchTestRepo = Files.createTempDir();
    }

    @AfterMethod
    public void cleanUp() {
        cleanupTestRepo(repository);
        cleanupTestRepo(fetchTestRepo);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testSimpleFetch(GitConnectionFactory connectionFactory)
            throws ServerException, IOException, UnauthorizedException, URISyntaxException {

        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        GitConnection fetchConnection = connectionFactory.getConnection(fetchTestRepo.getAbsolutePath());

        addFile(connection, "README", "readme content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("fetch test"));
        //clone default repo into fetchRepo
        fetchConnection.clone(newDto(CloneRequest.class).withRemoteUri(connection.getWorkingDir().getAbsolutePath())
                                                        .withWorkingDir(fetchConnection.getWorkingDir().getAbsolutePath()));

        //add new File into defaultRepository
        addFile(connection, "newfile1", "newfile1 content");
        //add file to index and make commit
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("fetch test"));

        //when
        fetchConnection.fetch(newDto(FetchRequest.class).withRemote(repository.getAbsolutePath()));

        //then
        //make merge with FETCH_HEAD
        fetchConnection.merge(newDto(MergeRequest.class).withCommit("FETCH_HEAD"));
        assertTrue(new File(fetchTestRepo, "newfile1").exists());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testFetchBranch(GitConnectionFactory connectionFactory)
            throws ServerException, IOException, UnauthorizedException, URISyntaxException {

        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        GitConnection fetchConnection = connectionFactory.getConnection(fetchTestRepo.getAbsolutePath());

        addFile(connection, "README", "readme content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("fetch test"));
        //clone default repo into fetchRepo
        fetchConnection.clone(newDto(CloneRequest.class).withRemoteUri(repository.getAbsolutePath()));

        //add new File into defaultRepository
        addFile(connection, "newfile1", "newfile1 content");
        //add file to index and make commit
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("fetch test"));

        String branchName = "branch";
        connection.checkout(newDto(CheckoutRequest.class).withCreateNew(true).withName(branchName));
        addFile(connection, "otherfile1", "otherfile1 content");
        addFile(connection, "otherfile2", "otherfile2 content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("fetch branch test"));

        //when
        fetchConnection.fetch(newDto(FetchRequest.class).withRemote(repository.getAbsolutePath()));
        //then
        //make merge with FETCH_HEAD
        fetchConnection.merge(newDto(MergeRequest.class).withCommit("FETCH_HEAD"));
        assertTrue(new File(fetchTestRepo, "otherfile1").exists());
        assertTrue(new File(fetchTestRepo, "otherfile2").exists());
        assertEquals(fetchConnection.log(newDto(LogRequest.class)).getCommits().get(0).getMessage(), "fetch branch test");
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class,
            expectedExceptions = GitException.class, expectedExceptionsMessageRegExp = "No remote repository specified.  " +
            "Please, specify either a URL or a remote name from which new revisions should be fetched in request.")
    public void testWhenThereAreNoAnyRemotes(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

        //when
        FetchRequest request = newDto(FetchRequest.class);
        connection.fetch(request);
    }
}
