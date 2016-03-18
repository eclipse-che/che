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
import org.eclipse.che.api.git.shared.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class TagDeleteTest {

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
    public void testDeleteTag(GitConnectionFactory connectionFactory) throws GitException, IOException {
        //given
        //create tags
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

        TagCreateRequest first = newDto(TagCreateRequest.class);
        first.setName("first-tag");
        connection.tagCreate(first);
        TagCreateRequest second = newDto(TagCreateRequest.class);
        second.setName("second-tag");
        connection.tagCreate(second);

        List<Tag> tags = connection.tagList(newDto(TagListRequest.class));
        assertTrue(tagExists(tags, "first-tag"));
        assertTrue(tagExists(tags, "second-tag"));
        //when
        //delete first-tag
        TagDeleteRequest request = newDto(TagDeleteRequest.class);
        request.setName("first-tag");
        connection.tagDelete(request);
        //then
        //check not exists more
        tags = connection.tagList(newDto(TagListRequest.class));
        assertFalse(tagExists(tags, "first-tag"));
        assertTrue(tagExists(tags, "second-tag"));
    }

    private boolean tagExists(List<Tag> list, String name) {
        for (Tag tag : list) {
            if (tag.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
