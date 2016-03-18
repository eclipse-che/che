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
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.LsFilesRequest;
import org.eclipse.che.api.git.shared.RmRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

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
        connection.rm(newDto(RmRequest.class).withItems(Arrays.asList("README.txt")).withCached(false));
        //then
        assertFalse(new File(connection.getWorkingDir(), "README.txt").exists());
        assertNotCached(connection, "README.txt");
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testCachedRemove(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        //when
        connection.rm(newDto(RmRequest.class).withItems(Arrays.asList("README.txt")).withCached(true));
        //then
        assertTrue(new File(connection.getWorkingDir(), "README.txt").exists());
        assertNotCached(connection, "README.txt");
    }

    private void assertNotCached(GitConnection connection, String... fileNames) throws GitException {
        List<String> output = connection.listFiles(newDto(LsFilesRequest.class).withCached(true));
        for (String fName : fileNames) {
            if (output.contains(fName)) {
                fail("Cache contains " + fName);
            }
        }
    }
}

