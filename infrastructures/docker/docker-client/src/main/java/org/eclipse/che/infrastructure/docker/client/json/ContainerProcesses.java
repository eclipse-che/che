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
