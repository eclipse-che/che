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
package org.eclipse.che.api.workspace.server.hc.probe;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.workspace.server.WorkspaceServiceTermination;

/**
 * Terminates {@link ProbeScheduler}.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class ProbeSchedulerTermination implements ServiceTermination {
  public static final String SERVICE_NAME = "ProbeScheduler";

  private final ProbeScheduler probeScheduler;

  @Inject
  public ProbeSchedulerTermination(ProbeScheduler probeScheduler) {
    this.probeScheduler = probeScheduler;
  }

  @Override
  public void terminate() {
    probeScheduler.shutdown();
  }

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  public Set<String> getDependencies() {
    return ImmutableSet.of(WorkspaceServiceTermination.SERVICE_NAME);
  }
}
