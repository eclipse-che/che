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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.getTestUserConnection;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class InitTest {
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
    public void testInit(GitConnectionFactory connectionFactory) throws GitException, URISyntaxException, IOException {
        GitConnection connection = getTestUserConnection(connectionFactory, repository);

        //check git is not initialized
        assertFalse(new File(repository, ".git").exists());

        //when
        connection.init(false);

        //then
        assertTrue(new File(repository, ".git").exists());
    }
}
