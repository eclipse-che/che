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
import org.eclipse.che.api.git.shared.TagCreateRequest;
import org.eclipse.che.api.git.shared.TagListRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
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
        int beforeTagCount = connection.tagList(newDto(TagListRequest.class)).size();
        //when
        TagCreateRequest request = newDto(TagCreateRequest.class);
        request.setName("v1");
        request.setMessage("first version");
        connection.tagCreate(request);
        //then
        int afterTagCount = connection.tagList(newDto(TagListRequest.class)).size();
        assertEquals(afterTagCount, beforeTagCount + 1);
    }
}
