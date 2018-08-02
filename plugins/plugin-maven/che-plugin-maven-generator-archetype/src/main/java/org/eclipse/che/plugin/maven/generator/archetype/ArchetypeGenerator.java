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
package org.eclipse.che.plugin.maven.generator.archetype;

import static java.io.File.separator;
import static org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput.State.DONE;
import static org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput.State.ERROR;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.core.util.Watchdog;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.ide.maven.tools.MavenArtifact;
import org.eclipse.che.ide.maven.tools.MavenUtils;
import org.eclipse.che.plugin.maven.shared.MavenArchetype;
import org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates projects with maven-archetype-plugin.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArchetypeGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(ArchetypeGenerator.class);

  private EventService eventService;
  private final FsManager fsManager;

  @Inject
  public ArchetypeGenerator(EventService eventService, FsManager fsManager) {
    this.eventService = eventService;
    this.fsManager = fsManager;
  }

  /**
   * Generates a new project from the specified archetype by given maven artifact descriptor.
   *
   * @param projectName name of the project
   * @param workDir folder where command will execute in common use root dir of workspace
   * @param archetype archetype from which need to generate new project
   * @param mavenArtifact maven artifact descriptor
   * @throws ServerException if an error occurs while generating project
   */
  public void generateFromArchetype(
      String projectName, File workDir, MavenArchetype archetype, MavenArtifact mavenArtifact)
      throws ServerException {
    Map<String, String> archetypeProperties = new HashMap<>();
    archetypeProperties.put(
        "-DinteractiveMode", "false"); // get rid of the interactivity of the archetype plugin
    archetypeProperties.put("-DarchetypeGroupId", archetype.getGroupId());
    archetypeProperties.put("-DarchetypeArtifactId", archetype.getArtifactId());
    archetypeProperties.put("-DarchetypeVersion", archetype.getVersion());
    archetypeProperties.put("-DgroupId", mavenArtifact.getGroupId());
    archetypeProperties.put("-DartifactId", mavenArtifact.getArtifactId());
    archetypeProperties.put("-Dversion", mavenArtifact.getVersion());
    archetypeProperties.put("-Dbasedir", workDir.toPath().resolve(projectName).toString());
    if (archetype.getRepository() != null) {
      archetypeProperties.put("-DarchetypeRepository", archetype.getRepository());
    }
    if (archetype.getProperties() != null) {
      archetypeProperties.putAll(archetype.getProperties());
    }
    final CommandLine commandLine = createCommandLine(archetypeProperties);
    try {
      execute(commandLine.toShellCommand(), workDir);
      // TODO Remove this block and use 'basedir' option of the Maven Archetype Plugin when the
      // related issue will be solved: https://issues.apache.org/jira/browse/ARCHETYPE-311.
      // Maven Archetype Plugin creates project directory with 'artifact-id' name, so need to rename
      // it to specified project name.
      if (!fsManager.exists(projectName) && fsManager.exists(mavenArtifact.getArtifactId())) {
        fsManager.move(separator + mavenArtifact.getArtifactId(), separator + projectName);
      }
    } catch (TimeoutException
        | IOException
        | InterruptedException
        | ConflictException
        | NotFoundException e) {
      LOG.error(e.getMessage());
    }
  }

  /**
   * Execute maven archetype command
   *
   * @param commandLine command to execution e.g. mvn archetype:generate
   *     -DarchetypeGroupId=<archetype-groupId> -DarchetypeArtifactId=<archetype-artifactId>
   *     -DarchetypeVersion=<archetype-version> -DgroupId=<my.groupid> -DartifactId=<my-artifactId>
   * @param workDir folder where command will execute in common use root dir of workspace
   * @throws TimeoutException
   * @throws IOException
   * @throws InterruptedException
   */
  private void execute(String[] commandLine, File workDir)
      throws TimeoutException, IOException, InterruptedException {
    ProcessBuilder pb =
        new ProcessBuilder(commandLine).redirectErrorStream(true).directory(workDir);

    eventService.publish(
        new ArchetypeOutputImpl("Start Project generation", ArchetypeOutput.State.START));

    LineConsumer lineConsumer =
        new AbstractLineConsumer() {
          @Override
          public void writeLine(String line) throws IOException {
            eventService.publish(new ArchetypeOutputImpl(line, ArchetypeOutput.State.IN_PROGRESS));
          }
        };

    // process will be stopped after timeout
    Watchdog watcher = new Watchdog(60, TimeUnit.SECONDS);

    try {
      final Process process = pb.start();
      final ValueHolder<Boolean> isTimeoutExceeded = new ValueHolder<>(false);
      watcher.start(
          () -> {
            isTimeoutExceeded.set(true);
            ProcessUtil.kill(process);
          });
      // consume logs until process ends
      ProcessUtil.process(process, lineConsumer);
      process.waitFor();
      eventService.publish(new ArchetypeOutputImpl("Done", DONE));
      if (isTimeoutExceeded.get()) {
        LOG.error("Generation project time expired : command-line " + Arrays.toString(commandLine));
        eventService.publish(new ArchetypeOutputImpl("Generation project time expired", ERROR));
        throw new TimeoutException();
      } else if (process.exitValue() != 0) {
        LOG.error("Generation project fail : command-line " + Arrays.toString(commandLine));
        eventService.publish(new ArchetypeOutputImpl("Generation project occurs error", ERROR));
        throw new IOException(
            "Process failed. Exit code "
                + process.exitValue()
                + " command-line : "
                + Arrays.toString(commandLine));
      }
    } finally {
      watcher.stop();
    }
  }

  /**
   * Create specified command for maven archetype plugin e.g mvn archetype:generate
   * -DgroupId=com.mycompany.app -DartifactId=my-app
   * -DarchetypeArtifactId=maven-archetype-quickstart
   *
   * @param archetypeProperties
   * @return command line ready to execute
   */
  private CommandLine createCommandLine(Map<String, String> archetypeProperties) {
    final CommandLine commandLine = new CommandLine(MavenUtils.getMavenExecCommand());
    commandLine.add("--batch-mode");
    commandLine.add("org.apache.maven.plugins:maven-archetype-plugin:RELEASE:generate");
    commandLine.add(archetypeProperties);
    return commandLine;
  }
}
