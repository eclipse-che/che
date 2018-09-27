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
package org.eclipse.che.multiuser.machine.authentication.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MachineAuthenticatedResource {

  private final String servicePath;

  private final Set<String> methodPaths;

  public MachineAuthenticatedResource(String servicePath, List<String> methodPaths) {
    this.servicePath = servicePath;
    this.methodPaths = new HashSet<>(methodPaths);
  }

  public String getServicePath() {
    return servicePath;
  }

  public Set<String> getMethodPaths() {
    return methodPaths;
  }
}
