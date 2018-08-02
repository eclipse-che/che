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
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.Arrays;

/** @author andrew00x */
public class ContainerCreated {
  private String id;
  private String[] warnings;

  public ContainerCreated() {}

  public ContainerCreated(String id, String[] warnings) {
    this.id = id;
    this.warnings = warnings;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getWarnings() {
    return warnings;
  }

  public void setWarnings(String[] warnings) {
    this.warnings = warnings;
  }

  @Override
  public String toString() {
    return "ContainerCreated{"
        + "id='"
        + id
        + '\''
        + ", warnings="
        + Arrays.toString(warnings)
        + '}';
  }
}
