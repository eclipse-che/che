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
package org.eclipse.che.api.factory.server;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;

/**
 * Defines a resolver that will produce factories for some parameters
 *
 * @author Florent Benoit
 */
public interface FactoryParametersResolver {

  /**
   * Resolver acceptance based on the given parameters.
   *
   * @param factoryParameters map of parameters dedicated to factories
   * @return true if it will be accepted by the resolver implementation or false if it is not
   *     accepted
   */
  boolean accept(@NotNull Map<String, String> factoryParameters);

  /**
   * Create factory object based on provided parameters
   *
   * @param factoryParameters map containing factory data parameters provided through URL
   * @throws BadRequestException when data are invalid
   */
  FactoryDto createFactory(@NotNull Map<String, String> factoryParameters)
      throws BadRequestException;
}
