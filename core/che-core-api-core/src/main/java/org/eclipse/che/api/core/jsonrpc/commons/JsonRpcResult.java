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
package org.eclipse.che.api.core.jsonrpc.commons;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Represents JSON RPC result object
 */
public class JsonRpcResult {
    private List<?> result;
    private boolean single;

    public JsonRpcResult(Object result) {
        this.result = singletonList(result);
        this.single = true;
    }

    public JsonRpcResult(List<?> result) {
        this.result = result;
        this.single = false;
    }

    public boolean isSingle() {
        return single;
    }

    public List<?> getMany() {
        return result;
    }

    public Object getOne() {
        return result.get(0);
    }
}
