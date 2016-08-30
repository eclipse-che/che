/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import io.typefox.lsapi.ResponseError;
import io.typefox.lsapi.ResponseErrorCode;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ResponseErrorDTO extends ResponseError {
    /**
     * A number indicating the error type that occured.
     * 
     */
    public abstract void setCode(final ResponseErrorCode code);

    /**
     * A string providing a short decription of the error.
     * 
     */
    public abstract void setMessage(final String message);

    /**
     * A Primitive or Structured value that contains additional information
     * about the error. Can be omitted.
     * 
     */
    public abstract void setData(final Object data);
}
