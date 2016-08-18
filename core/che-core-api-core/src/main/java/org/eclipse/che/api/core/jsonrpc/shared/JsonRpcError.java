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
package org.eclipse.che.api.core.jsonrpc.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * When a rpc call encounters an error, the Response Object MUST contain the error member
 * with a value that is a Object with the following members:
 *
 * <ul>
 * <li>code</li>
 * A Number that indicates the error type that occurred. This MUST be an integer.
 * <li>message</li>
 * A String providing a short description of the error. The message SHOULD be limited to a
 * concise single sentence.
 * <li>data</li>
 * A Primitive or Structured value that contains additional information about the error.
 * This may be omitted. The value of this member is defined by the Server (e.g. detailed
 * error information, nested errors etc.).
 * </ul>
 *
 * @author Dmitry Kuleshov
 * @see <a href="http://www.jsonrpc.org/specification#error_object">Error Object</a>
 */
@DTO
public interface JsonRpcError {

    Integer getCode();

    String getMessage();

    String getData();

    JsonRpcError withCode(Integer code);

    JsonRpcError withMessage(String message);

    JsonRpcError withData(String data);
}
