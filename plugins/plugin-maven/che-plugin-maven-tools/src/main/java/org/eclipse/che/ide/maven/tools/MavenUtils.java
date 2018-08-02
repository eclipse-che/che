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

import static java.nio.file.Files.exists;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.SystemInfo;

/**
 * A smattering of useful methods to work with the Maven POM.
 *
 * @author Artem Zatsarynnyi
 * @author andrew00x
 * @author Eugene Voevodin
 */
public class MavenUtils {
  public static final Pattern MAVEN_LOGGER_PREFIX_REMOVER =
      Pattern.compile("(\\[INFO\\]|\\[WARNING\\]|\\[DEBUG\\]|\\[ERROR\\])\\s+(.*)");

  /** Not instantiable. */
  private MavenUtils() {}

  public static String removeLoggerPrefix(String origin) {
    final Matcher matcher = MAVEN_LOGGER_PREFIX_REMOVER.matcher(origin);
    if (matcher.matches()) {
      return origin.substring(matcher.start(2));
    }
    return origin;
  }

  /**
   * Get description of maven project and all its modules if any as plain list.
   *
   * @param sources maven project directory. Note: Must contains pom.xml file.
   * @return description of maven project
   * @throws IOException if an i/o error occurs
   */
  public static List<Model> getModules(java.io.File sources) throws IOException {
    final LinkedList<Model> modules = new LinkedList<>();
    addModules(Model.readFrom(sources), modules);
    return modules;
  }

  private static void addModules(Model model, List<Model> modules) throws IOException {
    if (!"pom".equals(model.getPackaging())) return;

    for (String module : model.getModules()) {
      final Path modulePom =
          model.getProjectDirectory().toPath().resolve(module).resolve("pom.xml");
      if (exists(modulePom)) {
        final Model child = Model.readFrom(modulePom);
        final String relativePath =
            modulePom.getParent().relativize(model.getPomFile().toPath()).toString();
        child.setParent(
            new Parent(model.getGroupId(), model.getArtifactId(), model.getVersion())
                .setRelativePath(relativePath));
        modules.add(child);
        addModules(child, modules);
      }
    }
  }

  /**
   * Parses lines of maven output of command 'mvn dependency:list', e.g.
   * org.eclipse.che.platform-api:codenvy-api-factory:jar:0.26.0:compile. Maven dependency plugin
   * sources: org.apache.maven.plugin.dependency.utils.DependencyStatusSets.getOutput(boolean,
   * boolean, boolean)
   *
   * @param line raw line. Line may contain prefix '[INFO]'
   * @return parsed dependency model
   */
  public static MavenArtifact parseMavenArtifact(String line) {
    if (line != null) {
      final String[] segments = removeLoggerPrefix(line).split(":");
      if (segments.length >= 5) {
        final String groupId = segments[0];
        final String artifactId = segments[1];
        final String type = segments[2];
        final String classifier;
        final String version;
        final String scope;
        if (segments.length == 5) {
          version = segments[3];
          classifier = null;
          scope = segments[4];
        } else {
          version = segments[4];
          classifier = segments[3];
          scope = segments[5];
        }
        return new MavenArtifact(groupId, artifactId, type, classifier, version, scope);
      }
    }
    return null;
  }

  /**
   * Returns an execution command to launch Maven. If Maven home environment variable isn't set then
   * 'mvn' will be returned since it's assumed that 'mvn' should be in PATH variable.
   *
   * @return an execution command to launch Maven
   */
  public static String getMavenExecCommand() {
    final java.io.File mvnHome = getMavenHome();
    if (mvnHome != null) {
      final String mvn = "bin" + java.io.File.separatorChar + "mvn";
      return new java.io.File(mvnHome, mvn)
          .getAbsolutePath(); // use Maven home directory if it's set
    } else {
      return "mvn"; // otherwise 'mvn' should be in PATH variable
    }
  }

