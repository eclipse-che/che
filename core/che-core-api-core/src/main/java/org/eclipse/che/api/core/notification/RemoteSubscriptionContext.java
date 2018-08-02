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
