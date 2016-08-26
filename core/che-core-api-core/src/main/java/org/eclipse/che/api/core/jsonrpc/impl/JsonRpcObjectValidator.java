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
package org.eclipse.che.api.core.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;

/**
 * It is used to make sure that {@link JsonRpcObject} is a valid entity.
 * Implementation of this interface must be called before any other operation
 * is performed to avoid any kind of inconsistency.
 *
 * @author Dmitry Kuleshov
 */
public interface JsonRpcObjectValidator {
    void validate(JsonRpcObject object);
}
