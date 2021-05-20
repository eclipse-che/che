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
package org.eclipse.che.api.core.model.workspace.devfile;

import java.util.Map;

public interface Endpoint {

  /** Returns the endpoint name. It is mandatory and unique per endpoints set. */
  String getName();

  /** Returns the container port that should be used for endpoint. It is mandatory. */
  Integer getPort();

  /**
   * Returns endpoints attributes. Emtpy map is returned is endpoint does not have attributes. It is
   * optional.
   */
  Map<String, String> getAttributes();
}
