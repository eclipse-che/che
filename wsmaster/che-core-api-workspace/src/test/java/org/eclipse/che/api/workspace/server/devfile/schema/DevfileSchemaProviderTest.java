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

package org.eclipse.che.api.workspace.server.devfile.schema;

import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import org.testng.annotations.Test;

public class DevfileSchemaProviderTest {

  private final DevfileSchemaProvider devfileSchemaProvider = new DevfileSchemaProvider();

  @Test
  public void shouldGetProperDevfileSchemaContent() throws IOException {
    String content = devfileSchemaProvider.getSchemaContent(CURRENT_API_VERSION);
    assertNotNull(content);
    assertTrue(content.contains("This schema describes the structure of the devfile object"));
  }

  @Test
  public void shouldGetProperDevfileSchemaContentAsReader() throws IOException {
    StringReader contentReader = devfileSchemaProvider.getAsReader(CURRENT_API_VERSION);
    assertNotNull(contentReader);

    StringBuilder contentBuilder = new StringBuilder();
    int c;
    while ((c = contentReader.read()) != -1) {
      contentBuilder.append((char) c);
    }
    assertTrue(
        contentBuilder
            .toString()
            .contains("This schema describes the structure of the devfile object"));
  }

  @Test(expectedExceptions = FileNotFoundException.class)
  public void shouldThrowExceptionWhenInvalidVersionRequested() throws IOException {
    devfileSchemaProvider.getSchemaContent("this_is_clearly_not_a_valid_schema_version");
  }
}
