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
package org.eclipse.che.infrastructure.docker.client;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.eclipse.che.infrastructure.docker.client.json.ProgressStatus;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class JsonMessageReaderTest {

  @Test
  public void shouldParseSequenceOfProcessStatusObjects() throws IOException {
    final String src =
        "{\"stream\":\"Step 0 : FROM busybox\\n\"}\n"
            + "{\"status\":\"The image you are pulling has been verified\",\"id\":\"busybox:latest\"}\n";

    final JsonMessageReader<ProgressStatus> reader =
        new JsonMessageReader<>(new ByteArrayInputStream(src.getBytes()), ProgressStatus.class);

    final ProgressStatus status1 = reader.next();
    final ProgressStatus status2 = reader.next();

    assertEquals(status1.getStream(), "Step 0 : FROM busybox\n");
    assertEquals(status2.getStatus(), "The image you are pulling has been verified");
    assertEquals(status2.getId(), "busybox:latest");
    assertNull(reader.next());
  }

  @Test
  public void shouldReturnNullIfJsonIsIncorrect() throws IOException {
    final String src = "not json";

    final JsonMessageReader<ProgressStatus> reader =
        new JsonMessageReader<>(new ByteArrayInputStream(src.getBytes()), ProgressStatus.class);

    assertNull(reader.next());
  }
}
