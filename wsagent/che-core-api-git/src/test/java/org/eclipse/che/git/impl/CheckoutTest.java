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


import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class CheckoutTest {
    private static final String FIRST_BRANCH_NAME  = "firstBranch";
    private static final String SECOND_BRANCH_NAME = "secondBranch";

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
    public void testSimpleCheckout(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));

        //when
        //create additional branch and make a commit
        connection.branchCreate(newDto(BranchCreateRequest.class).withName(FIRST_BRANCH_NAME));
        connection.checkout(newDto(CheckoutRequest.class).withName(FIRST_BRANCH_NAME));
        addFile(connection, "newfile", "new file content");
        connection.add(newDto(AddRequest.class).withFilepattern(AddRequest.DEFAULT_PATTERN));
        connection.commit(newDto(CommitRequest.class).withMessage("Commit message"));
        connection.checkout(newDto(CheckoutRequest.class).withName("master"));
        //then
        assertFalse(new File(repository, "newf3ile").exists());

        //when
        connection.checkout(newDto(CheckoutRequest.class).withName(FIRST_BRANCH_NAME));
        //then
        assertTrue(new File(repository, "newfile").exists());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testSimpleFileCheckout(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));

        //when
        //modify a file
        String MODIFIED_CONTENT = "README modified content";
        addFile(connection, "README.txt", MODIFIED_CONTENT);
        
        //then
        assertTrue(new File(repository, "README.txt").exists());
        assertEquals(MODIFIED_CONTENT, Files.toString(new File(connection.getWorkingDir(), "README.txt"), Charsets.UTF_8));
        
        //when
        connection.checkout(newDto(CheckoutRequest.class).withFiles(Arrays.asList("README.txt")));

        //then
        assertTrue(new File(repository, "README.txt").exists());
        assertEquals(org.eclipse.che.git.impl.GitTestUtil.CONTENT, Files.toString(new File(connection.getWorkingDir(), "README.txt"), Charsets.UTF_8));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testCheckoutTwoFiles(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
  
        String ORIG_CONTENT_1_TXT = "1.txt original content";
        String ORIG_CONTENT_2_TXT = "2.txt original content";
        addFile(connection, "1.txt", ORIG_CONTENT_1_TXT);
        addFile(connection, "2.txt", ORIG_CONTENT_2_TXT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt", "1.txt", "2.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));

        //when
        //modify the two files
        String MODIFIED_CONTENT_1_TXT = "1.txt modified content";
        String MODIFIED_CONTENT_2_TXT = "2.txt modified content";
        addFile(connection, "1.txt", MODIFIED_CONTENT_1_TXT);
        addFile(connection, "2.txt", MODIFIED_CONTENT_2_TXT);
        
        //then
        assertTrue(new File(repository, "1.txt").exists());
        assertTrue(new File(repository, "2.txt").exists());
        assertEquals(MODIFIED_CONTENT_1_TXT, Files.toString(new File(connection.getWorkingDir(), "1.txt"), Charsets.UTF_8));
        assertEquals(MODIFIED_CONTENT_2_TXT, Files.toString(new File(connection.getWorkingDir(), "2.txt"), Charsets.UTF_8));
        
        //when
        connection.checkout(newDto(CheckoutRequest.class).withFiles(ImmutableList.of("1.txt", "2.txt")));

        //then
        assertTrue(new File(repository, "1.txt").exists());
        assertTrue(new File(repository, "2.txt").exists());
        assertEquals(ORIG_CONTENT_1_TXT, Files.toString(new File(connection.getWorkingDir(), "1.txt"), Charsets.UTF_8));
        assertEquals(ORIG_CONTENT_2_TXT, Files.toString(new File(connection.getWorkingDir(), "2.txt"), Charsets.UTF_8));
    }

    
    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testCreateNewAndCheckout(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));

        //check existence of branch master
        assertEquals(connection.branchList(newDto(BranchListRequest.class)).size(), 1);

        //when
        connection.checkout(newDto(CheckoutRequest.class).withName("thirdBranch").withCreateNew(true));

        //then
        assertEquals(connection.branchList(newDto(BranchListRequest.class)).size(), 2);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testCheckoutFromStartPoint(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));

        //when
        //create branch additional branch and make a commit
        connection.branchCreate(newDto(BranchCreateRequest.class).withName(FIRST_BRANCH_NAME));
        connection.checkout(newDto(CheckoutRequest.class).withName(FIRST_BRANCH_NAME));
        addFile(connection, "newfile", "new file content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("Commit message"));
        connection.checkout(newDto(CheckoutRequest.class).withName("master"));

        //check existence of 2 branches
        assertEquals(connection.branchList(newDto(BranchListRequest.class)).size(), 2);

        //when
        connection.checkout(newDto(CheckoutRequest.class)
                                          .withName(SECOND_BRANCH_NAME)
                                          .withStartPoint(FIRST_BRANCH_NAME)
                                          .withCreateNew(true));
        //then
        assertEquals(connection.branchList(newDto(BranchListRequest.class)).size(), 3);
        assertTrue(new File(repository, "newfile").exists());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testTrackRemoteBranch(GitConnectionFactory connectionFactory) throws GitException, IOException, UnauthorizedException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial add"));

        //when
        //create branch additional branch and make a commit
        connection.branchCreate(newDto(BranchCreateRequest.class).withName(FIRST_BRANCH_NAME));
        connection.checkout(newDto(CheckoutRequest.class).withName(FIRST_BRANCH_NAME));
        addFile(connection, "newfile", "new file content");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        connection.commit(newDto(CommitRequest.class).withMessage("Commit message"));
        connection.checkout(newDto(CheckoutRequest.class).withName("master"));

        //check existence of 2 branches
        assertEquals(connection.branchList(newDto(BranchListRequest.class)).size(), 2);

        //when
        connection.checkout(newDto(CheckoutRequest.class)
                                          .withCreateNew(true)
                                          .withName(SECOND_BRANCH_NAME)
                                          .withTrackBranch(FIRST_BRANCH_NAME));
        //then
        assertEquals(connection.branchList(newDto(BranchListRequest.class)).size(), 3);
        assertTrue(new File(repository, "newfile").exists());
    }
}
