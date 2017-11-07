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
package org.eclipse.che.workspace.infrastructure.openshift.project.pvc;

import static java.lang.String.format;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides implementation of {@link WorkspacePVCStrategy} for configured value.
 *
 * @author Anton Korneta
 */
@Singleton
public class WorkspacePVCStrategyProvider implements Provider<WorkspacePVCStrategy> {

  private final WorkspacePVCStrategy pvcStrategy;

  @Inject
  public WorkspacePVCStrategyProvider(
      @Named("che.infra.openshift.pvc.strategy") String strategy,
      Map<String, WorkspacePVCStrategy> strategies) {
    final WorkspacePVCStrategy pvcStrategy = strategies.get(strategy);
    if (pvcStrategy != null) {
      this.pvcStrategy = pvcStrategy;
    } else {
      throw new IllegalArgumentException(
          format("Unsupported PVC strategy '%s' configured", strategy));
    }
  }

  @Override
  public WorkspacePVCStrategy get() {
    return pvcStrategy;
  }
}
