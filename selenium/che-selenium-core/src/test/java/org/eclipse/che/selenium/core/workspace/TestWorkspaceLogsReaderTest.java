/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.workspace.TestWorkspaceLogsReader.LogInfo.create;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
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
  private static final Path PATH_TO_STORE_LOGS = Paths.get("path-to-store-logs");
  private static final WorkspaceStatus WRONG_WORKSPACE_STATUS = WorkspaceStatus.STOPPED;
  private static final NullPointerException EXCEPTION_TO_BE_THROWN = new NullPointerException();

  @Spy private TestWorkspaceLogsReader testWorkspaceLogsReader;

  @Mock private TestWorkspace testWorkspace;
  @Mock private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Mock private Logger log;

  private ListLineConsumer testStdoutOfFirstLogRead = new ListLineConsumer();
  private ListLineConsumer testStderrOfFirstLogRead = new ListLineConsumer();

  private ListLineConsumer testStdoutOfSecondLogRead = new ListLineConsumer();
  private ListLineConsumer testStderrOfSecondLogRead = new ListLineConsumer();

  @BeforeMethod
  public void setup() throws ExecutionException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    testWorkspaceLogsReader.workspaceServiceClient = testWorkspaceServiceClient;
    testWorkspaceLogsReader.log = log;

    doReturn(TEST_WORKSPACE_ID).when(testWorkspace).getId();

    // init logs read commands
    doReturn(TEST_LOG_INFOS).when(testWorkspaceLogsReader).getLogInfos();

    doReturn(TEST_READ_FIRST_LOG_COMMAND)
        .when(testWorkspaceLogsReader)
        .getReadLogsCommand(
            TEST_WORKSPACE_ID,
            Paths.get(
                format(
                    "%s/%s/%s", PATH_TO_STORE_LOGS, TEST_WORKSPACE_ID, FIRST_LOG_INFO.getName())),
            FIRST_LOG_INFO.getLocationInsideWorkspace());

    doReturn(TEST_READ_SECOND_LOG_COMMAND)
        .when(testWorkspaceLogsReader)
        .getReadLogsCommand(
            TEST_WORKSPACE_ID,
            Paths.get(
                format(
                    "%s/%s/%s", PATH_TO_STORE_LOGS, TEST_WORKSPACE_ID, SECOND_LOG_INFO.getName())),
            SECOND_LOG_INFO.getLocationInsideWorkspace());

    testStdoutOfFirstLogRead.clear();
    testStdoutOfSecondLogRead.clear();
    doReturn(testStdoutOfFirstLogRead)
        .doReturn(testStdoutOfSecondLogRead)
        .when(testWorkspaceLogsReader)
        .getStdoutConsumer();

    testStderrOfFirstLogRead.clear();
    testStderrOfSecondLogRead.clear();
    doReturn(testStderrOfFirstLogRead)
        .doReturn(testStderrOfSecondLogRead)
        .when(testWorkspaceLogsReader)
        .getStderrConsumer();
  }

  @Test
  public void shouldReadLogSuccessfully() throws Exception {
    // given
    doReturn(true).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doReturn(WorkspaceStatus.RUNNING).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.read(testWorkspace, PATH_TO_STORE_LOGS);

    // then
    verifyZeroInteractions(log);
    assertEquals(testStdoutOfFirstLogRead.getText(), "read-log-1");
    assertEquals(testStderrOfFirstLogRead.getText(), "");

    assertEquals(testStdoutOfSecondLogRead.getText(), "read-log-2");
    assertEquals(testStderrOfSecondLogRead.getText(), "");
  }

  @Test
  public void shouldAbortExecutionIfWorkspaceIsNotRunning() throws Exception {
    // given
    doReturn(true).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doReturn(WRONG_WORKSPACE_STATUS).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.read(testWorkspace, PATH_TO_STORE_LOGS);

    // then
    verify(testWorkspaceLogsReader, never()).getLogInfos();
    verify(log)
        .warn(
            "It's impossible to get logs of workspace with id='{}' because of improper status '{}'",
            "workspace-id",
            WRONG_WORKSPACE_STATUS);

    assertEquals(testStdoutOfFirstLogRead.getText(), "");
    assertEquals(testStderrOfFirstLogRead.getText(), "");
    assertEquals(testStdoutOfSecondLogRead.getText(), "");
    assertEquals(testStderrOfSecondLogRead.getText(), "");
  }

  @Test
  public void shouldAbortExecutionIfWorkspaceCannotBeRead() throws Exception {
    // given
    doReturn(false).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doReturn(WorkspaceStatus.RUNNING).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.read(testWorkspace, PATH_TO_STORE_LOGS);

    // then
    verify(testWorkspaceLogsReader, never()).getLogInfos();
    verifyZeroInteractions(log);

    assertEquals(testStdoutOfFirstLogRead.getText(), "");
    assertEquals(testStderrOfFirstLogRead.getText(), "");
    assertEquals(testStdoutOfSecondLogRead.getText(), "");
    assertEquals(testStderrOfSecondLogRead.getText(), "");
  }

  @Test
  public void shouldAbortExecutionIfItIsImpossibleToGetWorkspaceStatus() throws Exception {
    // given
    doReturn(true).when(testWorkspaceLogsReader).canWorkspaceLogsBeRead();
    doThrow(EXCEPTION_TO_BE_THROWN).when(testWorkspaceServiceClient).getStatus(TEST_WORKSPACE_ID);

    // when
    testWorkspaceLogsReader.read(testWorkspace, PATH_TO_STORE_LOGS);

    // then
    verify(testWorkspaceLogsReader, never()).getLogInfos();
    verify(log)
        .warn(
            "It's impossible to get status of workspace with id='{}'",
            "workspace-id",
            EXCEPTION_TO_BE_THROWN);

    assertEquals(testStdoutOfFirstLogRead.getText(), "");
    assertEquals(testStderrOfFirstLogRead.getText(), "");
    assertEquals(testStdoutOfSecondLogRead.getText(), "");
    assertEquals(testStderrOfSecondLogRead.getText(), "");
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
    testWorkspaceLogsReader.read(testWorkspace, PATH_TO_STORE_LOGS);

    // then
    ArgumentCaptor<String> logArgumentCaptor1 = forClass(String.class);
    ArgumentCaptor<String> logArgumentCaptor2 = forClass(String.class);
    ArgumentCaptor<String> logArgumentCaptor3 = forClass(String.class);
    ArgumentCaptor<Path> logArgumentCaptor4 = forClass(Path.class);
    ArgumentCaptor<String> logArgumentCaptor5 = forClass(String.class);

    verify(log)
        .warn(
            logArgumentCaptor1.capture(),
            logArgumentCaptor2.capture(),
            logArgumentCaptor3.capture(),
            logArgumentCaptor4.capture(),
            logArgumentCaptor5.capture());

    assertEquals(
        logArgumentCaptor1.getValue(),
        "Can't obtain '{}' logs from workspace with id='{}' from directory '{}'. Error: {}");
    assertEquals(logArgumentCaptor2.getValue(), FIRST_LOG_INFO.getName());
    assertEquals(logArgumentCaptor3.getValue(), TEST_WORKSPACE_ID);
    assertEquals(logArgumentCaptor4.getValue(), FIRST_LOG_INFO.getLocationInsideWorkspace());

    String errorMessage = logArgumentCaptor5.getValue();
    assertTrue(
        errorMessage.contains(UNKNOWN_COMMAND), "Actual errorMessage content: " + errorMessage);

    assertEquals(testStdoutOfFirstLogRead.getText(), "");
    assertTrue(
        testStderrOfFirstLogRead.getText().contains(UNKNOWN_COMMAND),
        "Actual stderr content: " + testStderrOfFirstLogRead.getText());

    assertEquals(testStdoutOfSecondLogRead.getText(), "read-log-2");
    assertEquals(testStderrOfSecondLogRead.getText(), "");
  }
}
