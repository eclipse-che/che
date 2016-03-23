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

import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.InitRequest;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RemoteListRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class RemoteAddTest {

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
    public void testSimpleRemoteAdd(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        int beforeCount = connection.remoteList(newDto(RemoteListRequest.class)).size();
        //when
        connection.remoteAdd(newDto(RemoteAddRequest.class).withName("origin").withUrl("some.url"));
        //then
        int afterCount = connection.remoteList(newDto(RemoteListRequest.class)).size();
        assertEquals(afterCount, beforeCount + 1);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testAddNotAllBranchesTracked(GitConnectionFactory connectionFactory)
            throws GitException, URISyntaxException, IOException, UnauthorizedException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        connection.branchCreate(newDto(BranchCreateRequest.class).withName("b1"));
        connection.branchCreate(newDto(BranchCreateRequest.class).withName("b2"));
        connection.branchCreate(newDto(BranchCreateRequest.class).withName("b3"));

        GitConnection connection2 = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
        connection2.init(newDto(InitRequest.class).withBare(false));
        //when
        //add remote tracked only to b1 and b3 branches.
        RemoteAddRequest remoteAddRequest = newDto(RemoteAddRequest.class)
                .withName("origin")
                .withUrl(connection.getWorkingDir().getAbsolutePath());
        remoteAddRequest.setBranches(Arrays.asList("b1", "b3"));
        connection2.remoteAdd(remoteAddRequest);
        //then
        //make pull
        connection2.pull(newDto(PullRequest.class).withRemote("origin"));

        assertTrue(Sets.symmetricDifference(
                Sets.newHashSet(connection2.branchList(newDto(BranchListRequest.class)
                                                               .withListMode(BranchListRequest.LIST_REMOTE))),
                Sets.newHashSet(newDto(Branch.class).withName("refs/remotes/origin/b1")
                                                    .withDisplayName("origin/b1")
                                                    .withActive(false)
                                                    .withRemote(true),
                                newDto(Branch.class).withName("refs/remotes/origin/b3")
                                                    .withDisplayName("origin/b3")
                                                    .withActive(false)
                                                    .withRemote(true))).isEmpty());
    }
}