/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.factory;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Client for Factory service.
 *
 * @author Vladyslav Zhukovskii
 */
public interface FactoryServiceClient {

    /**
     * Get valid JSON factory object based on input factory ID
     *
     * @param factoryId
     *         factory ID to retrieve
     * @param validate
     *         indicates whether or not factory should be validated by accept validator
     * @return Factory through a Promise
     */
    Promise<Factory> getFactory(@NotNull String factoryId, boolean validate);

    /**
     * @param factoryId
     *         Factory's id
     * @param type
     *         snippet's type (markdown, html, etc)
     * @param callback
     *         callback which returns snippet of the factory or exception if occurred
     */
    void getFactorySnippet(@NotNull String factoryId, @NotNull String type, @NotNull AsyncRequestCallback<String> callback);

    /**
     * Retrieves factory object prototype for given project with it's attributes. It's not the stored factory object.
     *
     * @param workspaceId
     *         workspace id
     * @param path
     *         project path
     * @param callback
     *         callback which returns snippet of the factory or exception if occurred
     */
    void getFactoryJson(@NotNull String workspaceId, @NotNull String path, @NotNull AsyncRequestCallback<Factory> callback);

    /**
     * Get factory as JSON.
     *
     * @param workspaceId
     *         workspace id
     * @param path
     *         project path
     * @return a promise that resolves to the {@link Factory}, or rejects with an error
     */
    Promise<Factory> getFactoryJson(@NotNull String workspaceId, @Nullable String path);

    /**
     * Save factory to storage.
     *
     * @param factory
     *         factory to save
     * @return a promise that resolves to the {@link Factory}, or rejects with an error
     */
    Promise<Factory> saveFactory(@NotNull Factory factory);

    /**
     * Save factory to storage.
     *
     * @param skipCount
     *         the number of the items to skip
     * @param maxItems
     *         the limit of the items in the response, default is 30
     * @return a promise that will provide a list of {@link Factory}s, or rejects with an error
     */
    Promise<List<Factory>> findFactory(Integer skipCount, Integer maxItems, List<Pair<String, String>> params);

    /**
     * Updates factory by id
     *
     * @param id
     *         factory identifier
     * @param factory
     *         update body
     * @return updated factory
     */
    Promise<Factory> updateFactory(String id, Factory factory);


    /**
     * Resolve factory object based on user parameters
     *
     * @param factoryParameters
     *         map containing factory data parameters provided through URL
     * @param validate
     *         indicates whether or not factory should be validated by accept validator
     * @return Factory through a Promise
     */
    Promise<Factory> resolveFactory(@NotNull Map<String, String> factoryParameters, boolean validate);

}
