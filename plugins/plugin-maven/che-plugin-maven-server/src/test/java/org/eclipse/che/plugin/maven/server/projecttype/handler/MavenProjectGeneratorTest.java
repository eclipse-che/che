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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import org.eclipse.che.plugin.maven.shared.MavenAttributes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;


public class MavenProjectGeneratorTest {

    private MavenProjectGenerator mavenProjectGenerator;


    @Before
    public void setup() throws Exception {
        //VirtualFile mockFile = Mockito.mock(VirtualFile.class);
//        FolderEntry folderEntry = new FolderEntry(Mockito.anyString(), mockFile);

        GeneratorStrategy generatorStrategy = Mockito.mock(GeneratorStrategy.class);
        Mockito.when(generatorStrategy.getId()).thenReturn("foo");
        Set<GeneratorStrategy> strategies = new HashSet<>(1);
        strategies.add(generatorStrategy);
        mavenProjectGenerator = new MavenProjectGenerator(strategies);
    }

    @Test
    public void testGetProjectType() throws Exception {
        Assert.assertEquals(MavenAttributes.MAVEN_ID, mavenProjectGenerator.getProjectType());
    }

    @Test
    public void testOnCreateProject() throws Exception {

    }
}
