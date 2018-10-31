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

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes resource which can be accessible using machine token. Consists of rest service path
 * with preliminary slash and set of method names.
 *
 * @author Max Shaposhnyk
 */
public class MachineAuthenticatedResource {

  private final String servicePath;

  private final Set<String> methodNames;

  public MachineAuthenticatedResource(String servicePath, String... methodNames) {
    this.servicePath = servicePath;
    this.methodNames = new HashSet(asList(methodNames));
  }

  public String getServicePath() {
    return servicePath;
  }

  public Set<String> getMethodNames() {
    return methodNames;
  }
}
