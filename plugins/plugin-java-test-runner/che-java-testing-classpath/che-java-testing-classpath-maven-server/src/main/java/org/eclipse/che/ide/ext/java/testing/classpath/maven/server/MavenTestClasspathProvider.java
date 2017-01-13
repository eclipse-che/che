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
package org.eclipse.che.ide.ext.java.testing.classpath.maven.server;


import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Maven implementation for the test classpath provider.
 *
 * @author Mirage Abeysekara
 */
public class MavenTestClasspathProvider implements TestClasspathProvider {

    private boolean buildClasspath(String projectPath) throws IOException, InterruptedException {

        final CommandLine commandLineClassPath = new CommandLine("mvn", "clean", "dependency:build-classpath",
                "-Dmdep.outputFile=target/test.classpath.maven");
        Process processBuildClassPath = new ProcessBuilder()
                .redirectErrorStream(true)
                .directory(new File(projectPath))
                .command(commandLineClassPath.toShellCommand())
                .start();
        ProcessUtil.process(processBuildClassPath, LineConsumer.DEV_NULL, LineConsumer.DEV_NULL);
        processBuildClassPath.waitFor();

        final CommandLine commandLineTestCompile = new CommandLine("mvn", "test-compile");
        Process processTestCompile = new ProcessBuilder()
                .redirectErrorStream(true)
                .directory(new File(projectPath))
                .command(commandLineTestCompile.toShellCommand())
                .start();
        ProcessUtil.process(processTestCompile, LineConsumer.DEV_NULL, LineConsumer.DEV_NULL);
        return processTestCompile.waitFor() == 0;

    }

    private List<URL> getProjectClasspath(String projectPath) throws IOException {

        List<URL> classUrls = new ArrayList<>();
        File cpFile = Paths.get(projectPath, "target", "test.classpath.maven").toFile();

        FileReader fileReader = new FileReader(cpFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = bufferedReader.readLine();
        String[] paths = line.split(":");

        for (String path : paths) {
            classUrls.add(new File(path).toURI().toURL());
        }
        bufferedReader.close();
        fileReader.close();

        classUrls.add(Paths.get(projectPath, "target", "classes").toUri().toURL());
        classUrls.add(Paths.get(projectPath, "target", "test-classes").toUri().toURL());

        return classUrls;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader(String projectPath, boolean updateClasspath) throws Exception {
        List<URL> classUrls;
        try {
            if (updateClasspath) {
                buildClasspath(projectPath);
            }
            classUrls = getProjectClasspath(projectPath);
        } catch (IOException | InterruptedException e) {
            throw new Exception("Failed to build Maven classpath.", e);
        }
        return new URLClassLoader(classUrls.toArray(new URL[classUrls.size()]), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectType() {
        return "maven";
    }
}
