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
package org.eclipse.che.ide.maven.tools;


import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author andrew00x */
public class MavenUtilTest {

    @Test
    public void testGetModules() throws Exception {
        URL project = Thread.currentThread().getContextClassLoader().getResource("multi-module");
        Assert.assertNotNull(project);
        List<Model> modules = MavenUtils.getModules(new File(project.getFile()));
        List<String> expected = Arrays.asList("parent:module1:jar:x.x.x",
                                              "parent:module2:jar:x.x.x",
                                              "project:project-modules-x:pom:x.x.x",
                                              "project:module3:jar:x.x.x",
                                              "project:module4:jar:x.x.x");
        Assert.assertEquals(expected.size(), modules.size());
        List<String> modulesStr = new ArrayList<>(modules.size());
        for (Model model : modules) {
            modulesStr.add(toString(model));
        }
        modulesStr.removeAll(expected);
        Assert.assertTrue("Unexpected modules " + modules, modulesStr.isEmpty());
    }

    private String toString(Model model) {
        String groupId = model.getGroupId();
        if (groupId == null) {
            Parent parent = model.getParent();
            if (parent != null) {
                groupId = parent.getGroupId();
            }
        }
        String version = model.getVersion();
        if (version == null) {
            Parent parent = model.getParent();
            if (parent != null) {
                version = parent.getVersion();
            }
        }
        return groupId + ":" + model.getArtifactId() + ":" + model.getPackaging() + ":" + version;
    }
}
