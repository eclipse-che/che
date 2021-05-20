/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import java.util.Map;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy;

/**
 * A simple base class for providers that are dependent on the server exposure strategy and
 * single-host workspace exposure.
 *
 * @param <T> the type this provider provides.
 */
public abstract class AbstractExposureStrategyAwareProvider<T> implements Provider<T> {

  protected final T instance;
  protected final Map<WorkspaceExposureType, T> instanceMap;

  /**
   * Constructs a new provider returning one of the instances from the provided mapping
   *
   * <p>If the server exposure strategy is not "single-host", the {@link
   * WorkspaceExposureType#NATIVE native} is used to lookup the instance from the map.
   *
   * <p>If the server exposure strategy is "single-host", the appropriate instance is looked up in
   * the mapping that corresponds to the {@code singleHostType}.
   *
   * @param exposureStrategy the server exposure strategy
   * @param wsExposureType the type of workspace exposure under single-host
   * @param mapping the mapping for the different exposure types
   * @param errorMessageTemplate the template for the error message which should contain a single
   *     '%s' that is going to be replaced by the "wsExposureType" value.
   * @throws IllegalStateException if the mapping doesn't contain a value for the chosen server
   *     strategy and workspace exposure
   */
  protected AbstractExposureStrategyAwareProvider(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String wsExposureType,
      Map<WorkspaceExposureType, T> mapping,
      String errorMessageTemplate) {
    if (exposureStrategy.equals(SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY)) {
      instance = mapping.get(WorkspaceExposureType.fromConfigurationValue(wsExposureType));
    } else {
      instance = mapping.get(WorkspaceExposureType.NATIVE);
    }

    if (instance == null) {
      throw new IllegalStateException(String.format(errorMessageTemplate, wsExposureType));
    }

    instanceMap = mapping;
  }

  /** Returns the object mapped to the configured exposure type. */
  public T get() {
    return instance;
  }

  /** Returns the object mapped to the provided exposure type. */
  public T get(WorkspaceExposureType exposureType) {
    return instanceMap.get(exposureType);
  }
}
