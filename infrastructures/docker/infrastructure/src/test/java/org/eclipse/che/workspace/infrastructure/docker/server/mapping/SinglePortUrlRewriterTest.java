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
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class SinglePortUrlRewriterTest {

  @Test(dataProvider = "urlRewritingTestProvider")
  public void shouldRewriteURL(
      RuntimeIdentity identity,
      String externalIP,
      String internalIp,
      String machineName,
      String serverName,
      String incomeURL,
      String expectedURL)
      throws Exception {
    SinglePortUrlRewriter rewriter = new SinglePortUrlRewriter(externalIP, internalIp);

    String rewrittenURL = rewriter.rewriteURL(identity, machineName, serverName, incomeURL);

    assertEquals(rewrittenURL, expectedURL);
  }

  @DataProvider(name = "urlRewritingTestProvider")
  public static Object[][] urlRewritingTestProvider() {
    return new Object[][] {
      // External IP
      {
        new RuntimeIdentityImpl("ws123", null, null),
        "172.12.0.2",
        "127.0.0.1",
        "machine1",
        "server/some",
        "http://127.0.0.1:8080/path",
        "http://server-server-some.machine1.ws123.172.12.0.2.nip.io:8080/path"
      },
      // Internal IP, protocol, path param
      {
        new RuntimeIdentityImpl("ws123", null, null),
        null,
        "127.0.0.1",
        "machine1",
        "server/some",
        "tcp://127.0.0.1:8080/path?param",
        "tcp://server-server-some.machine1.ws123.127.0.0.1.nip.io:8080/path?param"
      },
      // Without machine name
      {
        new RuntimeIdentityImpl("ws123", null, null),
        null,
        "127.0.0.1",
        null,
        "server/some",
        "tcp://127.0.0.1:8080/path?param",
        "tcp://server-server-some.ws123.127.0.0.1.nip.io:8080/path?param"
      },
      // Without server
      {
        new RuntimeIdentityImpl("ws123", null, null),
        null,
        "127.0.0.1",
        "machine1",
        null,
        "tcp://127.0.0.1:8080/path?param",
        "tcp://machine1.ws123.127.0.0.1.nip.io:8080/path?param"
      },
    };
  }

  @Test(
    expectedExceptions = InternalInfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Rewriting of host 'server-server.machine1.ws123.172.12.0.2.nip.io' in URL ':' failed. Error: .*"
  )
  public void shouldThrowExceptionWhenRewritingFails() throws Exception {
    SinglePortUrlRewriter rewriter = new SinglePortUrlRewriter("172.12.0.2", "127.0.0.1");
    rewriter.rewriteURL(new RuntimeIdentityImpl("ws123", null, null), "machine1", "server", ":");
  }
}
