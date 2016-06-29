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
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.LogRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static java.nio.file.Files.write;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;

/**
 * @author Eugene Voevodin
 */
public class CommitTest {
    private File repository;
    private String CONTENT = "git repository content\n";

    @BeforeMethod
    public void setUp() {
        repository = Files.createTempDir();
    }

    @AfterMethod
    public void cleanUp() {
        cleanupTestRepo(repository);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testSimpleCommit(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        //add new File
        addFile(connection, "DONTREADME", "secret");
        //add changes
        connection.add(newDto(AddRequest.class).withFilepattern(AddRequest.DEFAULT_PATTERN));

        //when
        CommitRequest commitRequest = newDto(CommitRequest.class)
                .withMessage("Commit message").withAmend(false).withAll(false);
        Revision revision = connection.commit(commitRequest);

        //then
        assertEquals(revision.getMessage(), commitRequest.getMessage());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testCommitWithAddAll(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));

        //when
        //change existing README
        addFile(connection, "README.txt", "not secret");

        //then
        CommitRequest commitRequest = newDto(CommitRequest.class)
                .withMessage("Other commit message").withAmend(false).withAll(true);
        Revision revision = connection.commit(commitRequest);
        assertEquals(revision.getMessage(), commitRequest.getMessage());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testAmendCommit(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));
        int beforeCount = connection.log(newDto(LogRequest.class)).getCommits().size();

        //when
        //change existing README
        addFile(connection, "README.txt", "some new content");
        CommitRequest commitRequest = newDto(CommitRequest.class)
                .withMessage("Amend commit").withAmend(true).withAll(true);

        //then
        Revision revision = connection.commit(commitRequest);
        int afterCount = connection.log(newDto(LogRequest.class)).getCommits().size();
        assertEquals(revision.getMessage(), commitRequest.getMessage());
        assertEquals(beforeCount, afterCount);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
          expectedExceptions = GitException.class)
    public void testCommitWithNotStagedChanges(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        //Prepare unstaged deletion
        addFile(connection, "FileToDelete.txt", "content");
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("FileToDelete.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("File to delete"));
        new File(connection.getWorkingDir().getAbsolutePath(), "FileToDelete.txt").delete();
        //Prepare unstaged new file
        addFile(connection, "newFile", "content");
        //Prepare unstaged editing
        write(new File(connection.getWorkingDir(), "README.txt").toPath(), "new content".getBytes());

        //when
        connection.commit(newDto(CommitRequest.class)).withMessage("test commit");
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
          expectedExceptions = GitException.class)
    public void testCommitWithCleanIndex(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

        //when
        connection.commit(newDto(CommitRequest.class)).withMessage("test commit");
    }
}
