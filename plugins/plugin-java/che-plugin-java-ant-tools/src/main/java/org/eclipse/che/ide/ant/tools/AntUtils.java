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
package org.eclipse.che.ide.ant.tools;

import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.vfs.impl.fs.VirtualFileImpl;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.helper.ProjectHelper2;

import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/** @author andrew00x */
// TODO: avoid usage org.apache.tools.ant.Project for representation Ant project.
public class AntUtils {
    private static final Path   BUILD_FILE_PATH    = Paths.get(System.getProperty("java.io.tmpdir"), "codenvy_ant_properties.xml");
    private static final String BUILD_FILE_CONTENT = "<project name=\"ant_properties\" default=\"get_properties\">\n" +
                                                     "    <target name=\"get_properties\">\n" +
                                                     "        <echo>Ant version: ${ant.version}</echo>\n" +
                                                     "        <echo>Ant home: ${ant.home}</echo>\n" +
                                                     "        <echo>Java version: ${java.version}, vendor: ${java.vendor}</echo>\n" +
                                                     "        <echo>Java home: ${java.home}</echo>\n" +
                                                     "        <echo>OS name: \"${os.name}\", version: \"${os.version}\", " +
                                                     "arch: \"${os.arch}\"</echo>\n" +
                                                     "    </target>\n" +
                                                     "</project>\n";

    /** Not instantiable. */
    private AntUtils() {
    }

    public static String getAntExecCommand() {
        final java.io.File antHome = getAntHome();
        if (antHome != null) {
            final String ant = "bin" + java.io.File.separatorChar + "ant";
            return new java.io.File(antHome, ant).getAbsolutePath(); // If ant home directory set use it
        } else {
            return "ant"; // otherwise 'ant' should be in PATH variable
        }
    }

    public static java.io.File getAntHome() {
        final String antHomeEnv = System.getenv("ANT_HOME");
        if (antHomeEnv == null) {
            return null;
        }
        java.io.File antHome = new java.io.File(antHomeEnv);
        return antHome.exists() ? antHome : null;
    }

    private static java.io.File getJavaHome() {
        final String javaHomeEnv = System.getenv("JAVA_HOME");
        if (javaHomeEnv == null) {
            return null;
        }
        java.io.File javaHome = new java.io.File(javaHomeEnv);
        return javaHome.exists() ? javaHome : null;
    }

    private static java.io.File getJavaHome2() {
        String javaHomeSys = System.getProperty("java.home");
        if (javaHomeSys == null) {
            return null;
        }
        java.io.File javaHome = new java.io.File(javaHomeSys);
        if (!javaHome.exists()) {
            return null;
        }
        final String toolsJar = "lib" + java.io.File.separatorChar + "tools.jar";
        if (new java.io.File(javaHome, toolsJar).exists()) {
            return javaHome;
        }
        if (javaHomeSys.endsWith("jre")) {
            javaHomeSys = javaHomeSys.substring(0, javaHomeSys.length() - 4); // remove "/jre"
        }
        javaHome = new java.io.File(javaHomeSys);
        if (!javaHome.exists()) {
            return null;
        }
        if (new java.io.File(javaHome, toolsJar).exists()) {
            return javaHome;
        }
        return null;
    }

    /**
     * Creates FileFilter that helps filter system.
     * Ant may add two tools.jar in classpath. It uses two JavaHome locations. One from java system property and one from OS environment
     * variable. Ant sources: org.apache.tools.ant.launch.Locator.getToolsJar.
     */
    public static FileFilter newSystemFileFilter() {
        final java.io.File antHome = AntUtils.getAntHome();
        final java.io.File javaHome = getJavaHome();
        final java.io.File javaHome2 = getJavaHome2();
        final Path antHomePath = antHome == null ? null : antHome.toPath();
        final Path javaHomePath = javaHome == null ? null : javaHome.toPath();
        final Path javaHomePath2 = javaHome2 == null ? null : javaHome2.toPath();
        return new FileFilter() {
            @Override
            public boolean accept(java.io.File file) {
                final Path path = file.toPath();
                // Skip ant and system jars
                return !(javaHomePath != null && path.startsWith(javaHomePath)
                         || javaHomePath2 != null && path.startsWith(javaHomePath2)
                         || antHomePath != null && path.startsWith(antHomePath));
            }
        };
    }

