/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
