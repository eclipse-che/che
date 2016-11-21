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
import org.eclipse.che.api.git.shared.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class TagListTest {

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
    public void testTagList(GitConnectionFactory connectionFactory) throws GitException, IOException {
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

        TagCreateRequest firstTag = newDto(TagCreateRequest.class);
        firstTag.setName("first-tag");
        connection.tagCreate(firstTag);
        TagCreateRequest firstTagOther = newDto(TagCreateRequest.class);
        firstTagOther.setName("first-tag-other");
        connection.tagCreate(firstTagOther);
        TagCreateRequest secondTag = newDto(TagCreateRequest.class);
        secondTag.setName("second-tag");
        connection.tagCreate(secondTag);

        assertTags(connection.tagList(
                newDto(TagListRequest.class)), "first-tag", "first-tag-other", "second-tag");
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
    public void testTagListPattern(GitConnectionFactory connectionFactory) throws GitException, IOException {
        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

        TagCreateRequest firstTag = newDto(TagCreateRequest.class);
        firstTag.setName("first-tag");
        connection.tagCreate(firstTag);
        TagCreateRequest firstTagOther = newDto(TagCreateRequest.class);
        firstTagOther.setName("first-tag-other");
        connection.tagCreate(firstTagOther);
        TagCreateRequest secondTag = newDto(TagCreateRequest.class);
        secondTag.setName("second-tag");
        connection.tagCreate(secondTag);

        TagListRequest request = newDto(TagListRequest.class);
        request.setPattern("first*");

        assertTags(connection.tagList(request), "first-tag", "first-tag-other");
    }

    protected void assertTags(List<Tag> tagList, String... expNames) {
        assertEquals(tagList.size(), expNames.length);
        List<String> names = new ArrayList<>(tagList.size());
        for (Tag t : tagList)
            names.add(t.getName());
        for (String name : expNames)
            assertTrue(names.contains(name), "Expected tag " + name + " not found in result. ");
    }
}
