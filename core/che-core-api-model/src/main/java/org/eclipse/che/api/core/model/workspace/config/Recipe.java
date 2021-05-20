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
package org.eclipse.che.api.core.model.workspace.config;

/**
 * Describes recipe of workspace environment.
 *
 * @author Alexander Garagatyi
 */
public interface Recipe {
  /** Type of the environment. */
  String getType();

  /** Content type of the environment recipe, e.g. application/x-yaml. */
  String getContentType();

  /** Content of an environment recipe. Content and location fields are mutually exclusive. */
  String getContent();

  /** Location of an environment recipe. Content and location fields are mutually exclusive. */
  String getLocation();
}