    public static Map<String, String> getAntEnvironmentInformation() throws IOException {
        final Map<String, String> versionInfo = new HashMap<>();
        final LineConsumer cmdOutput = new LineConsumer() {
            boolean end = false;

            @Override
            public void writeLine(String line) throws IOException {
                if (line.isEmpty()) {
                    end = true;
                }
                if (end) {
                    return;
                }
                String key = null;
                int keyEnd = 0;
                int valueStart = 0;
                final int l = line.length();
                while (keyEnd < l) {
                    if (line.charAt(keyEnd) == ':') {
                        valueStart = keyEnd + 1;
                        break;
                    }
                    keyEnd++;
                }
                if (keyEnd > 0) {
                    key = line.substring(0, keyEnd);
                }
                if (key != null) {
                    while (valueStart < l && Character.isWhitespace(line.charAt(valueStart))) {
                        valueStart++;
                    }
                    if ("Ant version".equals(key)) {
                        int valueEnd = line.indexOf("compiled on", valueStart);
                        final String value = line.substring(valueStart, valueEnd).trim();
                        versionInfo.put(key, value);
                    } else {
                        final String value = line.substring(valueStart);
                        versionInfo.put(key, value);
                    }
                }
            }

            @Override
            public void close() throws IOException {
            }
        };
        readAntEnvironmentInformation(cmdOutput);
        return versionInfo;
    }

    private static void readAntEnvironmentInformation(LineConsumer cmdOutput) throws IOException {
        if (!Files.isReadable(BUILD_FILE_PATH)) {
            try (Writer writer = Files.newBufferedWriter(BUILD_FILE_PATH, Charset.forName("UTF-8"))) {
                writer.write(BUILD_FILE_CONTENT);
            }
        }
        final CommandLine commandLine = new CommandLine(getAntExecCommand()).add("-f", BUILD_FILE_PATH.toString(), "-quiet", "-emacs");
        final ProcessBuilder processBuilder = new ProcessBuilder().command(commandLine.toShellCommand()).redirectErrorStream(true);
        final Process process = processBuilder.start();
        ProcessUtil.process(process, cmdOutput, LineConsumer.DEV_NULL);
    }

    /** Get source directories. */
    public static List<String> getSourceDirectories(java.io.File buildFile) throws IOException {
        return getSourceDirectories(readProject(buildFile));
    }

    /** Get source directories. */
    public static List<String> getSourceDirectories(VirtualFile buildFile) throws IOException {
        //TODO: try fix problem with some build.xml that don't have basedir prop
        return getSourceDirectories(readProject(buildFile));
    }

    /**
     * Read description of ant project.
     *
     * @param buildFile
     *         path to build.xml file
     * @return description of ant project
     */
    public static Project readProject(java.io.File buildFile) throws IOException {
        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();
        antProject.setBasedir(buildFile.getParentFile().getAbsolutePath()); //TODO: try fix problem with some build.xml that don't have basedir prop
        try {
            ProjectHelper2.configureProject(antProject, buildFile);
        } catch (Exception e) {
//            throw new IOException("Error parsing ant file. " + e.getMessage());
            return antProject; //TODO: return empty project. Skip all parsing error. Lets importing project if not parse build.xml. In this case will used default props.
        }
        return antProject;
    }

    public static Project readProject(VirtualFile buildFile) throws IOException {
        return  readProject(((VirtualFileImpl)buildFile).getIoFile());
    }


    /** Get source directories. */
    public static List<String> getSourceDirectories(Project project) {
        Hashtable<String, Object> properties = project.getProperties();
        String absProjectPath = project.getBaseDir().getAbsolutePath();
        List<String> paths = new ArrayList<>(2);

        if (properties.containsKey("src.dir")) {
            String srcPath = (String)properties.get("src.dir");
            srcPath = srcPath.substring(absProjectPath.length());

            if (srcPath.startsWith("/")) {
                srcPath = srcPath.substring(1);
            }
            paths.add(srcPath);
        }
        if (properties.containsKey("test.dir")) {
            String testPath = (String)properties.get("test.dir");
            testPath = testPath.substring(absProjectPath.length());

            if (testPath.startsWith("/")) {
                testPath = testPath.substring(1);
            }
            paths.add(testPath);
        }
        if (paths.isEmpty()) {
            paths.add("src");
            paths.add("test");
        }
        return paths;
    }
}
