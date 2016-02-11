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
//package org.eclipse.che.ide.extension.maven.server.projecttype.handler;
//
//import org.eclipse.che.api.core.ServerException;
//import org.eclipse.che.api.project.server.type.AttributeValue;
//import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
//import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_GENERATION_STRATEGY;
//
///** @author Artem Zatsarynnyi */
//@RunWith(MockitoJUnitRunner.class)
//public class ArchetypeProjectGeneratorTest {
//
//    private final VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
//    private ArchetypeGenerationStrategy archetypeGenerationStrategy;
//
//    @Before
//    public void setUp() throws Exception {
//        archetypeGenerationStrategy =
//                new ArchetypeGenerationStrategy(new String[]{"http://localhost:8080/api/internal/builder"}, vfsRegistry);
//    }
//
//    @Test
//    public void testGetId() throws Exception {
//        Assert.assertEquals(ARCHETYPE_GENERATION_STRATEGY, archetypeGenerationStrategy.getId());
//    }
//
//    @Test(expected = ServerException.class)
//    public void shouldNotGenerateWhenRequiredAttributeMissed() throws Exception {
//        Map<String, AttributeValue> attributeValues = new HashMap<>();
//        attributeValues.put(MavenAttributes.GROUP_ID, new AttributeValue("my_group"));
//        attributeValues.put(MavenAttributes.PACKAGING, new AttributeValue("jar"));
//        attributeValues.put(MavenAttributes.VERSION, new AttributeValue("1.0-SNAPSHOT"));
//        attributeValues.put(MavenAttributes.SOURCE_FOLDER, new AttributeValue("src/main/java"));
//        attributeValues.put(MavenAttributes.TEST_SOURCE_FOLDER, new AttributeValue("src/test/java"));
//
//        Map<String, String> options = new HashMap<>();
//        options.put("type", MavenAttributes.ARCHETYPE_GENERATION_STRATEGY);
//
//        archetypeGenerationStrategy.generateProject(null, attributeValues, options);
//    }
//}