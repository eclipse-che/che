/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.dynamodule;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import com.google.inject.Module;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.eclipse.che.inject.ModuleFinder;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test the generation of the class and the serviceloader SPI.
 *
 * @author Florent Benoit
 */
@FixMethodOrder(NAME_ASCENDING)
public class DynaModuleListGeneratorMojoTest {

  /** Rule to manage the mojo (inject, get variables from mojo) */
  @Rule public MojoRule rule = new MojoRule();

  /** Resources of each test mapped on the name of the method */
  @Rule public TestResources resources = new TestResources();

  /**
   * Helper method used to inject data in mojo
   *
   * @param mojo the mojo
   * @param baseDir root dir on which we extract files
   * @throws IllegalAccessException if unable to set variables
   */
  protected void configure(DynaModuleListGeneratorMojo mojo, File baseDir) throws Exception {
    this.rule.setVariableValueToObject(
        mojo, "targetDirectory", this.resources.getBasedir("project"));
    this.rule.setVariableValueToObject(mojo, "useClassPath", true);
  }

  /** Check that the ModuleList class is generated and contains the expected modules. */
  @Test
  public void testModuleListGenerated() throws Exception {

    File projectCopy = this.resources.getBasedir("project");
    File pom = new File(projectCopy, "pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());

    DynaModuleListGeneratorMojo mojo =
        (DynaModuleListGeneratorMojo) this.rule.lookupMojo("build", pom);
    configure(mojo, projectCopy);
    mojo.execute();

    File generatedModuleFile = mojo.getGuiceGeneratedModuleFile();
    // Check file has been generated
    assertTrue(generatedModuleFile.exists());

    Class<ModuleFinder> moduleFinderClass =
        (Class<ModuleFinder>)
            new CustomClassLoader()
                .defineClass(
                    "org.eclipse.che.dynamodule.MyDynamoduleTestModule",
                    Files.readAllBytes(generatedModuleFile.toPath()));

    ModuleFinder moduleFinder = moduleFinderClass.getDeclaredConstructor().newInstance();
    List<Module> moduleList = moduleFinder.getModules();
    org.testng.Assert.assertEquals(moduleList.size(), 2);

    assertTrue(moduleList.stream().anyMatch(item -> item.getClass().equals(MyCustomModule.class)));
    assertTrue(
        moduleList.stream().anyMatch(item -> item.getClass().equals(AnotherCustomModule.class)));
  }

  /** Check that the ServiceLoader is generated and working */
  @Test
  public void testServiceLoaderGenerated() throws Exception {

    File projectCopy = this.resources.getBasedir("project");
    File pom = new File(projectCopy, "pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());

    DynaModuleListGeneratorMojo mojo =
        (DynaModuleListGeneratorMojo) this.rule.lookupMojo("build", pom);
    configure(mojo, projectCopy);
    mojo.execute();

    URL url = new File(projectCopy + File.separator + "classes").toURI().toURL();

    URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {url});

    ServiceLoader<ModuleFinder> moduleFinderServiceLoader =
        ServiceLoader.load(ModuleFinder.class, urlClassLoader);
    Iterator<ModuleFinder> iterator = moduleFinderServiceLoader.iterator();
    assertTrue(iterator.hasNext());
    ModuleFinder moduleFinder = iterator.next();
    List<Module> moduleList = moduleFinder.getModules();
    org.testng.Assert.assertEquals(moduleList.size(), 2);

    assertTrue(moduleList.stream().anyMatch(item -> item.getClass().equals(MyCustomModule.class)));
    assertTrue(
        moduleList.stream().anyMatch(item -> item.getClass().equals(AnotherCustomModule.class)));
    assertFalse(iterator.hasNext());
  }

  /** Check that the plugin is able to scan war files */
  @Test
  public void testWarFiles() throws Exception {

    File projectCopy = this.resources.getBasedir("project");
    File pom = new File(projectCopy, "pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());

    DynaModuleListGeneratorMojo mojo =
        (DynaModuleListGeneratorMojo) this.rule.lookupMojo("build", pom);
    configure(mojo, projectCopy);
    this.rule.setVariableValueToObject(mojo, "scanWarDependencies", true);
    this.rule.setVariableValueToObject(mojo, "scanJarInWarDependencies", true);
    mojo.execute();
    Set<String> findClasses =
        mojo.getDynaModuleListGenerator().getDynaModuleScanner().getDynaModuleClasses();
    assertTrue(
        findClasses
                .stream()
                .filter(className -> className.contains("org.eclipse.che.wsagent"))
                .collect(toList())
                .size()
            > 2);

    // this dependency is inside the wsagent-core file.
    assertTrue(
        findClasses
                .stream()
                .filter(className -> className.contains("org.eclipse.che.wsagent.server."))
                .collect(toList())
                .size()
            >= 2);
  }

  private static class CustomClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }
}
