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
package org.eclipse.che.api.workspace.server.wsnext;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;

/** @author Alexander Garagatyi */
public interface InternalEnvironmentConverter {

  /**
   * Converts one {@link InternalEnvironment} to another. Might be useful to improve features
   * compatibility between infrastructures and recipes.
   *
   * <p>May return the same {@link InternalEnvironment} that is passed or convert it into another.
   *
   * @param internalEnvironment environment to convert
   * @return converted environment
   */
  InternalEnvironment convert(InternalEnvironment internalEnvironment)
      throws InfrastructureException;
}
