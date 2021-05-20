/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.system.server;

import static com.jayway.restassured.RestAssured.expect;
import static org.testng.Assert.*;

import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.commons.lang.ZipUtils;
import org.everrest.assured.EverrestJetty;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(EverrestJetty.class)
public class JvmServiceTest {

  JvmService service = new JvmService(new JvmManager());

  @Test
  public void testThreadDump() {
    final Response response =
        expect().statusCode(200).contentType(MediaType.TEXT_PLAIN).when().get("/jvm/dump/thread");
    assertTrue(response.body().asString().contains("Dump of"));
    assertTrue(response.body().asString().contains("\"main\" prio="));
    assertTrue(response.body().asString().contains("\"main\" prio="));
    assertTrue(response.body().asString().contains("Non-daemon threads"));
    assertTrue(response.body().asString().contains("Blocked threads"));
  }

  @Test
  public void testHeapDump() throws IOException {
    final Response response =
        expect().statusCode(200).contentType("application/zip").when().get("/jvm/dump/heap");

    byte[] array = response.asByteArray();
    File tmp = File.createTempFile("test", "zip");
    tmp.deleteOnExit();
    Files.write(tmp.toPath(), array);
    assertTrue(ZipUtils.isZipFile(tmp));
    assertTrue(array.length > 1000);
  }
}
