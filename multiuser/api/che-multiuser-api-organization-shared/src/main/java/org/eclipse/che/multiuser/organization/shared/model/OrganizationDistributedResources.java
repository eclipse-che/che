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
package org.eclipse.che.multiuser.organization.shared.model;

import java.util.List;
import org.eclipse.che.multiuser.resource.model.Resource;

/**
 * Defines resources which are distributed for suborganization by parent organization
 *
 * @author Sergii Leschenko
 */
public interface OrganizationDistributedResources {
  /** Id of organization that owns these distributed resources */
  String getOrganizationId();

  /**
   * Returns resources cap that limit usage of parent organization's resources.
   *
   * <p>Note that suborganization is not limited to use parent organization's resources if resource
   * is not capped.
   */
  List<? extends Resource> getResourcesCap();
}
