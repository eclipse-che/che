/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.RmParams;
import org.eclipse.che.api.git.shared.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


import java.io.File;

import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.eclipse.che.git.impl.GitTestUtil.deleteFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class StatusTest {

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
    public void testEmptyStatus(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertTrue(status.getAdded().isEmpty());
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getUntracked().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testUntracked(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "a content");
        addFile(connection, "b", "b content");
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getUntracked(), ImmutableList.of("a", "b"));
        assertTrue(status.getAdded().isEmpty());
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testUntrackedFolder(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection.getWorkingDir().toPath().resolve("new_directory"), "a", "content of a");
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getUntrackedFolders(), ImmutableList.of("new_directory"));
        assertTrue(status.getAdded().isEmpty());
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testAdded(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "a content");
        addFile(connection, "b", "b content");
        addFile(connection, "c", "c content");
        //add "a" and "b" files
        connection.add(AddParams.create(ImmutableList.of("a", "b")));
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getAdded(), ImmutableList.of("a", "b"));
        assertEquals(status.getUntracked(), ImmutableList.of("c"));
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testModified(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "a content");
        addFile(connection, "b", "b content");
        //add "a" and "b"
        connection.add(AddParams.create(ImmutableList.of("a", "b")));
        //modify "a"
        addFile(connection, "a", "new content of a");
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getModified(), ImmutableList.of("a"));
        assertTrue(status.getUntracked().isEmpty());
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testChanged(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "a content");
        addFile(connection, "b", "b content");
        //add "a" and "b"
        connection.add(AddParams.create(ImmutableList.of("a", "b")));
        //commit "a" and "b"
        connection.commit(CommitParams.create("add 2 test files"));
        //modify "a"
        addFile(connection, "a", "modified content of a");
        //add "a"
        connection.add(AddParams.create(ImmutableList.of("a")));
        //change "a"
        addFile(connection, "a", "changed content of a");
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getChanged(), ImmutableList.of("a"));
        assertTrue(status.getAdded().isEmpty());
        assertTrue(status.getUntracked().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testConflicting(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "a content");
        addFile(connection, "b", "b content");
        //add "a" and "b"
        connection.add(AddParams.create(ImmutableList.of("a", "b")));
        //commit "a" and "b"
        connection.commit(CommitParams.create("add 2 test files"));
        //switch to other branch
        connection.checkout(CheckoutParams.create("new_branch").withCreateNew(true));
        //modify and commit "a"
        addFile(connection, "a", "new_branch a content");
        connection.commit(CommitParams.create("a changed in new_branch").withAll(true));
        //switch back to master
        connection.checkout(CheckoutParams.create("master"));
        //modify and commit "a"
        addFile(connection, "a", "master content");
        connection.commit(CommitParams.create("a changed in master").withAll(true));
        //merge with "new_branch" to get conflict
        connection.merge("new_branch");
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getConflicting(), ImmutableList.of("a"));
        assertTrue(status.getModified().isEmpty());
        assertTrue(status.getAdded().isEmpty());
        assertTrue(status.getUntracked().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testMissing(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "content of a");
        //add "a"
        connection.add(AddParams.create(ImmutableList.of("a")));
        //delete "a"
        deleteFile(connection, "a");
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getMissing(), ImmutableList.of("a"));
        assertEquals(status.getAdded(), ImmutableList.of("a"));
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getUntracked().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testRemovedFromFilesSystem(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "a content");
        addFile(connection, "b", "b content");
        //add "a" and "b"
        connection.add(AddParams.create(ImmutableList.of("a", "b")));
        //commit "a" and "b"
        connection.commit(CommitParams.create("add 2 test files"));
        //delete "a"
        deleteFile(connection, "a");
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertTrue(status.getRemoved().isEmpty());
        assertTrue(status.getAdded().isEmpty());
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertEquals(status.getMissing(), ImmutableList.of("a"));
        assertTrue(status.getUntracked().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testRemovedFromIndex(GitConnectionFactory connectionFactory) throws Exception {
        //given
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "a", "a content");
        addFile(connection, "b", "b content");
        //add "a" and "b"
        connection.add(AddParams.create(ImmutableList.of("a", "b")));
        //commit "a" and "b"
        connection.commit(CommitParams.create("add 2 test files"));
        //remove "a" from index
        connection.rm(RmParams.create(ImmutableList.of("a")));
        //when
        final Status status = connection.status(StatusFormat.SHORT);
        //then
        assertEquals(status.getRemoved(), ImmutableList.of("a"));
        assertTrue(status.getAdded().isEmpty());
        assertTrue(status.getChanged().isEmpty());
        assertTrue(status.getConflicting().isEmpty());
        assertTrue(status.getMissing().isEmpty());
        assertTrue(status.getUntracked().isEmpty());
        assertTrue(status.getUntrackedFolders().isEmpty());
    }
}
