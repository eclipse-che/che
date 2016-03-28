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

import org.eclipse.che.api.git.DiffPage;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.DiffRequest;
import org.eclipse.che.api.git.shared.RmRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.api.git.shared.DiffRequest.DiffType;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class DiffTest {
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
    public void testDiffNameStatus(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withType(DiffType.NAME_STATUS)
                                             .withRenameLimit(0),
                                     connection);
        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("M\taaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameStatusWithCommits(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);
        //change README.txt
        connection.add(newDto(AddRequest.class).withFilepattern(AddRequest.DEFAULT_PATTERN));
        connection.rm(newDto(RmRequest.class).withItems(Arrays.asList("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("testDiffNameStatusWithCommits"));

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(null)
                                             .withType(DiffType.NAME_STATUS)
                                             .withRenameLimit(0)
                                             .withCommitA("HEAD^")
                                             .withCommitB("HEAD"),
                                     connection);

        //then
        assertEquals(diff.size(), 2);
        assertTrue(diff.contains("D\tREADME.txt"));
        assertTrue(diff.contains("A\taaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameStatusWithFileFilterAndCommits(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList("aaa")));
        connection.rm(newDto(RmRequest.class).withItems(Arrays.asList("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("testDiffNameStatusWithCommits"));

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(Arrays.asList("aaa"))
                                             .withType(DiffType.NAME_STATUS)
                                             .withNoRenames(false)
                                             .withRenameLimit(0)
                                             .withCommitA("HEAD^1")
                                             .withCommitB("HEAD"),
                                     connection);

        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("A\taaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnly(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(null)
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0),
                                     connection);

        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("aaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnlyWithCommits(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList("aaa")));
        connection.rm(newDto(RmRequest.class).withItems(Arrays.asList("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("testDiffNameStatusWithCommits"));

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(null)
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0)
                                             .withCommitA("HEAD^1")
                                             .withCommitB("HEAD"),
                                     connection);

        //then
        assertEquals(diff.size(), 2);
        assertTrue(diff.contains("README.txt"));
        assertTrue(diff.contains("aaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnlyCached(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList("aaa")));
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(null)
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0)
                                             .withCommitA("HEAD")
                                             .withCached(true),
                                     connection);

        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("aaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnlyCachedNoCommit(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList("aaa")));
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(null)
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0)
                                             .withCommitA(null)
                                             .withCached(true),
                                     connection);

        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("aaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnlyWorkingTree(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(null)
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0)
                                             .withCommitA("HEAD")
                                             .withCached(false),
                                     connection);

        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("aaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnlyWithFileFilter(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(Arrays.asList("aaa"))
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0),
                                     connection);

        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("aaa"));
    }


    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnlyNotMatchingWithFileFilter(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(Arrays.asList("anotherFile"))
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0),
                                     connection);
        //then
        assertEquals(diff.size(), 0);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffNameOnlyWithFileFilterAndCommits(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList("aaa")));
        connection.rm(newDto(RmRequest.class).withItems(Arrays.asList("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("testDiffNameStatusWithCommits"));

        //when
        List<String> diff = readDiff(newDto(DiffRequest.class)
                                             .withFileFilter(Arrays.asList("aaa"))
                                             .withType(DiffType.NAME_ONLY)
                                             .withNoRenames(false)
                                             .withRenameLimit(0)
                                             .withCommitA("HEAD^1")
                                             .withCommitB("HEAD"),
                                     connection);

        //then
        assertEquals(diff.size(), 1);
        assertTrue(diff.contains("aaa"));
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffRaw(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        //when
        DiffRequest request = newDto(DiffRequest.class)
                .withFileFilter(null)
                .withType(DiffType.RAW)
                .withNoRenames(false)
                .withRenameLimit(0);
        DiffPage diffPage = connection.diff(request);

        //then
        diffPage.writeTo(System.out);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testDiffRawWithCommits(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        makeCommitInMaster(connection);

        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList("aaa")));
        connection.rm(newDto(RmRequest.class).withItems(Arrays.asList("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("testDiffNameStatusWithCommits"));

        //when
        DiffRequest request = newDto(DiffRequest.class)
                .withFileFilter(null)
                .withType(DiffType.RAW)
                .withNoRenames(false)
                .withRenameLimit(0)
                .withCommitA("HEAD^1")
                .withCommitB("HEAD");
        DiffPage diffPage = connection.diff(request);

        //then
        diffPage.writeTo(System.out);
    }

    private List<String> readDiff(DiffRequest request, GitConnection connection) throws GitException, IOException {
        DiffPage diffPage = connection.diff(request);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        diffPage.writeTo(out);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));

        String line;
        List<String> diff = new ArrayList<>();
        while ((line = reader.readLine()) != null)
            diff.add(line);

        return diff;
    }

    private void makeCommitInMaster(GitConnection connection) throws GitException, IOException {
        //create branch "master"
        addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
        connection.add(newDto(AddRequest.class).withFilepattern(ImmutableList.of("README.txt")));
        connection.commit(newDto(CommitRequest.class).withMessage("Initial addd"));

        //make some changes
        addFile(connection, "aaa", "AAA\n");
        connection.add(newDto(AddRequest.class).withFilepattern(Arrays.asList(".")));
        addFile(connection, "aaa", "BBB\n");
    }
}
