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
package org.eclipse.che.plugin.dynamodule;

import com.google.inject.Module;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.che.inject.ModuleFinder;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Integration test of {@Link DynaModuleListGeneratorMojo}
 * It generates the DynaModule list and load it.
 * @author Florent Benoit
 */
@Listeners(value = {MockitoTestNGListener.class})
public class DynaModuleListGeneratorMojoITest {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DynaModuleListGeneratorMojoITest.class);

    /**
     * DynaModule generated class name
     */
    private static final String GENERATED_GUICE_CLASSNAME = "MyDynamoduleTestModule";

    /**
     * DynaModule generated filename
     */
    private static final String GENERATED_GUICE_FILE = GENERATED_GUICE_CLASSNAME + ".class";

    /**
     * Target folder of maven.
     */
    private Path buildDirectory;

    /**
     * Init folders
     */
    @BeforeClass
    public void init() throws URISyntaxException, IOException, InterruptedException {

        // target folder
        String buildDirectoryProperty = System.getProperty("buildDirectory");
        if (buildDirectoryProperty != null) {
            buildDirectory = new File(buildDirectoryProperty).toPath();
        }

        LOG.debug("Using building directory {0}", buildDirectory);
    }

    /**
     * Starts tests by compiling  generated Java class from maven plugin
     * @throws IOException if unable to start process
     * @throws InterruptedException if unable to wait the end of the process
     */
    @Test
    public void compileGuiceListModule()
        throws IOException, InterruptedException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

        // search generated guice module list file
        Path p = this.buildDirectory;
        final int maxDepth = 10;
        Stream<Path> matches = java.nio.file.Files.find(p, maxDepth, (path, basicFileAttributes) -> (path.getFileName().toString().equals(GENERATED_GUICE_FILE) && path.toString().contains("testModuleListGenerated")));

        // take first
        Optional<Path> optionalPath = matches.findFirst();
        if (!optionalPath.isPresent()) {
            throw new IllegalStateException("Unable to find generated Guice file named '" + GENERATED_GUICE_FILE + "'. Check it has been generated first");
        }

        Path generatedJavaFilePath = optionalPath.get();

        String className = generatedJavaFilePath.getFileName().toString();
        className = className.substring(0, className.length() - ".class".length());

        Class<ModuleFinder> moduleFinderClass = (Class<ModuleFinder>) new CustomClassLoader().defineClass("org.eclipse.che.dynamodule." + className, Files
            .readAllBytes(generatedJavaFilePath));

        ModuleFinder moduleFinder=  moduleFinderClass.getDeclaredConstructor().newInstance();
        List<Module> moduleList = moduleFinder.getModules();
        Assert.assertEquals(moduleList.size(), 2);


        Assert.assertTrue(moduleList.stream().anyMatch(item -> item.getClass().equals(MyCustomModule.class)));
        Assert.assertTrue(moduleList.stream().anyMatch(item -> item.getClass().equals(AnotherCustomModule.class)));
    }


    /**
     * Custom loader to define the class
     */
    private static class CustomClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

}
