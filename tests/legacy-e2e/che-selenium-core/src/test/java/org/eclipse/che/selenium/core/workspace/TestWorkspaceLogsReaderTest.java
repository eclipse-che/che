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
package org.eclipse.che.selenium.core.workspace;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.workspace.TestWorkspaceLogsReader.LogInfo.create;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceLogsReader.LogInfo;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Unit tests for the {@link TestWorkspaceLogsReader}
 *
 * @author Dmytro Nochevnov
 */
@Listeners(value = MockitoTestNGListener.class)
public class TestWorkspaceLogsReaderTest {

  private static final LogInfo FIRST_LOG_INFO = create("log-1", Paths.get("log-1-location"));
  private static final LogInfo SECOND_LOG_INFO = create("log-2", Paths.get("log-2-location"));
  private static final ImmutableList<LogInfo> TEST_LOG_INFOS =
      ImmutableList.of(FIRST_LOG_INFO, SECOND_LOG_INFO);
  private static final String TEST_READ_FIRST_LOG_COMMAND = "echo 'read-log-1'";
  private static final String TEST_READ_SECOND_LOG_COMMAND = "echo 'read-log-2'";
  private static final String UNKNOWN_COMMAND = "command-435f4q6we3as5va5s";
  private static final String TEST_WORKSPACE_ID = "workspace-id";
  private static final Path PATH_TO_STORE_LOGS =
      Paths.get(TestWorkspaceLogsReaderTest.class.getResource("").getPath());
  private static final WorkspaceStatus WRONG_WORKSPACE_STATUS = WorkspaceStatus.STOPPED;

  @Spy private TestWorkspaceLogsReader testWorkspaceLogsReader;

  @Mock private TestWorkspace testWorkspace;
  @Mock private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Mock private Logger log;

  @BeforeMethod
  public void setup() throws ExecutionException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    testWorkspaceLogsReader.workspaceServiceClient = testWorkspaceServiceClient;
    testWorkspaceLogsReader.log = log;
    testWorkspaceLogsReader.processAgent = new ProcessAgent();

    lenient().doReturn(TEST_WORKSPACE_ID).when(testWorkspace).getId();

    // init logs read commands
    lenient().doReturn(TEST_LOG_INFOS).when(testWorkspaceLogsReader).getLogInfos();

    lenient()
        .doReturn(TEST_READ_FIRST_LOG_COMMAND)
        .when(testWorkspaceLogsReader)
        .getReadLogsCommand(
            TEST_WORKSPACE_ID,
            Paths.get(
                format(
                    "%s/%s/%s", PATH_TO_STORE_LOGS, TEST_WORKSPACE_ID, FIRST_LOG_INFO.getName())),
            FIRST_LOG_INFO.getLocationInsideWorkspace());

    lenient()
        .doReturn(TEST_READ_SECOND_LOG_COMMAND)
        .when(testWorkspaceLogsReader)
        .getReadLogsCommand(
            TEST_WORKSPACE_ID,
            Paths.get(
                format(
                    "%s/%s/%s", PATH_TO_STORE_LOGS, TEST_WORKSPACE_ID, SECOND_LOG_INFO.getName())),
            SECOND_LOG_INFO.getLocationInsideWorkspace());
  }

  @Test
  public void shouldReadLogSuccessfully() throws Exception {
    // given
    doReturn(true).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doReturn(WorkspaceStatus.RUNNING).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.store(testWorkspace, PATH_TO_STORE_LOGS, false);

    // then
    verifyZeroInteractions(log);
  }

  @Test
  public void shouldReadLogIfWorkspaceIsNotRunning() throws Exception {
    // given
    doReturn(true).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doReturn(WRONG_WORKSPACE_STATUS).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.store(testWorkspace, PATH_TO_STORE_LOGS, false);

    // then
    verifyZeroInteractions(log);
  }

  @Test
  public void shouldAbortExecutionIfWorkspaceCannotBeRead() throws Exception {
    // given
    doReturn(false).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();

    // when
    testWorkspaceLogsReader.store(testWorkspace, PATH_TO_STORE_LOGS, false);

    // then
    verify(testWorkspaceLogsReader, never()).getLogInfos();
    verifyZeroInteractions(log);
  }

  @Test
  public void shouldHandleCommandError() throws Exception {
    // given
    doReturn(UNKNOWN_COMMAND)
        .when(testWorkspaceLogsReader)
        .getReadLogsCommand(
            TEST_WORKSPACE_ID,
            Paths.get(
                format(
                    "%s/%s/%s", PATH_TO_STORE_LOGS, TEST_WORKSPACE_ID, FIRST_LOG_INFO.getName())),
            FIRST_LOG_INFO.getLocationInsideWorkspace());

    doReturn(true).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doReturn(WorkspaceStatus.RUNNING).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.store(testWorkspace, PATH_TO_STORE_LOGS, false);

    // then
    ArgumentCaptor<String> logArgumentCaptor1 = forClass(String.class);
    ArgumentCaptor<String> logArgumentCaptor2 = forClass(String.class);
    ArgumentCaptor<String> logArgumentCaptor3 = forClass(String.class);
    ArgumentCaptor<Path> logArgumentCaptor4 = forClass(Path.class);
    ArgumentCaptor<ProcessAgentException> logArgumentCaptor5 =
        forClass(ProcessAgentException.class);

    verify(log)
        .warn(
            logArgumentCaptor1.capture(),
            logArgumentCaptor2.capture(),
            logArgumentCaptor3.capture(),
            logArgumentCaptor4.capture(),
            logArgumentCaptor5.capture());

    assertEquals(
        logArgumentCaptor1.getValue(),
        "Can't obtain '{}' logs from workspace with id='{}' from directory '{}'.");
    assertEquals(logArgumentCaptor2.getValue(), FIRST_LOG_INFO.getName());
    assertEquals(logArgumentCaptor3.getValue(), TEST_WORKSPACE_ID);
    assertEquals(logArgumentCaptor4.getValue(), FIRST_LOG_INFO.getLocationInsideWorkspace());

    assertNotNull(logArgumentCaptor5.getValue());
    String errorMessage = logArgumentCaptor5.getValue().getMessage();
    assertTrue(
        errorMessage.contains(UNKNOWN_COMMAND), "Actual errorMessage content: " + errorMessage);
  }

  @Test
  public void shouldSuppressCommandErrorWarnings() throws Exception {
    // given
    doReturn(UNKNOWN_COMMAND)
        .when(testWorkspaceLogsReader)
        .getReadLogsCommand(
            TEST_WORKSPACE_ID,
            Paths.get(
                format(
                    "%s/%s/%s", PATH_TO_STORE_LOGS, TEST_WORKSPACE_ID, FIRST_LOG_INFO.getName())),
            FIRST_LOG_INFO.getLocationInsideWorkspace());

    doReturn(true).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doReturn(WorkspaceStatus.RUNNING).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.store(testWorkspace, PATH_TO_STORE_LOGS, true);

    // then
    verifyZeroInteractions(log);
  }
}
