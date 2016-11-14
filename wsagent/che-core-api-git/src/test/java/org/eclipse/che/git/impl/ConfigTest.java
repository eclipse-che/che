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
import org.eclipse.che.api.git.exception.GitException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;

/**
 * @author Eugene Voevodin
 */
public class ConfigTest {

    private static final String PROPERTY_NAME         = "test.prop";
    private static final String INVALID_PROPERTY_NAME = "someInvalidProperty";
    private static final String PROPERTY_VALUE        = "testValue";

    private static File repository;

    @BeforeMethod
    public void setUp() {
        repository = Files.createTempDir();
    }

    @AfterMethod
    public void cleanUp() {
        cleanupTestRepo(repository);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testAddProperty(GitConnectionFactory connectionFactory) throws GitException, IOException {
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

        //when
        //write value
        connection.getConfig().add(PROPERTY_NAME, PROPERTY_VALUE);

        //then
        //read written value
        String resultValue = connection.getConfig().get(PROPERTY_NAME);
        assertEquals(resultValue, PROPERTY_VALUE);

        //clear
        connection.getConfig().unset(PROPERTY_NAME);
    }

    @Test(expectedExceptions = GitException.class,
          expectedExceptionsMessageRegExp = "error: key does not contain a section: " + INVALID_PROPERTY_NAME + "\n",
          dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testShouldWarnOnInvalidPropertySetting(GitConnectionFactory connectionFactory) throws Exception {
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

        connection.getConfig().add(INVALID_PROPERTY_NAME, PROPERTY_VALUE);
    }

    @Test(expectedExceptions = GitException.class, expectedExceptionsMessageRegExp = "Can not find property '" + PROPERTY_NAME +
                                                                                     "' in Git configuration settings.",
          dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testShouldReturnEmptyValueForParameter(GitConnectionFactory connectionFactory) throws Exception {
        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

        connection.getConfig().get(PROPERTY_NAME);
    }
}
