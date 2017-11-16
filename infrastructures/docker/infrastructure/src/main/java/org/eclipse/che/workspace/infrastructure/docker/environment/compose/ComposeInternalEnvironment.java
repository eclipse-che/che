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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class ComposeInternalEnvironment extends InternalEnvironment {

  private final DockerEnvironment composeEnvironment;

  public ComposeInternalEnvironment(
      Map<String, InternalMachineConfig> machines,
      InternalRecipe recipe,
      List<Warning> warnings,
      DockerEnvironment composeEnvironment)
      throws ValidationException {
    super(machines, recipe, warnings);
    this.composeEnvironment = composeEnvironment;
  }

  public DockerEnvironment getComposeEnvironment() {
    return composeEnvironment;
  }
}
