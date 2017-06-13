/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.registry;

import java.util.concurrent.CompletableFuture;

public interface LSOperation<C, R> {

    boolean canDo(C element); 

    CompletableFuture<R> start(C element);

    /**
     * Handle the result of of processing an element.
     * @param result
     * @return whether the result is valid (non-empty, not null)
     */
    boolean handleResult(C element, R result);
}
