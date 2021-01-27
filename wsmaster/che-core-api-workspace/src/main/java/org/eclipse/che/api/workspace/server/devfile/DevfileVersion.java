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
package org.eclipse.che.api.workspace.server.devfile;

import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;

@Singleton
public class DevfileVersion {
  private final String DEVFILE_V1_VERSION_FIELD = "apiVersion";
  private final String DEVFILE_V2_VERSION_FIELD = "schemaVersion";

  public String devfileVersion(JsonNode devfile) throws DevfileException {
    final String version;
    if (devfile.has(DEVFILE_V1_VERSION_FIELD)) {
      version = devfile.get(DEVFILE_V1_VERSION_FIELD).textValue();
    } else if (devfile.has(DEVFILE_V2_VERSION_FIELD)) {
      version = devfile.get(DEVFILE_V2_VERSION_FIELD).textValue();
    } else {
      throw new DevfileException(
          "Neither of `apiVersion` and `schemaVersion` found. This is not a valid devfile.");
    }

    return version;
  }

  public int devfileMajorVersion(JsonNode devfile) throws DevfileException {
    String version = devfileVersion(devfile);

    String majorVersion = version.substring(0, version.indexOf("."));
    return Integer.parseInt(majorVersion);
  }
}
