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

import com.google.common.io.Files;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.RmParams;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class RemoveTest {

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
    public void testNotCachedRemove(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        //when
        connection.rm(RmParams.create(singletonList("README.txt")).withCached(false));
        //then
        assertFalse(new File(connection.getWorkingDir(), "README.txt").exists());
        assertEquals(connection.status(StatusFormat.SHORT).getRemoved().get(0), "README.txt");
        assertTrue(connection.status(StatusFormat.SHORT).getUntracked().isEmpty());
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testCachedRemove(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        //when
        connection.rm(RmParams.create(singletonList("README.txt")).withCached(true));
        //then
        assertTrue(new File(connection.getWorkingDir(), "README.txt").exists());
        assertEquals(connection.status(StatusFormat.SHORT).getRemoved().get(0), "README.txt");
        assertEquals(connection.status(StatusFormat.SHORT).getUntracked().get(0), "README.txt");
    }
}

