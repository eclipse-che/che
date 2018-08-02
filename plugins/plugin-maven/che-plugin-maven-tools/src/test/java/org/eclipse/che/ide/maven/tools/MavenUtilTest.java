/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.maven.tools;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** @author andrew00x */
public class MavenUtilTest {

  @Test
  public void testGetModules() throws Exception {
    URL project = Thread.currentThread().getContextClassLoader().getResource("multi-module");
    Assert.assertNotNull(project);
    List<Model> modules = MavenUtils.getModules(new File(project.getFile()));
    List<String> expected =
        Arrays.asList(
            "parent:module1:jar:x.x.x",
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
