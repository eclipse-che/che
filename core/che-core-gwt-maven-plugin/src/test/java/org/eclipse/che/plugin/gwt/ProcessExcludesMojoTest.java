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
package org.eclipse.che.plugin.gwt;

import static org.eclipse.che.plugin.gwt.ProcessExcludesMojo.FULL_IDE_GWT_MODULE_SUFFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Rule;
import org.junit.Test;

public class ProcessExcludesMojoTest {

  /** Rule to manage the mojo (set/get values of variables of mojo). */
  @Rule public MojoRule rule = new MojoRule();

  /** Resources of each test mapped on the name of the method. */
  @Rule public TestResources resources = new TestResources();

  /**
   * Injects data in mojo.
   *
   * @param mojo the mojo
   * @param baseDir root dir on which we extract files
   * @throws IllegalAccessException if unable to set variables
   */
  private void configureMojo(ProcessExcludesMojo mojo, File baseDir) throws Exception {
    rule.setVariableValueToObject(mojo, "outputDirectory", new File(baseDir, "classes"));
  }

  /**
   * Tests the ability of the plugin to process IDE GWT module (IDE.gwt.xml) to prevent inheriting
   * the GWT modules of the excluded IDE plugins.
   */
  @Test
  public void testProcessingExcludes() throws Exception {
    File projectCopy = resources.getBasedir("project");
    File pom = new File(projectCopy, "pom.xml");
    assertTrue(pom.exists());

    ProcessExcludesMojo mojo = (ProcessExcludesMojo) rule.lookupMojo("process-excludes", pom);
    assertNotNull(mojo);

    configureMojo(mojo, projectCopy);
    mojo.execute();

    File fullGwtXml =
        new File(
            projectCopy,
            "classes/org/eclipse/che/ide/Full" + FULL_IDE_GWT_MODULE_SUFFIX + ".gwt.xml");
    assertTrue(fullGwtXml.exists());

    String fullGwtXmlContent = FileUtils.fileRead(fullGwtXml);
    assertFalse(fullGwtXmlContent.contains("org.eclipse.che.ide.ext.help.HelpAboutExtension"));

    File ideGwtXml = new File(projectCopy, "classes/org/eclipse/che/ide/IDE.gwt.xml");
    assertTrue(ideGwtXml.exists());

    String ideGwtXmlContent = FileUtils.fileRead(ideGwtXml);
    assertFalse(ideGwtXmlContent.contains("<inherits name=\"org.eclipse.che.ide.Full\"/>"));
    assertTrue(
        ideGwtXmlContent.contains(
            "<inherits name=\"org.eclipse.che.ide.Full" + FULL_IDE_GWT_MODULE_SUFFIX + "\"/>"));
  }

  /**
   * Tests that plugins doesn't modify the IDE GWT module (IDE.gwt.xml) if there are no excluded
   * plugins.
   */
  @Test
  public void testWithoutExcludes() throws Exception {
    File projectCopy = resources.getBasedir("project-without-exclusions");
    File pom = new File(projectCopy, "pom.xml");
    assertTrue(pom.exists());

    ProcessExcludesMojo mojo = (ProcessExcludesMojo) rule.lookupMojo("process-excludes", pom);
    assertNotNull(mojo);

    configureMojo(mojo, projectCopy);
    mojo.execute();

    File outputDirectory = (File) rule.getVariableValueFromObject(mojo, "outputDirectory");
    File expected =
        new File(
            "src/test/projects/project-without-exclusions/classes/org/eclipse/che/ide/IDE.gwt.xml");
    File actual = new File(outputDirectory, "org/eclipse/che/ide/IDE.gwt.xml");

    assertEquals(
        "IDE.gwt.xml is changed but it shouldn't.",
        StringUtils.getNestedString(FileUtils.fileRead(expected), "<module", "</module>"),
        StringUtils.getNestedString(FileUtils.fileRead(actual), "<module", "</module>"));
  }
}
