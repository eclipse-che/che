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
