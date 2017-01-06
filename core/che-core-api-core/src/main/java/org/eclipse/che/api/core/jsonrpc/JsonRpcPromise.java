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
package org.eclipse.che.api.core.jsonrpc;

import java.util.function.BiConsumer;

/**
 * Simple promise like binary consumer holder. First consumer's argument
 * always represents endpoint identifier, while the second can be of
 * arbitrary type and depends on business logic.
 *
 * @param <R>
 *         type of second argument of binary consumer
 */
public class JsonRpcPromise<R> {
    private BiConsumer<String, R> successConsumer;
    private BiConsumer<String, R> failureConsumer;

    BiConsumer<String, R> getSuccessConsumer() {
        return successConsumer;
    }

    BiConsumer<String, R> getFailureConsumer() {
        return failureConsumer;
    }

    public JsonRpcPromise<R> onSuccess(BiConsumer<String, R> successConsumer) {
        this.successConsumer = successConsumer;
        return this;
    }

    public JsonRpcPromise<R> onFailure(BiConsumer<String, R> failureConsumer) {
        this.failureConsumer = failureConsumer;
        return this;
    }
}
