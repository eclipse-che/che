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
package org.eclipse.che.api.workspace.server;

import static java.lang.String.format;

import org.eclipse.che.api.core.BadRequestException;

/** Helper class to validate workspace composite keys. */
public class WorkspaceKeyValidator {

  /**
   * Checks that key consists either from workspaceId or username:workspace_name string.
   *
   * @param key key string to validate
   * @throws BadRequestException if validation is failed
   */
  public static void validateKey(String key) throws BadRequestException {
    String[] parts = key.split(":", -1); // -1 is to prevent skipping trailing part
    switch (parts.length) {
      case 1:
        {
          return; // consider it's id
        }
      case 2:
        {
          if (parts[1].isEmpty()) {
            throw new BadRequestException(
                "Wrong composite key format - workspace name required to be set.");
          }
          break;
        }
      default:
        {
          throw new BadRequestException(
              format("Wrong composite key %s. Format should be 'username:workspace_name'. ", key));
        }
    }
  }
}
