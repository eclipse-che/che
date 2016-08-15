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
import com.google.common.io.Files;

import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.shared.Revision;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;

/**
 * @author Igor Vinokur
 */
public class LogTest {
    private File repository;

    @BeforeMethod
    public void setUp() {
        repository = Files.createTempDir();
    }

    @AfterMethod
    public void cleanUp() {
        cleanupTestRepo(repository);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testSimpleLog(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", "someChanges");
        connection.add(AddParams.create(ImmutableList.of("README.txt")));
        connection.commit(CommitParams.create("Initial add"));

        addFile(connection, "README.txt", "newChanges");
        connection.add(AddParams.create(ImmutableList.of("README.txt")));
        connection.commit(CommitParams.create("Second commit"));

        addFile(connection, "README.txt", "otherChanges");
        connection.add(AddParams.create(ImmutableList.of("README.txt")));
        connection.commit(CommitParams.create("Third commit"));

        //when
        List<Revision> commits = connection.log(LogParams.create()).getCommits();

        //then
        assertEquals("Third commit", commits.get(0).getMessage());
        assertEquals("Second commit", commits.get(1).getMessage());
        assertEquals("Initial add", commits.get(2).getMessage());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testLogWithFileFilter(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", "someChanges");
        connection.add(AddParams.create(ImmutableList.of("README.txt")));
        connection.commit(CommitParams.create("Initial add"));

        addFile(connection, "README.txt", "newChanges");
        connection.add(AddParams.create(ImmutableList.of("README.txt")));
        connection.commit(CommitParams.create("Second commit"));

        addFile(connection, "README.txt", "otherChanges");
        connection.add(AddParams.create(ImmutableList.of("README.txt")));
        connection.commit(CommitParams.create("Third commit"));

        addFile(connection, "newFile.txt", "someChanges");
        connection.add(AddParams.create(ImmutableList.of("newFile.txt")));
        connection.commit(CommitParams.create("Add newFile.txt"));


        //when
        int readMeCommitCount =
                connection.log(LogParams.create().withFileFilter(Collections.singletonList("README.txt"))).getCommits().size();
        int newFileCommitCount =
                connection.log(LogParams.create().withFileFilter(Collections.singletonList("newFile.txt"))).getCommits().size();
        List<String> fileFilter = new ArrayList<>();
        fileFilter.add("README.txt");
        fileFilter.add("newFile.txt");
        int allFilesCommitCount =
                connection.log(LogParams.create().withFileFilter(fileFilter)).getCommits().size();

        //then
        assertEquals(3, readMeCommitCount);
        assertEquals(1, newFileCommitCount);
        assertEquals(4, allFilesCommitCount);
    }
}
