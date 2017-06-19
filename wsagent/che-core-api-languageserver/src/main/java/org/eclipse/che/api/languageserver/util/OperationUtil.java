package org.eclipse.che.api.languageserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OperationUtil {
    private final static Logger LOG = LoggerFactory.getLogger(OperationUtil.class);

    /**
     * Execute the given operation on each element of the collection in
     * sequence. Stops as soon as {@link LSOperation#canDo(Object)} returns
     * true.
     * 
     * @param collection
     * @param op
     * @param timeoutMillis
     */
    public static <C, R> void doInSequence(Collection<C> collection, LSOperation<C, R> op, long timeoutMillis) {
        long endTime = System.currentTimeMillis() + timeoutMillis;
        for (C element : collection) {
            if (op.canDo(element)) {
                CompletableFuture<R> future = op.start(element);
                try {
                    R result = future.get(Math.max(endTime - timeoutMillis, 1), TimeUnit.MILLISECONDS);
                    if (op.handleResult(element, result)) {
                        return;
                    }
                } catch (InterruptedException e) {
                    LOG.info("Thread interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LOG.info("Exception occurred in op", e);
                } catch (TimeoutException e) {
                    future.cancel(true);
                }
            }
        }
    }

    /**
     * Executes the given operation in parallel for each element in the
     * collection. Failures in any of the operations are ignored.
     * 
     * @param collection
     * @param op
     * @param timeoutMillis
     */
    public static <C, R> void doInParallel(Collection<C> collection, LSOperation<C, R> op, long timeoutMillis) {
        Object lock = new Object();
        List<CompletableFuture<?>> pendingResponses = new ArrayList<>();

        for (C element : collection) {
            if (op.canDo(element)) {
                CompletableFuture<R> future = op.start(element);
                synchronized (lock) {
                    pendingResponses.add(future);
                    lock.notifyAll();
                }
                future.thenAccept(result -> {
                    synchronized (lock) {
                        if (!future.isCancelled()) {
                            op.handleResult(element, result);
                            pendingResponses.remove(future);
                            lock.notifyAll();
                        }
                    }
                }).exceptionally((t) -> {
                    LOG.info("Exception occurred in request", t);
                    synchronized (lock) {
                        pendingResponses.remove(future);
                        lock.notifyAll();
                    }
                    return null;
                });
            }
        }

        long endTime = System.currentTimeMillis() + 5000;

        try {
            synchronized (lock) {
                while (System.currentTimeMillis() < endTime && pendingResponses.size() > 0) {
                    lock.wait(endTime - System.currentTimeMillis());
                }
            }
        } catch (InterruptedException e) {
            LOG.info("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
        synchronized (lock) {
            for (CompletableFuture<?> pending : new ArrayList<>(pendingResponses)) {
                pending.cancel(true);
            }
            lock.notifyAll();
        }
    }

}
