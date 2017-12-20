/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
