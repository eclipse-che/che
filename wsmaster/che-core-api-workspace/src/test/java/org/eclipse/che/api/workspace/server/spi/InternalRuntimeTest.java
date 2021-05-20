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
package org.eclipse.che.api.workspace.server.spi;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.UNKNOWN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.test.mockito.answer.WaitingAnswer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class InternalRuntimeTest {
  private static final long SECOND_IN_MILLISECONDS = 1_000L;
  private static final String RUNTIME_STARTED_EXC_MESSAGE = "Runtime already started";
  private static final URLRewriter TEST_URL_REWRITER = new TestURLRewriter();

  private ExecutorService executor;
  private TestInternalRuntime internalRuntime;

  @BeforeMethod
  public void setUp() throws Exception {
    executor = createExecutor();
    internalRuntime = null;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    executor.shutdownNow();
    executor.awaitTermination(1, TimeUnit.SECONDS);
    assertTrue(executor.isTerminated());
  }

  @Test
  public void shouldStartRuntime() throws Exception {
    // given
    setNewRuntime();

    // when
    internalRuntime.start(emptyMap());

    // then
    verify(internalRuntime).internalStart(emptyMap());
  }

  @Test(
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = RUNTIME_STARTED_EXC_MESSAGE)
  public void shouldNotStartRunningRuntime() throws Exception {
    // given
    setRunningRuntime();

    // when
    internalRuntime.start(emptyMap());
  }

  @Test(
      timeOut = SECOND_IN_MILLISECONDS,
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = RUNTIME_STARTED_EXC_MESSAGE)
  public void shouldNotStartStartingRuntime() throws Exception {
    // given
    setStartingRuntime();

    // when
    internalRuntime.start(emptyMap());
  }

  @Test(
      timeOut = SECOND_IN_MILLISECONDS,
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = RUNTIME_STARTED_EXC_MESSAGE)
  public void shouldNotStartStoppingRuntime() throws Exception {
    // given
    setStoppingRuntime();

    // when
    internalRuntime.start(emptyMap());
  }

  @Test(
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = RUNTIME_STARTED_EXC_MESSAGE)
  public void shouldNotStartStoppedRuntime() throws Exception {
    // given
    setRunningRuntime();
    internalRuntime.stop(emptyMap());

    // when
    internalRuntime.start(emptyMap());
  }

  @Test(
      dataProvider = "excProvider",
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = RUNTIME_STARTED_EXC_MESSAGE)
  public void shouldNotStartRuntimeThatThrewExceptionOnPreviousStart(Exception e) throws Exception {
    // given
    setNewRuntime();
    doThrow(e).when(internalRuntime).internalStart(emptyMap());
    try {
      internalRuntime.start(emptyMap());
    } catch (Exception ignored) {
    }

    // when
    internalRuntime.start(emptyMap());
  }

  @DataProvider(name = "excProvider")
  public static Object[][] excProvider() {
    return new Object[][] {
      {new InfrastructureException("")},
      {new InternalInfrastructureException("")},
      {new RuntimeException()}
    };
  }

  @Test
  public void shouldStopRuntimeCreatedAsRunning() throws Exception {
    // given
    setRunningRuntime();

    // when
    internalRuntime.stop(emptyMap());

    // then
    verify(internalRuntime).internalStop(emptyMap());
  }

  @Test
  public void shouldStopStartedRuntime() throws Exception {
    // given
    setNewRuntime();
    internalRuntime.start(emptyMap());

    // when
    internalRuntime.stop(emptyMap());

    // then
    verify(internalRuntime).internalStop(emptyMap());
  }

  @Test(timeOut = SECOND_IN_MILLISECONDS)
  public void shouldStopStartingRuntime() throws Exception {
    // given
    setStartingRuntime();

    // when
    internalRuntime.stop(emptyMap());

    // then
    verify(internalRuntime).internalStart(emptyMap());
    verify(internalRuntime).internalStop(emptyMap());
  }

  @Test(
      timeOut = SECOND_IN_MILLISECONDS,
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = "The environment must be running or starting")
  public void shouldNotStopStoppingRuntime() throws Exception {
    // given
    setStoppingRuntime();

    // when
    internalRuntime.stop(emptyMap());
  }

  @Test(
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = RUNTIME_STARTED_EXC_MESSAGE)
  public void shouldNotStartRuntimeAfterStopThatCaughtInfrastructureExceptionInInternalStop()
      throws Exception {
    // given
    setRunningRuntime();
    doThrow(new InfrastructureException("")).when(internalRuntime).internalStop(any());
    try {
      internalRuntime.stop(emptyMap());
    } catch (InfrastructureException ignore) {
    }

    // when
    internalRuntime.start(emptyMap());
  }

  @Test(
      dataProvider = "excProvider",
      expectedExceptions = StateException.class,
      expectedExceptionsMessageRegExp = RUNTIME_STARTED_EXC_MESSAGE)
  public void shouldNotStartRuntimeAfterStopThatCaughtExceptionFromInternalStop(Exception e)
      throws Exception {
    // given
    setRunningRuntime();
    doThrow(e).when(internalRuntime).internalStop(any());
    try {
      internalRuntime.stop(emptyMap());
    } catch (Exception ignored) {
    }

    // when
    internalRuntime.start(emptyMap());
  }

  @Test(timeOut = SECOND_IN_MILLISECONDS, dataProvider = "getMachinesResultProvider")
  public void shouldGetMachinesWithRewrittenURLsFromStartingRuntime(
      Map<String, ? extends Machine> internalMachines, Map<String, ? extends Machine> expected)
      throws Exception {
    // given
    setStartingRuntime();
    doReturn(internalMachines).when(internalRuntime).getInternalMachines();

    // when
    Map<String, ? extends Machine> actual = internalRuntime.getMachines();

    // then
    assertEquals(actual, expected);
  }

  @DataProvider(name = "getMachinesResultProvider")
  public static Object[][] getMachinesResultProvider() throws Exception {
    return new Object[][] {
      {ImmutableMap.of(), ImmutableMap.of()},
      {
        ImmutableMap.of(
            "m1", createMachine(),
            "m2", createMachine()),
        ImmutableMap.of(
            "m1", rewriteURLs(createMachine()),
            "m2", rewriteURLs(createMachine()))
      },
    };
  }

  @Test(dataProvider = "getMachinesResultProvider")
  public void shouldGetMachinesWithRewrittenURLsFromRunningRuntime(
      Map<String, ? extends Machine> internalMachines, Map<String, ? extends Machine> expected)
      throws Exception {
    // given
    setRunningRuntime();
    doReturn(internalMachines).when(internalRuntime).getInternalMachines();

    // when
    Map<String, ? extends Machine> actual = internalRuntime.getMachines();

    // then
    assertEquals(actual, expected);
  }

  @Test(timeOut = SECOND_IN_MILLISECONDS, dataProvider = "getMachinesResultProvider")
  public void shouldGetMachinesWithRewrittenURLsFromStoppingRuntime(
      Map<String, ? extends Machine> internalMachines, Map<String, ? extends Machine> expected)
      throws Exception {
    // given
    setStoppingRuntime();
    doReturn(internalMachines).when(internalRuntime).getInternalMachines();

    // when
    Map<String, ? extends Machine> actual = internalRuntime.getMachines();

    // then
    assertEquals(actual, expected);
  }

  @Test
  public void shouldNotRewriteURLsOfInternalServers() throws Exception {
    // given
    ServerImpl internalServer =
        createServer(singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, "true"));
    ServerImpl regularServer = createServer(RUNNING);
    MachineImpl machineWithInternalServer =
        new MachineImpl(
            createAttributes(),
            ImmutableMap.of("server1", regularServer, "server2", internalServer),
            MachineStatus.RUNNING);
    ImmutableMap<String, MachineImpl> internalMachines =
        ImmutableMap.of("m1", createMachine(), "m2", machineWithInternalServer);
    ImmutableMap<String, MachineImpl> expected =
        ImmutableMap.of(
            "m1",
            rewriteURLs(createMachine()),
            "m2",
            new MachineImpl(
                createAttributes(),
                ImmutableMap.of("server1", rewriteURL(regularServer), "server2", internalServer),
                MachineStatus.RUNNING));
    setRunningRuntime();
    doReturn(internalMachines).when(internalRuntime).getInternalMachines();

    // when
    Map<String, ? extends Machine> actual = internalRuntime.getMachines();

    // then
    assertEquals(actual, expected);
  }

  @Test
  public void
      getMachinesResultShouldNotBeAffectedByFollowingModificationOfResultOfGetInternalMachines()
          throws Exception {

    // given
    setRunningRuntime(new URLRewriter.NoOpURLRewriter());
    String originMachineName = "exp_m";
    String originServerName = "exp_s";
    ServerStatus originServerStatus = ServerStatus.UNKNOWN;
    String originServerUrl = "https://expected.url:1000";
    Map<String, String> originProps = ImmutableMap.of("origProp1", "value1");
    int initialPropsSize = originProps.size();
    HashMap<String, MachineImpl> originInternalMachines =
        createMachines(
            originMachineName, originProps, originServerName, originServerUrl, originServerStatus);
    int initialMachinesAmount = originInternalMachines.size();
    int initialServersAmountInOriginMachine =
        originInternalMachines.get(originMachineName).getServers().size();

    doReturn(originInternalMachines).when(internalRuntime).getInternalMachines();

    // when
    Map<String, ? extends Machine> actualMachines = internalRuntime.getMachines();
    // verify that retrieved state is equal to the origin one
    assertValues(
        actualMachines,
        initialMachinesAmount,
        originMachineName,
        initialPropsSize,
        initialServersAmountInOriginMachine,
        originServerName,
        originServerUrl,
        originServerStatus);

    // modify origin machines
    modifyMachines(originInternalMachines, originMachineName, originServerName);

    // then
    // ensure actual values retrieved from runtime
    // are not changed automatically after changes in origin internal runtime
    assertValues(
        actualMachines,
        initialMachinesAmount,
        originMachineName,
        initialPropsSize,
        initialServersAmountInOriginMachine,
        originServerName,
        originServerUrl,
        originServerStatus);
  }

  private HashMap<String, MachineImpl> createMachines(
      String expectedMachineName,
      Map<String, String> expectedProps,
      String expectedServerName,
      String expectedServerUrl,
      ServerStatus expectedServerStatus)
      throws Exception {
    MachineImpl expectedMachine =
        new MachineImpl(
            expectedProps,
            singletonMap(
                expectedServerName,
                new ServerImpl().withUrl(expectedServerUrl).withStatus(expectedServerStatus)),
            MachineStatus.RUNNING);
    HashMap<String, MachineImpl> result = new HashMap<>();
    result.put("m1", createMachine());
    result.put("m2", createMachine());
    result.put(expectedMachineName, expectedMachine);
    return result;
  }

  private void assertValues(
      Map<String, ? extends Machine> actualMachines,
      int expectedMachinesAmount,
      String expectedMachineName,
      int expectedMachinePropsSize,
      int expectedMachineServersSize,
      String expectedServerName,
      String expectedServerUrl,
      ServerStatus expectedServerStatus) {
    assertEquals(actualMachines.size(), expectedMachinesAmount);
    assertTrue(actualMachines.containsKey(expectedMachineName));
    Machine actualMachine = actualMachines.get(expectedMachineName);
    assertEquals(actualMachine.getAttributes().size(), expectedMachinePropsSize);
    assertEquals(actualMachine.getServers().size(), expectedMachineServersSize);
    assertTrue(actualMachine.getServers().containsKey(expectedServerName));
    assertEquals(
        actualMachine.getServers().get(expectedServerName),
        new ServerImpl()
            .withUrl(expectedServerUrl)
            .withStatus(expectedServerStatus)
            .withAttributes(emptyMap()));
  }

  private void modifyMachines(
      HashMap<String, MachineImpl> originInternalMachines,
      String machineToModify,
      String serverToModify)
      throws Exception {
    // add new machine
    originInternalMachines.put("newM", createMachine());
    MachineImpl originMachine = originInternalMachines.get(machineToModify);
    // change properties of origin server
    originMachine.getAttributes().put("new_prop", "new_value");
    // add new server in origin machine
    originMachine.getServers().put("newS", createServer(RUNNING));
    ServerImpl originServer = originMachine.getServers().get(serverToModify);
    // change status and URL of origin server
    originServer.setStatus(RUNNING);
    originServer.setUrl("http://localhost:9191/new_url");
  }

  @Test
  public void shouldAddAWarningInsteadOfAServerIfURLRewritingFailed() throws Exception {
    // given
    URLRewriter urlRewriter = spy(new URLRewriter.NoOpURLRewriter());
    setRunningRuntime(urlRewriter);
    Map<String, MachineImpl> expectedMachines = new HashMap<>();
    Map<String, MachineImpl> internalMachines = new HashMap<>();
    MachineImpl machine1 = createMachine();
    MachineImpl machine2 = createMachine();
    HashMap<String, ServerImpl> expectedServers = new HashMap<>(machine1.getServers());
    String badServerName = "badServer";
    String badServerURL = "ws://failing-rewriting:8000";
    String badServerRewritingExcMessage = "test exc";
    ServerImpl failingRewritingServer = createServer(badServerURL);
    machine1.getServers().put(badServerName, failingRewritingServer);
    internalMachines.put("m1", machine1);
    internalMachines.put("m2", machine2);
    expectedMachines.put(
        "m1", new MachineImpl(machine1.getAttributes(), expectedServers, machine1.getStatus()));
    expectedMachines.put("m2", machine2);
    List<WarningImpl> expectedWarnings = new ArrayList<>();
    expectedWarnings.add(
        new WarningImpl(
            InternalRuntime.MALFORMED_SERVER_URL_FOUND,
            "Malformed URL for " + badServerName + " : " + badServerRewritingExcMessage));
    doReturn(internalMachines).when(internalRuntime).getInternalMachines();
    doThrow(new InfrastructureException(badServerRewritingExcMessage))
        .when(urlRewriter)
        .rewriteURL(any(RuntimeIdentity.class), any(), anyString(), eq(badServerURL));

    // when
    Map<String, ? extends Machine> actualMachines = internalRuntime.getMachines();
    List<? extends Warning> actualWarnings = internalRuntime.getWarnings();

    // then
    assertEquals(actualMachines, expectedMachines);
    assertEquals(actualWarnings, expectedWarnings);
  }

  private static MachineImpl createMachine() throws Exception {
    return new MachineImpl(createAttributes(), createServers(), MachineStatus.RUNNING);
  }

  private static Map<String, String> createAttributes() {
    return ImmutableMap.of(
        "prop1", "prop1Value",
        "prop2", "prop2Value",
        "prop3", "prop3Value");
  }

  private static Map<String, ServerImpl> createServers() throws Exception {
    return ImmutableMap.of(
        "server1", createServer(RUNNING),
        "server2", createServer(UNKNOWN),
        "server3", createServer(STOPPED));
  }

  private static ServerImpl createServer(ServerStatus status) throws Exception {
    return new ServerImpl().withUrl("http://localhost:8080/").withStatus(status);
  }

  private static ServerImpl createServer(String url) throws Exception {
    return new ServerImpl().withUrl(url).withStatus(RUNNING);
  }

  private static ServerImpl createServer(Map<String, String> attributes) throws Exception {
    return new ServerImpl()
        .withUrl("http://internal-dns:8080/")
        .withStatus(RUNNING)
        .withAttributes(attributes);
  }

  private static MachineImpl rewriteURLs(MachineImpl machine) throws InfrastructureException {
    for (Map.Entry<String, ServerImpl> serverEntry : machine.getServers().entrySet()) {
      serverEntry.setValue(rewriteURL(serverEntry.getValue()));
    }
    return machine;
  }

  private static ServerImpl rewriteURL(ServerImpl server) throws InfrastructureException {
    return new ServerImpl()
        .withStatus(server.getStatus())
        .withAttributes(server.getAttributes())
        .withUrl(TEST_URL_REWRITER.rewriteURL(null, null, null, server.getUrl()));
  }

  /**
   * Uses waits, so should be used with tests execution time limitation. See {@link Test#timeOut()}
   */
  private void setStartingRuntime() throws Exception {
    checkRuntime();
    setNewRuntime(TEST_URL_REWRITER);
    WaitingAnswer waitingAnswer = new WaitingAnswer();
    // do not let internalStart end until second start is called
    doAnswer(waitingAnswer).when(internalRuntime).internalStart(emptyMap());
    executor.submit(
        () -> {
          internalRuntime.start(emptyMap());
          return null;
        });
    waitingAnswer.waitAnswerCall(500, TimeUnit.MILLISECONDS);
  }

  /**
   * Uses waits, so should be used with tests execution time limitation. See {@link Test#timeOut()}
   */
  private void setStoppingRuntime() throws Exception {
    checkRuntime();
    setRunningRuntime(TEST_URL_REWRITER);
    WaitingAnswer waitingAnswer = new WaitingAnswer();
    // do not let internalStop end until start is called
    doAnswer(waitingAnswer).when(internalRuntime).internalStop(emptyMap());
    executor.submit(
        () -> {
          internalRuntime.stop(emptyMap());
          return null;
        });
    waitingAnswer.waitAnswerCall(500, TimeUnit.MILLISECONDS);
  }

  private void setRunningRuntime() throws Exception {
    setRunningRuntime(TEST_URL_REWRITER);
  }

  private void setRunningRuntime(URLRewriter urlRewriter) throws Exception {
    checkRuntime();
    internalRuntime = spy(new TestInternalRuntime(urlRewriter, true));
  }

  private void setNewRuntime() throws Exception {
    setNewRuntime(TEST_URL_REWRITER);
  }

  private void setNewRuntime(URLRewriter urlRewriter) throws Exception {
    checkRuntime();
    internalRuntime = spy(new TestInternalRuntime(urlRewriter, false));
  }

  private void checkRuntime() {
    if (internalRuntime != null) {
      throw new RuntimeException(
          "internalRuntime field is not null. Looks like multiple runtimes were created.");
    }
  }

  private ExecutorService createExecutor() {
    return Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat(this.getClass().getSimpleName() + "-%d")
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .build());
  }

  private static class TestURLRewriter implements URLRewriter {

    @Override
    public String rewriteURL(
        RuntimeIdentity identity, String machineName, String serverName, String url)
        throws InfrastructureException {
      return url + "#something";
    }
  }

  private static class TestInternalRuntime extends InternalRuntime<RuntimeContext> {
    public TestInternalRuntime(URLRewriter urlRewriter, boolean running)
        throws ValidationException, InfrastructureException {
      super(
          new TestRuntimeContext(
              new InternalEnvironment() {},
              new RuntimeIdentityImpl("ws", "env", "id", "infraNamespace"),
              null),
          urlRewriter,
          running ? WorkspaceStatus.RUNNING : null);
    }

    @Override
    protected Map<String, ? extends Machine> getInternalMachines() {
      return null;
    }

    @Override
    public List<? extends Command> getCommands() throws InfrastructureException {
      return null;
    }

    @Override
    protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {}

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {}

    @Override
    public Map<String, String> getProperties() {
      return null;
    }
  }

  private static class TestRuntimeContext extends RuntimeContext {

    public TestRuntimeContext(
        InternalEnvironment environment,
        RuntimeIdentity identity,
        RuntimeInfrastructure infrastructure)
        throws ValidationException, InfrastructureException {
      super(environment, identity, infrastructure);
    }

    @Override
    public InternalRuntime getRuntime() throws InfrastructureException {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public URI getOutputChannel() throws InfrastructureException, UnsupportedOperationException {
      throw new RuntimeException("Not implemented");
    }
  }
}