  /**
   * Returns Maven home directory.
   *
   * @return Maven home directory
   */
  public static java.io.File getMavenHome() {
    String m2HomeEnv = System.getenv("M2_HOME");
    if (m2HomeEnv == null) {
      return null;
    }
    if (SystemInfo.isWindows() && m2HomeEnv.contains(" ")) {
      m2HomeEnv = "\"" + m2HomeEnv + "\"";
    }
    final java.io.File m2Home = new java.io.File(m2HomeEnv);
    return m2Home.exists() ? m2Home : null;
  }

  /**
   * Get groupId of artifact. If artifact doesn't have groupId this method checks parent artifact
   * for groupId.
   */
  public static String getGroupId(Model model) {
    String groupId = model.getGroupId();
    if (groupId == null) {
      final Parent parent = model.getParent();
      if (parent != null) {
        groupId = parent.getGroupId();
      }
    }
    return groupId;
  }

  /**
   * Get version of artifact. If artifact doesn't have version this method checks parent artifact
   * for version.
   */
  public static String getVersion(Model model) {
    String version = model.getVersion();
    if (version == null) {
      final Parent parent = model.getParent();
      if (parent != null) {
        version = parent.getVersion();
      }
    }
    return version;
  }

  /** Get source directories. */
  public static List<String> getSourceDirectories(Model model) {
    List<String> list = new LinkedList<>();
    Build build = model.getBuild();
    if (build != null) {
      if (build.getSourceDirectory() != null) {
        list.add(build.getSourceDirectory());
      } else if (build.getTestSourceDirectory() != null) {
        list.add(build.getTestSourceDirectory());
      }
    }
    if (list.isEmpty()) {
      list.add("src/main/java");
      list.add("src/test/java");
    }
    return list;
  }

  /** Get source directories. */
  public static List<String> getSourceDirectories(java.io.File pom) throws IOException {
    return getSourceDirectories(Model.readFrom(pom));
  }

  /** Get resource directories. */
  public static List<String> getResourceDirectories(Model model) {
    List<String> list = new LinkedList<>();
    Build build = model.getBuild();

    if (build != null) {
      if (build.getResources() != null && !build.getResources().isEmpty()) {
        for (Resource resource : build.getResources()) list.add(resource.getDirectory());
      }
    }
    if (list.isEmpty()) {
      list.add("src/main/resources");
      list.add("src/test/resources");
    }
    return list;
  }

  /** Get resource directories. */
  public static List<String> getResourceDirectories(java.io.File pom) throws IOException {
    return getResourceDirectories(Model.readFrom(pom));
  }

  public static Map<String, String> getMavenVersionInformation() throws IOException {
    final Map<String, String> versionInfo = new HashMap<>();
    final LineConsumer cmdOutput =
        new LineConsumer() {
          @Override
          public void writeLine(String line) throws IOException {
            String key = null;
            int keyEnd = 0;
            int valueStart = 0;
            final int l = line.length();
            if (line.startsWith("Apache Maven")) {
              key = "Maven version";
            } else {
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
            }
            if (key != null) {
              while (valueStart < l && Character.isWhitespace(line.charAt(valueStart))) {
                valueStart++;
              }
              if ("Maven version".equals(key)) {
                int valueEnd = valueStart;
                // Don't show version details, e.g. (0728685237757ffbf44136acec0402957f723d9a;
                // 2013-09-17 18:22:22+0300)
                while (valueEnd < l && '(' != line.charAt(valueEnd)) {
                  valueEnd++;
                }
                final String value = line.substring(valueStart, valueEnd).trim();
                versionInfo.put(key, value);
              } else {
                final String value = line.substring(valueStart);
                versionInfo.put(key, value);
              }
            }
          }

          @Override
          public void close() throws IOException {}
        };
    readMavenVersionInformation(cmdOutput);
    return versionInfo;
  }

  private static void readMavenVersionInformation(LineConsumer cmdOutput) throws IOException {
    final CommandLine commandLine = new CommandLine(getMavenExecCommand()).add("-version");
    final ProcessBuilder processBuilder =
        new ProcessBuilder().command(commandLine.toShellCommand()).redirectErrorStream(true);
    final Process process = processBuilder.start();
    ProcessUtil.process(process, cmdOutput, LineConsumer.DEV_NULL);
  }
}
