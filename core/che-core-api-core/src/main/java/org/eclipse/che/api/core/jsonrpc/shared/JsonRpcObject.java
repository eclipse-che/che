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
 * Stores JSON RPC request or JSON RPC response instances. Type of the instance
 * is defined by <code>type</code> field, while request/response is stored as a
 * {@link String} representation in a <code>message</code> field.
 *
 * @author Dmitry Kuleshov
 */
@DTO
public interface JsonRpcObject {

    String getType();

    String getMessage();

    JsonRpcObject withType(String type);

    JsonRpcObject withMessage(String message);
}
