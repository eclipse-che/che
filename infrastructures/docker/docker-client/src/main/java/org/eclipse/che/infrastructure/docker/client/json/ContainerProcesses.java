/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.json;

/** @author andrew00x */
public class ContainerProcesses {
  private String[] titles;
  private String[][] processes;

  public String[] getTitles() {
    return titles;
  }

  public void setTitles(String[] titles) {
    this.titles = titles;
  }

  public String[][] getProcesses() {
    return processes;
  }

  public void setProcesses(String[][] processes) {
    this.processes = processes;
  }
}
