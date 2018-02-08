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
package org.eclipse.che.api.workspace.server.hc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class ServersCheckerTest {

  private static final String MACHINE_NAME = "mach1";
  private static final String MACHINE_TOKEN = "machineToken";
  private static final String WORKSPACE_ID = "ws123";
  private static final int SERVER_PING_SUCCESS_THRESHOLD = 1;

  @Mock private Consumer<String> readinessHandler;
  @Mock private MachineTokenProvider machineTokenProvider;
  @Mock private HttpConnectionServerChecker connectionChecker;
  @Mock private RuntimeIdentity runtimeIdentity;
  private Map<String, ServerImpl> servers;

  private CompletableFuture<String> compFuture;
  private ServersChecker checker;

  @BeforeMethod
  public void setUp() throws Exception {
    servers = new HashMap<>();
    servers.putAll(
        ImmutableMap.of(
            "wsagent/http", new ServerImpl().withUrl("http://localhost/api"),
            "exec-agent/http", new ServerImpl().withUrl("http://localhost/exec-agent/process"),
            "terminal", new ServerImpl().withUrl("http://localhost/terminal/pty")));

    compFuture = new CompletableFuture<>();

    when(connectionChecker.getReportCompFuture()).thenReturn(compFuture);

    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    checker =
        spy(
            new ServersChecker(
                runtimeIdentity,
                MACHINE_NAME,
                servers,
                machineTokenProvider,
                SERVER_PING_SUCCESS_THRESHOLD));
    when(checker.doCreateChecker(any(URL.class), anyString())).thenReturn(connectionChecker);
    when(machineTokenProvider.getToken(anyString())).thenReturn(MACHINE_TOKEN);
  }

  @AfterMethod(timeOut = 1000)
  public void tearDown() throws Exception {
    try {
      checker.await();
    } catch (Exception ignored) {
    }
  }

  @Test(timeOut = 1000)
  public void shouldUseMachineTokenWhenConstructionUrlToCheck() throws Exception {
    servers.clear();
    servers.put("wsagent/http", new ServerImpl().withUrl("http://localhost"));

    checker.startAsync(readinessHandler);
    connectionChecker.getReportCompFuture().complete("wsagent/http");

    verify(machineTokenProvider).getToken(WORKSPACE_ID);
    ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
    verify(checker).doCreateChecker(urlCaptor.capture(), eq("wsagent/http"));
    URL urlToCheck = urlCaptor.getValue();
    assertNotEquals(urlToCheck.getQuery().indexOf("token=" + MACHINE_TOKEN), -1);
  }

  @Test(timeOut = 1000)
  public void shouldNotifyReadinessHandlerAboutEachServerReadiness() throws Exception {
    checker.startAsync(readinessHandler);

    verify(readinessHandler, after(500).never()).accept(anyString());

    connectionChecker.getReportCompFuture().complete("test_ref");

    verify(readinessHandler, times(3)).accept("test_ref");
  }

  @Test(timeOut = 1000)
  public void shouldThrowExceptionIfAServerIsUnavailable() throws Exception {
    checker.startAsync(readinessHandler);

    connectionChecker
        .getReportCompFuture()
        .completeExceptionally(new InfrastructureException("my exception"));

    try {
      checker.await();
    } catch (InfrastructureException e) {
      assertEquals(e.getMessage(), "my exception");
    }
  }

  @Test(timeOut = 1000)
  public void shouldNotCheckNotHardcodedServers() throws Exception {
    servers.clear();
    servers.putAll(
        ImmutableMap.of(
            "wsagent/http", new ServerImpl().withUrl("http://localhost"),
            "not-hardcoded", new ServerImpl().withUrl("http://localhost")));

    checker.startAsync(readinessHandler);
    connectionChecker.getReportCompFuture().complete("test_ref");
    checker.await();

    verify(readinessHandler).accept("test_ref");
  }

  @Test(timeOut = 1000)
  public void awaitShouldReturnOnFirstUnavailability() throws Exception {
    CompletableFuture<String> future1 = spy(new CompletableFuture<>());
    CompletableFuture<String> future2 = spy(new CompletableFuture<>());
    CompletableFuture<String> future3 = spy(new CompletableFuture<>());
    when(connectionChecker.getReportCompFuture())
        .thenReturn(future1)
        .thenReturn(future2)
        .thenReturn(future3);

    checker.startAsync(readinessHandler);

    future2.completeExceptionally(new InfrastructureException("error"));

    try {
      checker.await();
      fail();
    } catch (InfrastructureException ignored) {
      verify(future1, never()).complete(anyString());
      verify(future2, never()).complete(anyString());
      verify(future3, never()).complete(anyString());
      verify(future1, never()).completeExceptionally(any(Throwable.class));
      verify(future2).completeExceptionally(any(Throwable.class));
      verify(future3, never()).completeExceptionally(any(Throwable.class));
    }
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "oops!"
  )
  public void throwsExceptionIfAnyServerIsNotAvailable() throws InfrastructureException {
    doThrow(new InfrastructureException("oops!")).when(connectionChecker).checkOnce(any());

    checker.checkOnce(ref -> {});
  }
}
