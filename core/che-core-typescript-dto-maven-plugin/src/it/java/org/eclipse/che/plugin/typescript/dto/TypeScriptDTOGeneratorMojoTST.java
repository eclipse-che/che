/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.typescript.dto;

import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.core.util.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Integration test of TypeScriptDTOGeneratorMojo
 * It uses docker to launch TypeScript compiler and then launch JavaScript tests to ensure generator has worked correctly
 * @author Florent Benoit
 */
public class TypeScriptDTOGeneratorMojoTST {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TypeScriptDTOGeneratorMojoTST.class);

    /**
     * DTO Generated file
     */
    private static final String GENERATED_DTO_NAME = "my-typescript-test-module.ts";

    private static final String GENERATED_DTO_DTS_NAME = "my-typescript-test-module.d.ts";

    /**
     * DTO new name
     */
    private static final String DTO_FILENAME = "dto.ts";

    private static final String DTO_DTS_FILENAME = "dtoD.d.ts";

    /**
     * DTO test name
     */
    private static final String DTO_SPEC_FILENAME = "dto.spec.ts";

    /**
     * Target folder of maven.
     */
    private Path buildDirectory;

    /**
     * Path to the package.json file used to setup typescript compiler
     */
    private Path dtoSpecJsonPath;

    /**
     * Path to the package.json file used to setup typescript compiler
     */
    private Path packageJsonPath;

    /**
     * Root directory for our tests
     */
    private Path rootPath;

    /**
     * Linux uid.
     */
    private String linuxUID;

    /**
     * Linux gid.
     */
    private String linuxGID;

    /**
     * Init folders
     */
    @BeforeClass
    public void init() throws URISyntaxException, IOException, InterruptedException {
        // setup packages
        this.packageJsonPath = new File(TypeScriptDTOGeneratorMojoTST.class.getClassLoader().getResource("package.json").toURI()).toPath();

        this.rootPath = this.packageJsonPath.getParent();

        // target folder
        String buildDirectoryProperty = System.getProperty("buildDirectory");
        if (buildDirectoryProperty != null) {
            buildDirectory = new File(buildDirectoryProperty).toPath();
        } else {
            buildDirectory = packageJsonPath.getParent().getParent();
        }

        LOG.info("Using building directory {0}", buildDirectory);
    }

    /**
     * Generates a docker exec command used to launch node commands. Uses podman if present on the
     * system.
     *
     * @return list of command parameters
     */
    protected List<String> getDockerExec() throws IOException, InterruptedException {
        // setup command line
        List<String> command = new ArrayList<>();
        if (hasPodman()) {
            command.add("podman");
        } else {
            command.add("docker");
        }
        command.add("run");
        command.add("--rm");
        command.add("-v");
        command.add(rootPath.toString() + ":/usr/src/app");
        command.add("-w");
        command.add("/usr/src/app");
        command.add("node:6");
        command.add("/bin/sh");
        command.add("-c");

        return command;
    }

    /**
     * Get UID of current user (used on Linux)
     */
    protected String getUid() throws IOException, InterruptedException {
        if (this.linuxUID == null) {
            // grab user id
            ProcessBuilder uidProcessBuilder = new ProcessBuilder("id", "-u");
            Process processId = uidProcessBuilder.start();
            int resultId = processId.waitFor();
            String uid = "";
            try (BufferedReader outReader = new BufferedReader(new InputStreamReader(processId.getInputStream()))) {
                uid = String.join(System.lineSeparator(), outReader.lines().collect(toList()));
            } catch (Exception error) {
                throw new IllegalStateException("Unable to get uid" + uid);
            }

            if (resultId != 0) {
                throw new IllegalStateException("Unable to get uid" + uid);
            }

            try {
                Integer.valueOf(uid);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("The uid is not a number" + uid);
            }
            this.linuxUID = uid;
        }

        return this.linuxUID;
    }

    /**
     * Get GID of current user (used on Linux)
     */
    protected String getGid() throws IOException, InterruptedException {
        if (this.linuxGID == null) {

            ProcessBuilder gidProcessBuilder = new ProcessBuilder("id", "-g");
            Process processGid = gidProcessBuilder.start();
            int resultGid = processGid.waitFor();
            String gid = "";
            try (BufferedReader outReader = new BufferedReader(new InputStreamReader(processGid.getInputStream()))) {
                gid = String.join(System.lineSeparator(), outReader.lines().collect(toList()));
            } catch (Exception error) {
                throw new IllegalStateException("Unable to get gid" + gid);
            }

            if (resultGid != 0) {
                throw new IllegalStateException("Unable to get gid" + gid);
            }

            try {
                Integer.valueOf(gid);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("The uid is not a number" + gid);
            }

            this.linuxGID = gid;
        }

        return this.linuxGID;
    }

    /**
     * Setup typescript compiler by downloading the dependencies
     * @throws IOException if unable to start process
     * @throws InterruptedException if unable to wait the end of the process
     */
    @Test(groups = {"tools"})
    protected void installTypeScriptCompiler() throws IOException, InterruptedException {

        // setup command line
        List<String> command = getDockerExec();

        // avoid root permissions in generated files
        if (SystemInfo.isLinux()) {
            command.add(wrapLinuxCommand("npm install"));
        } else {
            command.add("npm install");
        }

        // setup typescript compiler
        ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(rootPath.toFile()).redirectErrorStream(true).inheritIO();
        Process process = processBuilder.start();

        LOG.info("Installing TypeScript compiler in {0}", rootPath);
        int resultProcess = process.waitFor();

        if (resultProcess != 0) {
            throw new IllegalStateException("Install of TypeScript has failed");
        }
        LOG.info("TypeScript compiler installed.");

    }


    /**
     * Wrap the given command into a command with chown. Also add group/user that match host environment if not exists
     * @param command the command to wrap
     * @return an updated command with chown applied on it
     */
    protected String wrapLinuxCommand(String command) throws IOException, InterruptedException {
        if (hasPodman()) {
            LOG.debug("using podman, don't need to wrap anything");
            return command;
        }
        String setGroup =
            "export GROUP_NAME=`(getent group " + getGid() + " || (groupadd -g " + getGid()
                + " user && echo user:x:" + getGid() + ")) | cut -d: -f1`";
        String setUser =
            "export USER_NAME=`(getent passwd " + getUid() + " || (useradd -u " + getUid()
                + " -g ${GROUP_NAME} user && echo user:x:" + getGid() + ")) | cut -d: -f1`";
        String chownCommand = "chown --silent -R ${USER_NAME}.${GROUP_NAME} /usr/src/app || true";
        return setGroup + " && " + setUser + " && " + chownCommand + " && " + command + " && "
            + chownCommand;
    }


    /**
     * Starts tests by compiling first generated DTO from maven plugin
     * @throws IOException if unable to start process
     * @throws InterruptedException if unable to wait the end of the process
     */
    @Test(dependsOnGroups = "tools")
    public void compileDTOAndLaunchTests() throws IOException, InterruptedException {

        // search DTO
        Path p = this.buildDirectory;
        final int maxDepth = 10;
        Stream<Path> matches = java.nio.file.Files.find( p, maxDepth, (path, basicFileAttributes) -> path.getFileName().toString().equals(GENERATED_DTO_NAME));

        // take first
        Optional<Path> optionalPath = matches.findFirst();
        if (!optionalPath.isPresent()) {
            throw new IllegalStateException("Unable to find generated DTO file named '" + GENERATED_DTO_NAME + "'. Check it has been generated first");
        }

        Path generatedDtoPath = optionalPath.get();

        //copy it in test resources folder where package.json is
        java.nio.file.Files.copy(generatedDtoPath, this.rootPath.resolve(DTO_FILENAME), StandardCopyOption.REPLACE_EXISTING);

        matches = java.nio.file.Files.find( p, maxDepth, (path, basicFileAttributes) -> path.getFileName().toString().equals(GENERATED_DTO_DTS_NAME));

        // take first
        optionalPath = matches.findFirst();
        if (!optionalPath.isPresent()) {
            throw new IllegalStateException("Unable to find generated DTO file named '" + GENERATED_DTO_DTS_NAME + "'. Check it has been generated first");
        }

        generatedDtoPath = optionalPath.get();

        //copy it in test resources folder where package.json is
        java.nio.file.Files.copy(generatedDtoPath, this.rootPath.resolve(DTO_DTS_FILENAME), StandardCopyOption.REPLACE_EXISTING);

        // setup command line
        List<String> command = getDockerExec();

        // avoid root permissions in generated files
        if (SystemInfo.isLinux()) {
            command.add(wrapLinuxCommand("npm test"));
        } else {
            command.add("npm test");
        }
        // setup typescript compiler
        ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(rootPath.toFile()).redirectErrorStream(true).inheritIO();
        Process process = processBuilder.start();

        LOG.info("Starting TypeScript tests...");
        int resultProcess = process.waitFor();

        if (resultProcess != 0) {
            throw new IllegalStateException("DTO has failed to compile");
        }
        LOG.info("TypeScript tests OK");

    }

    private boolean hasPodman() throws InterruptedException, IOException {
        if (SystemInfo.isLinux()) {
            ProcessBuilder podmanProcessBuilder = new ProcessBuilder("which", "podman");
            Process podmanProcess = podmanProcessBuilder.start();
            if (podmanProcess.waitFor(1, TimeUnit.SECONDS)) {
                return podmanProcess.exitValue() == 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
