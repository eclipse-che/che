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
package org.eclipse.che.workspace.infrastructure.docker.server.mapping;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class SinglePortUrlBuilderTest {

  @Test(dataProvider = "hostnameBuilderTestProvider")
  public void shouldRewriteURL(
      String externalIP,
      String internalIp,
      String machineName,
      String serverName,
      String workspaceId,
      String wildcardHost,
      String expectedHost)
      throws Exception {
    SinglePortHostnameBuilder builder =
        new SinglePortHostnameBuilder(externalIP, internalIp, wildcardHost);

    String host = builder.build(serverName, machineName, workspaceId);

    assertEquals(host, expectedHost);
  }

  @DataProvider(name = "hostnameBuilderTestProvider")
  public static Object[][] urlRewritingTestProvider() {
    return new Object[][] {
      // External IP
      {
        "172.12.0.2",
        "127.0.0.1",
        "machine1",
        "exec/http",
        "ws123",
        "nip.io",
        "exec-http-machine1-ws123.172.12.0.2.nip.io"
      },
      // Wildcard host, but not nip.io or xip.io
      {
        "172.12.0.2",
        "127.0.0.1",
        "machine1",
        "exec/http",
        "ws123",
        "my.io",
        "exec-http-machine1-ws123.my.io"
      },
      // Normalizing of hostname parts
      {
        "172.12.0.2",
        "127.0.0.1",
        "ma#chi$ne%1",
        "%%exec/http::",
        "[ws123]",
        "nip.io",
        "exec-http-ma-chi-ne-1-ws123.172.12.0.2.nip.io"
      },
      // Normalization doesn't influence wildcard domain
      {
        "172.12.0.2",
        "127.0.0.1",
        "machine.1",
        "exec/http",
        "[ws123]",
        "my-domain.io",
        "exec-http-machine-1-ws123.my-domain.io"
      },
      // Internal IP, wildcard
      {
        null,
        "127.0.0.1",
        "machine1",
        "exec/ws",
        "ws123",
        null,
        "exec-ws-machine1-ws123.127.0.0.1.nip.io"
      },
      // Without machine name
      {null, "127.0.0.1", null, "exec/ws", "ws123", null, "exec-ws-ws123.127.0.0.1.nip.io"},
      // Without server
      {null, "127.0.0.1", "machine1", null, "ws123", null, "machine1-ws123.127.0.0.1.nip.io"},
      // Without workspace id
      {null, "127.0.0.1", "machine1", "exec/ws", null, null, "exec-ws-machine1.127.0.0.1.nip.io"}
    };
  }
}
