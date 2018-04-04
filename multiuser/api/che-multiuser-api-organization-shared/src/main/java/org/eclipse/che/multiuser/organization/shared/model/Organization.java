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

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Describes group of users that can use common resources
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface Organization {

  /**
   * Returns the identifier of the organization (e.g. "organization0x1234567890"). The identifier
   * value is unique and mandatory.
   */
  String getId();

  /**
   * Returns name of organization. The name is mandatory and updatable. The name is unique per
   * parent organization.
   */
  String getName();

  /**
   * Returns the qualified name that includes all parent's names and the name of current
   * organization separated by '/' symbol e.g. "parentOrgName/subOrgName/subSubOrgName". The
   * qualified name is unique.
   */
  String getQualifiedName();

  /**
   * Returns id of parent organization. The returned value can be nullable in case when organization
   * is root
   */
  @Nullable
  String getParent();
}
