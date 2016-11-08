/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.ResponseError;
import io.typefox.lsapi.ResponseErrorCode;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface ResponseErrorDTO extends ResponseError {
    /**
     * A number indicating the error type that occured.
     */
    void setCode(final ResponseErrorCode code);

    /**
     * A string providing a short decription of the error.
     */
    void setMessage(final String message);

    /**
     * A Primitive or Structured value that contains additional information
     * about the error. Can be omitted.
     */
    void setData(final Object data);
}
