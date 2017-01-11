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
package org.eclipse.che.ide.jsonrpc;

/**
 * Specific to JSON RPC exception that is to be raised when
 * any JSON RPC related error is met. According to the spec
 * there should be an error code and an error message.
 */
public class JsonRpcException extends Exception {
    private final int    code;
    private final String id;

    public JsonRpcException(int code, String message) {
        this(code, message, null);
    }

    public JsonRpcException(int code, String message, String id) {
        super(message);
        this.code = code;
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public String getId() {
        return id;
    }
}
