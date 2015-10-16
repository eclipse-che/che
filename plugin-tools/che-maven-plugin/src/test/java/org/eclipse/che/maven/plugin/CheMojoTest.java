/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.maven.plugin;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.eclipse.che.maven.plugin.stub.CheMojoProjectStubSingle;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Test of the Che plugin
 * @author Florent Benoit
 */
public class CheMojoTest {

    /**
     * Rule to manage the mojo (inject, get variables from mojo)
     */
    @Rule
    public MojoRule rule = new MojoRule();

    /**
     * Resources of each test mapped on the name of the method
     */
    @Rule
    public TestResources resources = new TestResources();


    /**
     * Helper method used to inject data in mojo
     * @param mojo the mojo
     * @param baseDir root dir on which we extract files
     * @throws IllegalAccessException if unable to set variables
     */
    protected void configure(CheMojo mojo, File baseDir) throws IllegalAccessException {
        this.rule.setVariableValueToObject(mojo, "srcJavaFolder",
                                           new File(baseDir, "src" + File.separator + "main" + File.separator + "java"));
        this.rule.setVariableValueToObject(mojo, "cheFolder", new File(baseDir, "src" + File.separator + "main" + File.separator + "che"));
        this.rule.setVariableValueToObject(mojo, "ymlFile", new File(baseDir, "che-plugin.yml"));
        this.rule.setVariableValueToObject(mojo, "destFile", new File(baseDir, "assembly-test.zip"));
    }


    /**
     * Test mojo with assembly mode (no java source folder)
     */
    @Test
    public void testAssembly1() throws Exception {

        File projectCopy = this.resources.getBasedir("assembly1");
        File pom = new File(projectCopy, "pom.xml");
        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        CheMojo mojo = (CheMojo)this.rule.lookupMojo("build", pom);
        configure(mojo, projectCopy);

        Assert.assertNotNull(mojo);
        mojo.execute();

        boolean hasMachines = false;
        boolean hasTemplates = false;
        boolean hasExtensions = false;
        boolean hasPluginDescriptor = false;

        // Check entries of the zip file
        File destFile = (File)rule.getVariableValueFromObject(mojo, "destFile");
        try (ZipFile zipFile = new ZipFile(destFile)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                if ("machines/machine1/Dockerfile".equals(entryName)) {
                    hasMachines = true;
                }
                if ("templates/template1.json".equals(entryName)) {
                    hasTemplates = true;
                }
                if ("extensions".equals(entryName)) {
                    hasExtensions = true;
                }
                if ("che-plugin.yml".equals(entryName)) {
                    hasPluginDescriptor = true;
                }

            }
        }

        Assert.assertTrue(hasPluginDescriptor);
        Assert.assertTrue(hasMachines);
        Assert.assertTrue(hasTemplates);
        Assert.assertFalse(hasExtensions);
    }


    /**
     * Test mojo with assembly mode (no java source folder) with some dependencies
     */
    @Test
    public void testAssembly2() throws Exception {

        File projectCopy = this.resources.getBasedir("assembly2");
        File pom = new File(projectCopy, "pom.xml");
        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        CheMojo mojo = (CheMojo)this.rule.lookupMojo("build", pom);
        configure(mojo, projectCopy);

        Assert.assertNotNull(mojo);
        mojo.execute();

        boolean hasMachines = false;
        boolean hasTemplates = false;
        boolean hasExtensions = false;
        boolean hasPluginDescriptor = false;

        // Check entries of the zip file
        File destFile = (File)rule.getVariableValueFromObject(mojo, "destFile");
        try (ZipFile zipFile = new ZipFile(destFile)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                if ("machines/machine1/Dockerfile".equals(entryName)) {
                    hasMachines = true;
                }
                if ("templates/template1.json".equals(entryName)) {
                    hasTemplates = true;
                }
                if ("extensions/junit.jar".equals(entryName)) {
                    hasExtensions = true;
                }
                if ("che-plugin.yml".equals(entryName)) {
                    hasPluginDescriptor = true;
                }

            }
        }

        Assert.assertTrue(hasPluginDescriptor);
        Assert.assertFalse(hasMachines);
        Assert.assertFalse(hasTemplates);
        Assert.assertTrue(hasExtensions);


        Assert.assertNotNull(mojo);
        mojo.execute();
    }


    /**
     * Check java mode where user defined both extension code and templates + machines
     * @throws Exception
     */
    @Test
    public void testSingle() throws Exception {

        File projectCopy = this.resources.getBasedir("single");
        File pom = new File(projectCopy, "pom.xml");
        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        CheMojoProjectStubSingle project = new CheMojoProjectStubSingle();

        String extensionName = project.getArtifactId() + "-" +
                               project.getVersion() + ".jar";

        CheMojo mojo = (CheMojo)this.rule.lookupMojo("build", pom);
        configure(mojo, projectCopy);
        this.rule.setVariableValueToObject(mojo, "compiledJarFile",
                                           new File(project.getOriginalBasedir(),
                                                    File.separator + "target" + File.separator +
                                                    "test-compilation" + File.separator + extensionName
                                           ));


        Assert.assertNotNull(mojo);
        mojo.execute();

        boolean hasMachines = false;
        boolean hasTemplates = false;
        boolean hasExtensions = false;
        boolean hasPluginDescriptor = false;

        // Check entries of the zip file
        File destFile = (File)rule.getVariableValueFromObject(mojo, "destFile");
        try (ZipFile zipFile = new ZipFile(destFile)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                if ("machines/machine1/Dockerfile".equals(entryName)) {
                    hasMachines = true;
                }
                if ("templates/template1.json".equals(entryName)) {
                    hasTemplates = true;
                }
                if (("extensions/" + extensionName).equals(entryName)) {
                    hasExtensions = true;
                }
                if ("che-plugin.yml".equals(entryName)) {
                    hasPluginDescriptor = true;
                }

            }
        }

        Assert.assertTrue(hasPluginDescriptor);
        Assert.assertTrue(hasMachines);
        Assert.assertTrue(hasTemplates);
        Assert.assertTrue(hasExtensions);
    }


}
