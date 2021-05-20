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
package org.eclipse.che.api.core.model.workspace.devfile;

/** Defines the metadata of a devfile. */
public interface Metadata {

  /** @return the name of the devfile */
  String getName();

  /** 'generateName' is used as a base string for generated name, when 'name' is not defined. */
  String getGenerateName();
}
