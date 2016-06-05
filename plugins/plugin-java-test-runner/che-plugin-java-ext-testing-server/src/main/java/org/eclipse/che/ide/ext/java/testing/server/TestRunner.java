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
package org.eclipse.che.ide.ext.java.testing.server;

import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class TestRunner {

    private final String projectPath;
    private final String sourceClassDir = "target/classes";
    private final String testClassDir = "target/test-classes";
    private final String classPathFile = "target/classpath.txt";
    protected final List<URL> classUrls = new ArrayList<URL>();

    public TestRunner(String projectPath) throws Exception {
        this.projectPath = projectPath;
        this.compile();
        this.processClasspath();
        this.addProjectClassPath();
    }

    private void addProjectClassPath() throws MalformedURLException {
        classUrls.add(Paths.get(projectPath, "target", "classes").toUri().toURL());
        classUrls.add(Paths.get(projectPath, "target", "test-classes").toUri().toURL());
    }

    private void processClasspath() throws IOException {

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
    }


    private boolean compile() throws IOException, InterruptedException {

        final CommandLine commandLineClassPath = new CommandLine("mvn","clean", "dependency:build-classpath",
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

    public abstract TestResult run(String testClass) throws Exception;


}
