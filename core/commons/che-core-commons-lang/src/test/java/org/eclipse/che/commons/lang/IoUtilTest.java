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
package org.eclipse.che.commons.lang;

import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class IoUtilTest {
  @Test
  public void shouldListFileResources() throws Exception {
    List<String> resources = new ArrayList<>();
    IoUtil.listResources(
        getClass().getResource("/").toURI(), path -> resources.add(path.getFileName().toString()));

    assertTrue(resources.contains("logback-test.xml"));
    assertTrue(resources.contains("findbugs-exclude.xml"));
  }

  @Test
  public void shouldListChildrenResourcesInJar() throws Exception {
    URL testJar = Thread.currentThread().getContextClassLoader().getResource("che/che.jar");
    URI codenvyDir = URI.create("jar:" + testJar + "!/codenvy");

    List<String> resources = new ArrayList<>();
    IoUtil.listResources(codenvyDir, path -> resources.add(path.getFileName().toString()));

    assertTrue(resources.contains("a.json"));
    assertTrue(resources.contains("b.json"));
  }

  @Test
  public void shouldListParentResourcesInJar() throws Exception {
    URL testJar = Thread.currentThread().getContextClassLoader().getResource("che/che.jar");
    URI codenvyDir = URI.create("jar:" + testJar + "!/");

    List<String> resources = new ArrayList<>();
    IoUtil.listResources(
        codenvyDir, path -> resources.add(path.getFileName().toString().replace("/", "")));

    assertTrue(resources.contains("codenvy"));
  }
}
