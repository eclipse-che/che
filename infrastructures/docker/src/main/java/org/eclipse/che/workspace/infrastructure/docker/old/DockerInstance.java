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
package org.eclipse.che.workspace.infrastructure.docker.old;

/**
 * Docker implementation of {@link }
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 * @author Anton Korneta
 * @author Mykola Morhun
 */
public class DockerInstance {
    /*public InstanceProcess getProcess(final int pid) throws NotFoundException, MachineException {
        final InstanceProcess machineProcess = machineProcesses.get(pid);
        if (machineProcess != null) {
            try {
                machineProcess.checkAlive();
                return machineProcess;
            } catch (NotFoundException e) {
                machineProcesses.remove(pid);
                throw e;
            }
        }
        throw new NotFoundException(format("Process with pid %s not found", pid));
    }

    public List<InstanceProcess> getProcesses() throws MachineException {
        List<InstanceProcess> processes = new LinkedList<>();
        try {
            final Exec exec = docker.createExec(CreateExecParams.create(container,
                                                                        new String[] {"/bin/sh",
                                                                                      "-c",
                                                                                      GET_ALIVE_PROCESSES_COMMAND})
                                                                .withDetach(false));
            docker.startExec(StartExecParams.create(exec.getId()), logMessage -> {
                final String pidFilePath = logMessage.getContent().trim();
                final Matcher matcher = PID_FILE_PATH_PATTERN.matcher(pidFilePath);
                if (matcher.matches()) {
                    final int virtualPid = Integer.parseInt(matcher.group(1));
                    final InstanceProcess dockerProcess = machineProcesses.get(virtualPid);
                    if (dockerProcess != null) {
                        processes.add(dockerProcess);
                    } else {
                        LOG.warn("Machine process {} exists in container but missing in processes map", virtualPid);
                    }
                }
            });
            return processes;
        } catch (IOException e) {
            throw new MachineException(e);
        }
    }

    @VisibleForTesting
    protected void commitContainer(String repository, String tag) throws IOException {
        String comment = format("Suspended at %1$ta %1$tb %1$td %1$tT %1$tZ %1$tY",
                                System.currentTimeMillis());
        // !! We SHOULD NOT pause container before commit because all execs will fail
        // to push image to private registry it should be tagged with registry in repo name
        // https://docs.docker.com/reference/api/docker_remote_api_v1.16/#push-an-image-on-the-registry
        docker.commit(CommitParams.create(container)
                                  .withRepository(repository)
                                  .withTag(tag)
                                  .withComment(comment));
    }

    private String generateRepository() {
        if (registryNamespace != null) {
            return registryNamespace + '/' + DockerInstanceProvider.MACHINE_SNAPSHOT_PREFIX + NameGenerator.generate(null, 16);
        }
        return DockerInstanceProvider.MACHINE_SNAPSHOT_PREFIX + NameGenerator.generate(null, 16);
    }

    public void destroy() throws MachineException {
        try {
            outputConsumer.close();
        } catch (IOException ignored) {}

        machineProcesses.clear();
        dockerInstanceStopDetector.stopDetection(container);
        try {
            if (getConfig().isDev()) {
                node.unbindWorkspace();
            }

            // kill container is not needed here, because we removing container with force flag
            docker.removeContainer(RemoveContainerParams.create(container)
                                                        .withRemoveVolumes(true)
                                                        .withForce(true));
        } catch (IOException | ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException(e.getLocalizedMessage());
        }

        try {
            docker.removeImage(RemoveImageParams.create(image).withForce(false));
        } catch (IOException ignore) {
            LOG.error("IOException during destroy(). Ignoring.");
        }
    }

    public DockerNode getNode() {
        return node;
    }

    /**
     * Reads file content by specified file path.
     *
     * TODO:
     * add file size checking,
     * note that size checking and file content reading
     * should be done in an atomic way,
     * which means that two separate instance processes is not the case.
     *
     * @param filePath
     *         path to file on machine instance
     * @param startFrom
     *         line number to start reading from
     * @param limit
     *         limitation on line
     * @return if {@code limit} and {@code startFrom} grater than 0
     * content from {@code startFrom} to {@code startFrom + limit} will be returned,
     * if file contains less lines than {@code startFrom} empty content will be returned
     * @throws MachineException
     *         if any error occurs with file reading
     */
    /*
    public String readFileContent(String filePath, int startFrom, int limit) throws MachineException {
        if (limit <= 0 || startFrom <= 0) {
            throw new MachineException("Impossible to read file " + limit + " lines from " + startFrom + " line");
        }

        // command sed getting file content from startFrom line to (startFrom + limit)
        String shCommand = format("sed -n \'%1$2s, %2$2sp\' %3$2s", startFrom, startFrom + limit, filePath);

        final String[] command = {"/bin/sh", "-c", shCommand};

        ListLineConsumer lines = new ListLineConsumer();
        try {
            Exec exec = docker.createExec(CreateExecParams.create(container, command).withDetach(false));
            docker.startExec(StartExecParams.create(exec.getId()), new LogMessagePrinter(lines, LogMessage::getContent));
        } catch (IOException e) {
            throw new MachineException(format("Error occurs while initializing command %s in docker container %s: %s",
                                              Arrays.toString(command), container, e.getLocalizedMessage()), e);
        }

        String content = lines.getText();
        if (content.contains("sed: can't read " + filePath + ": No such file or directory") ||
            content.contains("cat: " + filePath + ": No such file or directory")) {
            throw new MachineException("File with path " + filePath + " not found");
        }
        return content;
    }

    public void copy(Instance sourceMachine, String sourcePath, String targetPath, boolean overwriteDirNonDir) throws MachineException {
        if (!(sourceMachine instanceof DockerInstance)) {
            throw new MachineException("Unsupported copying between not docker machines");
        }
        try {
            docker.putResource(PutResourceParams.create(container,
                                                        targetPath,
                                                        docker.getResource(GetResourceParams.create(
                                                                ((DockerInstance)sourceMachine).container, sourcePath)))
                                                .withNoOverwriteDirNonDir(overwriteDirNonDir));
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage());
        }
    }

    /**
     * Not implemented.<p/>
     *
     * {@inheritDoc}
     */
    /*
    public void copy(String sourcePath, String targetPath) throws MachineException {
        throw new MachineException("Unsupported operation for docker machine implementation");
    }

    private MachineRuntimeInfoImpl doGetRuntime() throws MachineException {
        try {
            return new MachineRuntimeInfoImpl(dockerMachineFactory.createMetadata(docker.inspectContainer(container),
                                                                                  getConfig(),
                                                                                  node.getHost()));
        } catch (IOException x) {
            throw new MachineException(x.getMessage(), x);
        }
    }*/
}
