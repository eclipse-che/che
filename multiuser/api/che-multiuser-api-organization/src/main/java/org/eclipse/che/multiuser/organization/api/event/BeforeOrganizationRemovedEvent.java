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
package org.eclipse.che.multiuser.organization.api.event;

import org.eclipse.che.core.db.cascade.event.RemoveEvent;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;

/**
 * Published before {@link OrganizationImpl organization} removed.
 *
 * @author Sergii Leschenko
 */
public class BeforeOrganizationRemovedEvent extends RemoveEvent {

  private final OrganizationImpl organization;

  public BeforeOrganizationRemovedEvent(OrganizationImpl organization) {
    this.organization = organization;
  }

  /** Returns organization which is going to be removed. */
  public OrganizationImpl getOrganization() {
    return organization;
  }
}
