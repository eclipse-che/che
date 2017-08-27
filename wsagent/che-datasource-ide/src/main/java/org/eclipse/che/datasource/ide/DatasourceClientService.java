/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.ide;

import com.google.gwt.http.client.RequestException;

import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;

/**
 * Client interface for the datasource plugin server services.
 */
public interface DatasourceClientService {


    void getAvailableDrivers(@NotNull AsyncRequestCallback<String> asyncRequestCallback) throws RequestException;

    String getRestServiceContext();


    void testDatabaseConnectivity(@NotNull DatabaseConfigurationDTO configuration,
                                  @NotNull AsyncRequestCallback<String> asyncRequestCallback) throws RequestException;

    void encryptText(@NotNull String textToEncrypt,
                     @NotNull AsyncRequestCallback<String> asyncRequestCallback) throws RequestException;

}
