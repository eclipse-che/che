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
package org.eclipse.che.api.workspace.server.hc.probe;

import java.util.List;

/**
 * Holds {@link ProbeFactory} instances for probes of a workspace runtime
 *
 * @author Alexander Garagatyi
 */
public class WorkspaceProbes {

  private String workspaceId;
  private List<ProbeFactory> probesFactories;

  public WorkspaceProbes(String workspaceId, List<ProbeFactory> probesFactories) {
    this.workspaceId = workspaceId;
    this.probesFactories = probesFactories;
  }

  public List<ProbeFactory> getProbes() {
    return probesFactories;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }
}
