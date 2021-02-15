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

import static org.eclipse.che.api.workspace.server.devfile.Constants.SCHEMAS_LOCATION;
import static org.eclipse.che.api.workspace.server.devfile.Constants.SCHEMA_FILENAME;
import static org.eclipse.che.api.workspace.server.devfile.Constants.SUPPORTED_VERSIONS;
import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

/** Loads a schema content and stores it in soft reference. */
@Singleton
public class DevfileSchemaProvider {

  private final Map<String, SoftReference<String>> schemas = new HashMap<>();

  public String getSchemaContent(String version) throws IOException {
    if (schemas.containsKey(version)) {
      String schema = schemas.get(version).get();
      if (schema != null) {
        return schema;
      } else {
        return loadAndPut(version);
      }
    } else {
      return loadAndPut(version);
    }
  }

  public StringReader getAsReader(String version) throws IOException {
    return new StringReader(getSchemaContent(version));
  }

  private String loadFile(String version) throws IOException {
    try {
      return readAndCloseQuietly(getResource(SCHEMAS_LOCATION + version + "/" + SCHEMA_FILENAME));
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException(
          String.format(
              "Unable to find schema for devfile version '%s'. Supported versions are '%s'.",
              version, SUPPORTED_VERSIONS));
    } catch (IOException ioe) {
      throw new IOException(
          String.format(
              "Unable to load devfile schema with version '%s'. Supported versions are '%s'",
              version, SUPPORTED_VERSIONS),
          ioe);
    }
  }

  private String loadAndPut(String version) throws IOException {
    final String schema = loadFile(version);
    schemas.put(version, new SoftReference<>(schema));
    return schema;
  }
}
