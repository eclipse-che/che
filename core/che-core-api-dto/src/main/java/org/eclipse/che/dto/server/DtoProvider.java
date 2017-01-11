/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.dto.server;

import com.google.gson.JsonElement;

/**
 * Provides implementation of DTO interface.
 *
 * @author andrew00x
 */
public interface DtoProvider<DTO> {
    Class<? extends DTO> getImplClass();

    DTO fromJson(String json);

    DTO fromJson(JsonElement json);

    DTO newInstance();

    DTO clone(DTO origin);
}
