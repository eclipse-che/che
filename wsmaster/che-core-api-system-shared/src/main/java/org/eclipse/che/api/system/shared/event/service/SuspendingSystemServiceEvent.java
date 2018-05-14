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
package org.eclipse.che.api.system.shared.event.service;

import org.eclipse.che.api.system.shared.event.EventType;

/**
 * See {@link EventType#SUSPENDING_SERVICE} description.
 *
 * @author Max Shaposhnyk
 */
public class SuspendingSystemServiceEvent extends SystemServiceEvent {

  public SuspendingSystemServiceEvent(String serviceName) {
    super(serviceName);
  }

  @Override
  public EventType getType() {
    return EventType.SUSPENDING_SERVICE;
  }
}
