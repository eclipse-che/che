/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class ExternalIpURLRewriterTest {
  @Test(dataProvider = "urlRewritingTestProvider")
  public void shouldRewriteURL(String externalIP, String incomeURL, String expectedURL)
      throws Exception {
    ExternalIpURLRewriter rewriter = new ExternalIpURLRewriter(externalIP);

    String rewrittenURL = rewriter.rewriteURL(null, null, null, incomeURL);

    assertEquals(rewrittenURL, expectedURL);
  }

  @DataProvider(name = "urlRewritingTestProvider")
  public static Object[][] urlRewritingTestProvider() {
    return new Object[][] {
      {"localhost", "http://127.0.0.1:8080/path", "http://localhost:8080/path"},
      {"127.0.0.1", "http://127.0.0.1:8080/path", "http://127.0.0.1:8080/path"},
      {
        "127.0.0.1", "wss://google.com:8080/some/path?param", "wss://127.0.0.1:8080/some/path?param"
      },
      {
        "www.some.host",
        "tcp://google.com:8080/some/path?param=value",
        "tcp://www.some.host:8080/some/path?param=value"
      },
      {"google.com", "http://127.0.0.1:8080/path", "http://google.com:8080/path"},
      {"178.19.20.12", "http://127.0.0.1:8080/path", "http://178.19.20.12:8080/path"},
    };
  }

  @Test
  public void shouldNotRewriteURLIfExternalIpIsNoConfigured() throws Exception {
    String toRewrite = "https://google.com:8080/some/path?param=value";
    ExternalIpURLRewriter rewriter = new ExternalIpURLRewriter(null);

    String rewrittenURL = rewriter.rewriteURL(null, null, null, toRewrite);

    assertEquals(rewrittenURL, toRewrite);
  }

  @Test(
    expectedExceptions = InternalInfrastructureException.class,
    expectedExceptionsMessageRegExp = "Rewriting of host 'localhost' in URL ':' failed. Error: .*"
  )
  public void shouldThrowExceptionWhenRewritingFails() throws Exception {
    String toRewrite = ":";
    ExternalIpURLRewriter rewriter = new ExternalIpURLRewriter("localhost");

    rewriter.rewriteURL(null, null, null, toRewrite);
  }
}
