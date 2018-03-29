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
package org.eclipse.che.api.core.notification;

import java.io.Serializable;
import java.util.Map;

/**
 * Describes single event subscription with limiting scope.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class RemoteSubscriptionContext implements Serializable {

  private final String endpointId;
  private final Map<String, String> scope;

  RemoteSubscriptionContext(String endpointId, Map<String, String> scope) {
    this.endpointId = endpointId;
    this.scope = scope;
  }

  public String getEndpointId() {
    return endpointId;
  }

  public Map<String, String> getScope() {
    return scope;
  }
}
