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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.params.CommitParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.PushParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.lang.String.format;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link DockerInstance}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class DockerInstanceTest {
    private static final String        FILE_PATH    = "/tmp";
    private static final String        CONTAINER    = "container144";
    private static final String        OWNER        = "owner12";
    private static final String        IMAGE        = "image12";
    private static final String        EXEC_ID      = "exec_id12";
    private static final String        MACHINE_ID   = "machine12";
    private static final String        WORKSPACE_ID = "workspace12";
    private static final String        NAME         = "suse-jdk";
    private static final String        TYPE         = "docker";
    private static final String        REGISTRY     = "registry";
    private static final String        USERNAME     = "username";
    private static final String        REPOSITORY   = "eclipse-che";
    private static final String        TAG          = "latest";
    private static final MachineStatus STATUS       = MachineStatus.RUNNING;

    @Mock
    private LogMessage                 logMessageMock;
    @Mock
    private Exec                       execMock;
    @Mock
    private DockerConnector            dockerConnectorMock;
    @Mock
    private DockerInstanceStopDetector dockerInstanceStopDetectorMock;
    @Mock
    private LineConsumer               outputConsumer;

    private DockerInstance dockerInstance;

    @BeforeMethod
    public void setUp() throws IOException {
        dockerInstance = getDockerInstance();
        when(dockerConnectorMock.createExec(any(CreateExecParams.class))).thenReturn(execMock);
        when(execMock.getId()).thenReturn(EXEC_ID);
        doAnswer(invoke -> {
            @SuppressWarnings("unchecked")
            MessageProcessor<LogMessage> msgProc = (MessageProcessor<LogMessage>)invoke.getArguments()[1];
            msgProc.process(logMessageMock);
            return msgProc;
        }).when(dockerConnectorMock)
          .startExec(any(StartExecParams.class), any());
    }

    @Test(expectedExceptions = MachineException.class)
    public void shouldThrowMachineExceptionWhenStartFromIsNegative() throws Exception {
        dockerInstance.readFileContent(FILE_PATH, -1, -10);
    }

    @Test(expectedExceptions = MachineException.class)
    public void shouldThrowMachineExceptionWhenExecProblemOccurs() throws Exception {
        when(dockerConnectorMock.createExec(any(CreateExecParams.class))).thenThrow(new IOException("File not found"));

        dockerInstance.readFileContent(FILE_PATH, -1, 10);
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "File with path " + FILE_PATH + " not found")
    public void shouldThrowFileNotFoundDuringSedCommand() throws Exception {
        when(logMessageMock.getContent()).thenReturn("sed: can't read " + FILE_PATH + ": No such file or directory");

        dockerInstance.readFileContent(FILE_PATH, 1, 10);
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "File with path " + FILE_PATH + " not found")
    public void shouldThrowFileNotFoundDuringCatCommand() throws Exception {
        when(logMessageMock.getContent()).thenReturn("cat: " + FILE_PATH + ": No such file or directory");

        dockerInstance.readFileContent(FILE_PATH, 1, 10);
    }

    @Test
    public void shouldFullyReadFileContent() throws Exception {
        final String content = "content";
        when(logMessageMock.getContent()).thenReturn(content);

        final String res = dockerInstance.readFileContent(FILE_PATH, 1, 10);

        assertEquals(res.trim(), content);
    }

    @Test
    public void shouldCreateDockerImageLocally() throws Exception {
        final String comment = format("Suspended at %1$ta %1$tb %1$td %1$tT %1$tZ %1$tY",
                                      System.currentTimeMillis());

        dockerInstance.commitContainer(REPOSITORY, TAG);

        verify(dockerConnectorMock, times(1)).commit(CommitParams.create(CONTAINER)
                                                                 .withRepository(REPOSITORY)
                                                                 .withTag(TAG)
                                                                 .withComment(comment));
    }

    @Test
    public void shouldSaveDockerInstanceStateIntoLocalImage() throws Exception {
        final MachineSource result = dockerInstance.saveToSnapshot();

        assertTrue(result instanceof DockerMachineSource);
        DockerMachineSource dockerMachineSource = (DockerMachineSource) result;
        assertEquals(dockerMachineSource.getTag(), TAG);
        assertNotNull(dockerMachineSource.getRepository());
        assertEquals(dockerMachineSource.getRegistry(), null);
    }

    @Test
    public void shouldCloseOutputConsumerOnDestroy() throws Exception {
        dockerInstance.destroy();

        verify(outputConsumer).close();
    }

    @Test
    public void shouldSaveDockerInstanceStateIntoRepository() throws Exception {
        final String digest = "image12";
        dockerInstance = getDockerInstance(getMachine(), REGISTRY, CONTAINER, IMAGE, true);
        when(dockerConnectorMock.push(any(PushParams.class), any(ProgressMonitor.class))).thenReturn(digest);

        final MachineSource result = dockerInstance.saveToSnapshot();

        assertTrue(result instanceof DockerMachineSource);
        DockerMachineSource dockerMachineSource = (DockerMachineSource) result;
        assertEquals(dockerMachineSource.getTag(), TAG);
        assertEquals(dockerMachineSource.getDigest(), digest);
        assertEquals(dockerMachineSource.getRegistry(), REGISTRY);
    }

    @Test(expectedExceptions = MachineException.class)
    public void shouldThrowMachineExceptionWhenDockerCommitFailed() throws Exception{
        when(dockerConnectorMock.commit(any(CommitParams.class))).thenThrow(new IOException("err"));

        dockerInstance.saveToSnapshot();
    }

    @Test(expectedExceptions = MachineException.class)
    public void shouldThrowMachineExceptionWhenDockerPushInterrupted() throws Exception {
        dockerInstance = getDockerInstance(getMachine(), REGISTRY, CONTAINER, IMAGE, true);
        when(dockerConnectorMock.push(any(PushParams.class),
                                      any(ProgressMonitor.class))).thenThrow(new IOException("err"));

        dockerInstance.saveToSnapshot();
    }

    private DockerInstance getDockerInstance() {
        return getDockerInstance(getMachine(), REGISTRY, CONTAINER, IMAGE, false);
    }

    private DockerInstance getDockerInstance(Machine machine,
                                             String registry,
                                             String container,
                                             String image,
                                             boolean snapshotUseRegistry) {
        return new DockerInstance(dockerConnectorMock,
                                  registry,
                                  USERNAME,
                                  mock(DockerMachineFactory.class),
                                  machine,
                                  container,
                                  image,
                                  mock(DockerNode.class),
                                  outputConsumer,
                                  dockerInstanceStopDetectorMock,
                                  mock(DockerInstanceProcessesCleaner.class),
                                  snapshotUseRegistry);
    }

    private Machine getMachine() {
        return getMachine(getMachineConfig(), OWNER, MACHINE_ID, WORKSPACE_ID, STATUS);
    }

    private Machine getMachine(MachineConfig config,
                               String owner,
                               String machineId,
                               String wsId,
                               MachineStatus status) {
        return MachineImpl.builder()
                          .setConfig(config)
                          .setId(machineId)
                          .setOwner(owner)
                          .setWorkspaceId(wsId)
                          .setEnvName("env")
                          .setStatus(status)
                          .build();
    }

    private MachineConfig getMachineConfig() {
        return getMachineConfig(true, NAME, TYPE);
    }

    private MachineConfig getMachineConfig(boolean isDev, String name, String type) {
        return MachineConfigImpl.builder()
                                .setDev(isDev)
                                .setName(name)
                                .setType(type)
                                .setSource(new MachineSourceImpl("docker").setLocation("location"))
                                .setLimits(new MachineLimitsImpl(64))
                                .build();
    }
}
