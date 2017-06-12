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
