/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;

/** Class that helps determine devfile versions. */
@Singleton
public class DevfileVersionDetector {

  private final String DEVFILE_V1_VERSION_FIELD = "apiVersion";
  private final String DEVFILE_V2_VERSION_FIELD = "schemaVersion";

  /**
   * Gives exact version of the devfile.
   *
   * @param devfile to inspect
   * @return exact version of the devfile
   * @throws DevfileException when can't find the field with version
   */
  public String devfileVersion(JsonNode devfile) throws DevfileException {
    final String version;
    if (devfile.has(DEVFILE_V1_VERSION_FIELD)) {
      version = devfile.get(DEVFILE_V1_VERSION_FIELD).asText();
    } else if (devfile.has(DEVFILE_V2_VERSION_FIELD)) {
      version = devfile.get(DEVFILE_V2_VERSION_FIELD).asText();
    } else {
      throw new DevfileException(
          "Neither of `apiVersion` or `schemaVersion` found. This is not a valid devfile.");
    }

    return version;
  }

  /**
   * Gives major version of the devfile.
   *
   * <pre>
   *   1 -> 1
   *   1.0.0 -> 1
   *   1.99 -> 1
   *   2.0.0 -> 2
   *   2.1 -> 2
   *   a.a -> DevfileException
   *   a -> DevfileException
   * </pre>
   *
   * @param devfile to inspect
   * @return major version of the devfile
   * @throws DevfileException when can't find the field with version
   */
  public int devfileMajorVersion(JsonNode devfile) throws DevfileException {
    String version = devfileVersion(devfile);

    int dot = version.indexOf(".");
    final String majorVersion = dot > 0 ? version.substring(0, dot) : version;
    try {
      return Integer.parseInt(majorVersion);
    } catch (NumberFormatException nfe) {
      throw new DevfileException(
          "Unable to parse devfile version. This is not a valid devfile.", nfe);
    }
  }
}
