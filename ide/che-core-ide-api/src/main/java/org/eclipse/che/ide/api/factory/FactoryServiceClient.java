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
package org.eclipse.che.ide.api.factory;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.util.Pair;

/**
 * Client for Factory service.
 *
 * @author Vladyslav Zhukovskii
 */
public interface FactoryServiceClient {

  /**
   * Get valid JSON factory object based on input factory ID
   *
   * @param factoryId factory ID to retrieve
   * @param validate indicates whether or not factory should be validated by accept validator
   * @return Factory through a Promise
   */
  Promise<FactoryDto> getFactory(@NotNull String factoryId, boolean validate);

  /**
   * Retrieves factory object prototype for given project with it's attributes. It's not the stored
   * factory object.
   *
   * @param workspaceId workspace id
   * @param path project path
   * @param callback callback which returns snippet of the factory or exception if occurred
   */
  void getFactoryJson(
      @NotNull String workspaceId,
      @NotNull String path,
      @NotNull AsyncRequestCallback<FactoryDto> callback);

  /**
   * Get factory as JSON.
   *
   * @param workspaceId workspace id
   * @param path project path
   * @return a promise that resolves to the {@link FactoryDto}, or rejects with an error
   */
  Promise<FactoryDto> getFactoryJson(@NotNull String workspaceId, @Nullable String path);

  /**
   * Save factory to storage.
   *
   * @param factory factory to save
   * @return a promise that resolves to the {@link FactoryDto}, or rejects with an error
   */
  Promise<FactoryDto> saveFactory(@NotNull FactoryDto factory);

  /**
   * Find factory by given search parameters.
   *
   * @param skipCount the number of the items to skip
   * @param maxItems the limit of the items in the response, default is 30ber of the items to skip
   * @param params parameters to search factory by
   * @return a promise that will provide a list of {@link FactoryDto}s, or rejects with an error
   */
  Promise<List<FactoryDto>> findFactory(
      Integer skipCount, Integer maxItems, List<Pair<String, String>> params);

  /**
   * Updates factory by id
   *
   * @param id factory identifier
   * @param factory update body
   * @return updated factory
   */
  Promise<FactoryDto> updateFactory(String id, FactoryDto factory);

  /**
   * Resolve factory object based on user parameters
   *
   * @param factoryParameters map containing factory data parameters provided through URL
   * @param validate indicates whether or not factory should be validated by accept validator
   * @return Factory through a Promise
   */
  Promise<FactoryDto> resolveFactory(
      @NotNull Map<String, String> factoryParameters, boolean validate);
}
