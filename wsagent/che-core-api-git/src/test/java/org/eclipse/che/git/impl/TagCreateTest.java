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
import org.eclipse.che.api.git.params.TagCreateParams;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.eclipse.che.git.impl.GitTestUtil.*;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
/**
 * @author Eugene Voevodin
 */
public class TagCreateTest {

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
    public void testCreateTag(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
        int beforeTagCount = connection.tagList(null).size();
        //when
        connection.tagCreate(TagCreateParams.create("v1").withMessage("first version"));
        //then
        int afterTagCount = connection.tagList(null).size();
        assertEquals(afterTagCount, beforeTagCount + 1);
    }
}
