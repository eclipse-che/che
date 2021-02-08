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
package org.eclipse.che.api.workspace.server.hc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.proxy.ProxyAuthenticator;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class HttpConnectionServerCheckerTest {
  private String MACHINE_NAME = "mach1";
  private String SERVER_REF = "ref1";
  private URL SERVER_URL;

  @Mock private Timer timer;
  @Mock private HttpURLConnection conn;

  private HttpConnectionServerChecker checker;

  @BeforeMethod
  public void setUp() throws Exception {
    SERVER_URL = new URL("http://localhost");

    checker =
        spy(
            new HttpConnectionServerChecker(
                SERVER_URL, MACHINE_NAME, SERVER_REF, 1, 10, 1, TimeUnit.SECONDS, timer, null));

    doReturn(conn).when(checker).createConnection(nullable(URL.class));
    when(conn.getResponseCode()).thenReturn(200);
  }

  @BeforeClass
  public void setup() {
    // These variables affect on ProxyAuthenticator's enums HTTP and HTTPS. It's very hard
    // to reinitialize them (internal fields in a private enum).
    // Until they do not affect other tests we can skip the cleanup phase.
    System.setProperty("http.proxyUser", "u1");
    System.setProperty("http.proxyPassword", "p1");
  }

  @Test(dataProvider = "successfulResponseCodeProvider")
  public void shouldConfirmConnectionSuccessIfResponseCodeIsBetween200And400(Integer responseCode)
      throws Exception {
    when(conn.getResponseCode()).thenReturn(responseCode);
    assertTrue(checker.isConnectionSuccessful(conn));
  }

  @DataProvider
  public static Object[][] successfulResponseCodeProvider() {
    return new Object[][] {{200}, {201}, {210}, {301}, {302}, {303}};
  }

  @Test(dataProvider = "nonSuccessfulResponseCodeProvider")
  public void shouldNotConfirmConnectionSuccessIfResponseCodeIsLessThan200Or400OrMore(
      Integer responseCode) throws Exception {
    when(conn.getResponseCode()).thenReturn(responseCode);
    assertFalse(checker.isConnectionSuccessful(conn));
  }

  @DataProvider
  public static Object[][] nonSuccessfulResponseCodeProvider() {
    return new Object[][] {{199}, {400}, {401}, {402}, {403}, {404}, {405}, {409}, {500}};
  }

  @Test
  public void shouldSetProxyAuthenticatorBeforeCreateConnection() throws Exception {

    Assert.assertFalse(isPasswordAuthenticationSet());
    when(checker.createConnection(eq(SERVER_URL)))
        .thenAnswer(
            (Answer<HttpURLConnection>)
                invocation -> {
                  assertTrue(isPasswordAuthenticationSet());
                  return conn;
                });
    checker.isAvailable();
    Assert.assertFalse(isPasswordAuthenticationSet());
  }

  @Test
  public void shouldOpenConnectionToProvidedUrl() throws Exception {
    checker.isAvailable();

    verify(checker).createConnection(eq(SERVER_URL));
  }

  @Test
  public void shouldSetTimeoutsToConnection() throws Exception {
    checker.isAvailable();

    verify(conn).setReadTimeout((int) TimeUnit.SECONDS.toMillis(3));
    verify(conn).setConnectTimeout((int) TimeUnit.SECONDS.toMillis(3));
  }

  @Test
  public void shouldBeAbleToConfirmAvailability() throws Exception {
    assertTrue(checker.isAvailable());
  }

  @Test
  public void shouldBeAbleToRejectAvailability() throws Exception {
    when(conn.getResponseCode()).thenReturn(401);
    assertFalse(checker.isAvailable());
  }

  @Test
  public void shouldRejectAvailabilityInCaseOfExceptionOnResponseCodeRetrieving() throws Exception {
    when(conn.getResponseCode()).thenThrow(new IOException());
    assertFalse(checker.isAvailable());
  }

  @Test
  public void shouldRejectAvailabilityInCaseOfExceptionOnConnectionOpening() throws Exception {
    when(checker.createConnection(nullable(URL.class))).thenThrow(new IOException());
    assertFalse(checker.isAvailable());
  }

  @Test
  public void shouldDisconnectIfAvailable() throws Exception {
    assertTrue(checker.isAvailable());
    verify(conn).disconnect();
  }

  @Test
  public void shouldDisconnectIfNotAvailable() throws Exception {
    when(conn.getResponseCode()).thenReturn(401);
    assertFalse(checker.isAvailable());
    verify(conn).disconnect();
  }

  public boolean isPasswordAuthenticationSet() {

    Authenticator authenticator = Authenticator.getDefault();
    if (authenticator != null && authenticator instanceof ProxyAuthenticator) {
      ProxyAuthenticator proxyAuthenticator = (ProxyAuthenticator) authenticator;

      try {
        Method method =
            ProxyAuthenticator.class.getDeclaredMethod("getPasswordAuthentication", null);
        method.setAccessible(true);

        PasswordAuthentication value =
            (PasswordAuthentication) method.invoke(proxyAuthenticator, null);
        return value != null;
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }
    return false;
  }
}
