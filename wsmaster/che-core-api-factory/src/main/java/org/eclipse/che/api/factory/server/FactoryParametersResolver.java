/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;

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
  FactoryMetaDto createFactory(@NotNull Map<String, String> factoryParameters) throws ApiException;
}
