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
package org.eclipse.che.api.core.model.factory;

/**
 * Defines the contract for the factory restrictions.
 *
 * @author Anton Korneta
 */
public interface Policies {

  /** Restrict access if referer header doesn't match this field */
  String getReferer();

  /** Restrict access for factories used earlier then author supposes */
  Long getSince();

  /** Restrict access for factories used later then author supposes */
  Long getUntil();

  /** Workspace creation strategy */
  String getCreate();
}
