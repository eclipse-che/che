/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.ResponseMessage;

@DTO
public interface ResponseMessageDTO extends ResponseMessage {
    /**
     * The request id.
     * 
     */
    public abstract void setId(final String id);

    /**
     * The result of a request. This can be omitted in the case of an error.
     * 
     */
    public abstract void setResult(final Object result);

    /**
     * The error object in case a request fails. Overridden to return the DTO
     * type.
     * 
     */
    public abstract ResponseErrorDTO getError();

    /**
     * The error object in case a request fails.
     * 
     */
    public abstract void setError(final ResponseErrorDTO error);

    public abstract void setJsonrpc(final String jsonrpc);
}
