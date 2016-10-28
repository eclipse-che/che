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
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.LogRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.deleteFile;
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
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial add"));

        addFile(connection, "README.txt", "newChanges");
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Second commit"));

        addFile(connection, "README.txt", "otherChanges");
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Third commit"));

        //when
        List<Revision> commits = connection.log(newDto(LogRequest.class)).getCommits();

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
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial add"));

        addFile(connection, "README.txt", "newChanges");
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Second commit"));

        addFile(connection, "README.txt", "otherChanges");
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Third commit"));

        addFile(connection, "newFile.txt", "someChanges");
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("newFile.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Add newFile.txt"));


        //when
        int readMeCommitCount =
                connection.log(newDto(LogRequest.class).withFileFilter(Collections.singletonList("README.txt"))).getCommits().size();
        int newFileCommitCount =
                connection.log(newDto(LogRequest.class).withFileFilter(Collections.singletonList("newFile.txt"))).getCommits().size();
        List<String> fileFilter = new ArrayList<>();
        fileFilter.add("README.txt");
        fileFilter.add("newFile.txt");
        int allFilesCommitCount =
                connection.log(newDto(LogRequest.class).withFileFilter(fileFilter)).getCommits().size();

        //then
        assertEquals(3, readMeCommitCount);
        assertEquals(1, newFileCommitCount);
        assertEquals(4, allFilesCommitCount);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testLogSkip(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "1.txt", "someChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 1.txt file"));

        addFile(connection, "2.txt", "newChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 2.txt file"));

        addFile(connection, "3.txt", "otherChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 3.txt file"));

        addFile(connection, "4.txt", "someChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 4.txt file"));

        //when
        List<Revision> allCommits =
            connection.log(newDto(LogRequest.class)).getCommits();
        List<Revision> firstBucketOfCommits =
            connection.log(newDto(LogRequest.class).withSkip(1)).getCommits();
        List<Revision> secondBucketOfCommits =
            connection.log(newDto(LogRequest.class).withSkip(3)).getCommits();

        //then
        assertEquals(4, allCommits.size());

        assertEquals(3, firstBucketOfCommits.size());
        assertEquals(firstBucketOfCommits.get(0).getMessage(), "add 3.txt file");
        assertEquals(firstBucketOfCommits.get(0).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(firstBucketOfCommits.get(0).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(firstBucketOfCommits.get(0).getDiffCommitFile().get(0).getNewPath(), "3.txt");
        assertEquals(firstBucketOfCommits.get(0).getDiffCommitFile().get(0).getChangeType(), "ADD");

        assertEquals(firstBucketOfCommits.get(1).getMessage(), "add 2.txt file");
        assertEquals(firstBucketOfCommits.get(1).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(firstBucketOfCommits.get(1).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(firstBucketOfCommits.get(1).getDiffCommitFile().get(0).getNewPath(), "2.txt");
        assertEquals(firstBucketOfCommits.get(1).getDiffCommitFile().get(0).getChangeType(), "ADD");

        assertEquals(firstBucketOfCommits.get(2).getMessage(), "add 1.txt file");
        assertEquals(firstBucketOfCommits.get(2).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(firstBucketOfCommits.get(2).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(firstBucketOfCommits.get(2).getDiffCommitFile().get(0).getNewPath(), "1.txt");
        assertEquals(firstBucketOfCommits.get(2).getDiffCommitFile().get(0).getChangeType(), "ADD");

        assertEquals(1, secondBucketOfCommits.size());

        assertEquals(secondBucketOfCommits.get(0).getMessage(), "add 1.txt file");
        assertEquals(secondBucketOfCommits.get(0).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(secondBucketOfCommits.get(0).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(secondBucketOfCommits.get(0).getDiffCommitFile().get(0).getNewPath(), "1.txt");
        assertEquals(secondBucketOfCommits.get(0).getDiffCommitFile().get(0).getChangeType(), "ADD");
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testLogMaxCount(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "1.txt", "someChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 1.txt file"));

        addFile(connection, "2.txt", "newChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 2.txt file"));

        addFile(connection, "3.txt", "otherChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 3.txt file"));

        addFile(connection, "4.txt", "someChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 4.txt file"));

        //when
        List<Revision> allCommits =
            connection.log(newDto(LogRequest.class)).getCommits();
        List<Revision> firstBucketOfCommits =
            connection.log(newDto(LogRequest.class).withMaxCount(4)).getCommits();
        List<Revision> secondBucketOfCommits =
            connection.log(newDto(LogRequest.class).withMaxCount(2)).getCommits();

        //then
        assertEquals(4, allCommits.size());

        assertEquals(4, firstBucketOfCommits.size());
        assertEquals(firstBucketOfCommits.get(0).getMessage(), "add 4.txt file");
        assertEquals(firstBucketOfCommits.get(1).getMessage(), "add 3.txt file");
        assertEquals(firstBucketOfCommits.get(2).getMessage(), "add 2.txt file");
        assertEquals(firstBucketOfCommits.get(3).getMessage(), "add 1.txt file");

        assertEquals(2, secondBucketOfCommits.size());
        assertEquals(secondBucketOfCommits.get(0).getMessage(), "add 4.txt file");
        assertEquals(secondBucketOfCommits.get(1).getMessage(), "add 3.txt file");
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testLogWithSkipAndMaxCount(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "1.txt", "someChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 1.txt file"));

        addFile(connection, "2.txt", "newChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 2.txt file"));

        addFile(connection, "3.txt", "otherChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 3.txt file"));

        addFile(connection, "4.txt", "someChanges");
        connection.add(newDto(AddRequest.class));
        connection.commit(newDto(CommitRequest.class).withMessage("add 4.txt file"));

        //when
        List<Revision> allCommits =
            connection.log(newDto(LogRequest.class)).getCommits();
        List<Revision> firstBacketOfCommits =
            connection.log(newDto(LogRequest.class).withSkip(1).withMaxCount(2)).getCommits();
        List<Revision> secondBacketOfCommits =
            connection.log(newDto(LogRequest.class).withSkip(2).withMaxCount(2)).getCommits();

        //then
        assertEquals(4, allCommits.size());

        assertEquals(2, firstBacketOfCommits.size());
        assertEquals(firstBacketOfCommits.get(0).getMessage(), "add 3.txt file");
        assertEquals(firstBacketOfCommits.get(0).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(firstBacketOfCommits.get(0).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(firstBacketOfCommits.get(0).getDiffCommitFile().get(0).getNewPath(), "3.txt");
        assertEquals(firstBacketOfCommits.get(0).getDiffCommitFile().get(0).getChangeType(), "ADD");

        assertEquals(firstBacketOfCommits.get(1).getMessage(), "add 2.txt file");
        assertEquals(firstBacketOfCommits.get(1).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(firstBacketOfCommits.get(1).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(firstBacketOfCommits.get(1).getDiffCommitFile().get(0).getNewPath(), "2.txt");
        assertEquals(firstBacketOfCommits.get(1).getDiffCommitFile().get(0).getChangeType(), "ADD");

        assertEquals(2, secondBacketOfCommits.size());

        assertEquals(secondBacketOfCommits.get(0).getMessage(), "add 2.txt file");
        assertEquals(secondBacketOfCommits.get(0).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(secondBacketOfCommits.get(0).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(secondBacketOfCommits.get(0).getDiffCommitFile().get(0).getNewPath(), "2.txt");
        assertEquals(secondBacketOfCommits.get(0).getDiffCommitFile().get(0).getChangeType(), "ADD");

        assertEquals(secondBacketOfCommits.get(1).getMessage(), "add 1.txt file");
        assertEquals(secondBacketOfCommits.get(1).getBranches().get(0).getName(), "refs/heads/master");
        assertEquals(secondBacketOfCommits.get(1).getDiffCommitFile().get(0).getOldPath(), "/dev/null");
        assertEquals(secondBacketOfCommits.get(1).getDiffCommitFile().get(0).getNewPath(), "1.txt");
        assertEquals(secondBacketOfCommits.get(1).getDiffCommitFile().get(0).getChangeType(), "ADD");
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testLogSince(GitConnectionFactory connectionFactory) throws GitException, IOException, InterruptedException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "1.txt", "someChanges");
        connection.add(newDto(AddRequest.class));
        String firstCommitId = connection.commit(newDto(CommitRequest.class).withMessage("add 1.txt file")).getId();

        addFile(connection, "2.txt", "secondChanges");
        connection.add(newDto(AddRequest.class));
        String secondCommitId = connection.commit(newDto(CommitRequest.class).withMessage("add 2.txt file")).getId();

        addFile(connection, "3.txt", "thirdChanges");
        connection.add(newDto(AddRequest.class));
        String thirdCommitId = connection.commit(newDto(CommitRequest.class).withMessage("add 3.txt file")).getId();

        addFile(connection, "4.txt", "fourthChanges");
        connection.add(newDto(AddRequest.class));
        String fourthCommitId = connection.commit(newDto(CommitRequest.class).withMessage("add 4.txt file")).getId();

        //when
        List<Revision> allCommits =
            connection.log(newDto(LogRequest.class)).getCommits();
        List<Revision> secondAndThirdAndFourthCommits =
            connection.log(newDto(LogRequest.class).withRevisionRangeSince(firstCommitId).withRevisionRangeUntil(fourthCommitId)).getCommits();
        List<Revision> thirdAndFourthCommits =
            connection.log(newDto(LogRequest.class).withRevisionRangeSince(secondCommitId).withRevisionRangeUntil(fourthCommitId)).getCommits();

        //then
        assertEquals(4, allCommits.size());
        assertEquals(3, secondAndThirdAndFourthCommits.size());
        assertEquals(2, thirdAndFourthCommits.size());


        assertEquals(secondAndThirdAndFourthCommits.get(0).getMessage(), "add 4.txt file");
        assertEquals(secondAndThirdAndFourthCommits.get(1).getMessage(), "add 3.txt file");
        assertEquals(secondAndThirdAndFourthCommits.get(2).getMessage(), "add 2.txt file");

        assertEquals(thirdAndFourthCommits.get(0).getMessage(), "add 4.txt file");
        assertEquals(thirdAndFourthCommits.get(1).getMessage(), "add 3.txt file");
    }
}
