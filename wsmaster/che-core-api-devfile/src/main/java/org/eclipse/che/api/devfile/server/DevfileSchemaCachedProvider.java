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
package org.eclipse.che.api.devfile.server;

import static org.eclipse.che.api.devfile.server.Constants.SCHEMA_LOCATION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;

/** Loads a schema content and stores it in soft reference. */
@Singleton
public class DevfileSchemaCachedProvider {

  private SoftReference<String> schemaRef;

  public DevfileSchemaCachedProvider() throws IOException {
    this.schemaRef = new SoftReference<>(loadFile());
  }

  public String getSchemaContent() throws ServerException {
    String schemaStream = schemaRef.get();
    if (schemaStream == null) {
      try {
        schemaStream = loadFile();
      } catch (IOException e) {
        throw new ServerException(e);
      }
      schemaRef = new SoftReference<>(schemaStream);
    }
    return schemaStream;
  }

  private String loadFile() throws IOException {

    try (InputStream schemaStream =
        getClass().getClassLoader().getResourceAsStream(SCHEMA_LOCATION)) {
      return new BufferedReader(new InputStreamReader(schemaStream))
          .lines()
          .parallel()
          .collect(Collectors.joining("\n"));
    }
  }
}
