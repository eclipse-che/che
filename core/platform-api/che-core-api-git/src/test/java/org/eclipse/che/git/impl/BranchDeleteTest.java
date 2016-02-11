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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchDeleteRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_LOCAL;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 * @author Mihail Kuznyetsov
 */
public class BranchDeleteTest {

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
    public void testSimpleDelete(GitConnectionFactory connectionFactory) throws GitException, IOException, UnauthorizedException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));
        connection.branchCreate(newDto(BranchCreateRequest.class).withName("newbranch"));

        assertTrue(Sets.symmetricDifference(
                Sets.newHashSet(
                        connection.branchList(newDto(BranchListRequest.class).withListMode(LIST_LOCAL))),
                Sets.newHashSet(
                        newDto(Branch.class).withName("refs/heads/master")
                                .withDisplayName("master").withActive(true).withRemote(false),
                        newDto(Branch.class).withName("refs/heads/newbranch")
                                .withDisplayName("newbranch").withActive(false).withRemote(false)
                )
        ).isEmpty());
        //when
        connection.branchDelete(newDto(BranchDeleteRequest.class).withName("newbranch").withForce(false));
        //then
        assertTrue(Sets.symmetricDifference(
                Sets.newHashSet(
                        connection.branchList(newDto(BranchListRequest.class).withListMode(LIST_LOCAL))),
                Sets.newHashSet(
                        newDto(Branch.class).withName("refs/heads/master")
                                .withDisplayName("master").withActive(true).withRemote(false))
        ).isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void shouldDeleteNotFullyMergedBranchWithForce(GitConnectionFactory connectionFactory)
            throws GitException, IOException, UnauthorizedException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));
        //create new branch and make a commit
        connection.checkout(newDto(CheckoutRequest.class).withName("newbranch").withCreateNew(true));
        addFile(connection, "newfile", "new file content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("second commit"));
        connection.checkout(newDto(CheckoutRequest.class).withName("master"));

        //when
        connection.branchDelete(newDto(BranchDeleteRequest.class).withName("newbranch").withForce(true));

        //then
        assertTrue(Sets.symmetricDifference(
                Sets.newHashSet(
                        connection.branchList(newDto(BranchListRequest.class).withListMode(LIST_LOCAL))),
                Sets.newHashSet(
                                newDto(Branch.class).withName("refs/heads/master")
                                        .withDisplayName("master").withActive(true).withRemote(false)
                )
        ).isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
            expectedExceptions = GitException.class)
    public void shouldThrowExceptionOnDeletingNotFullyMergedBranchWithoutForce(GitConnectionFactory connectionFactory)
            throws GitException, IOException, UnauthorizedException, NoSuchFieldException, IllegalAccessException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));
        //create new branch and make a commit
        connection.checkout(newDto(CheckoutRequest.class).withName("newbranch").withCreateNew(true));
        addFile(connection, "newfile", "new file content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("second commit"));
        connection.checkout(newDto(CheckoutRequest.class).withName("master"));

        connection.branchDelete(newDto(BranchDeleteRequest.class).withName("newbranch").withForce(false));
    }
}
