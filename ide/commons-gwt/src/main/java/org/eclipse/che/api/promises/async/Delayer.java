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
package org.eclipse.che.api.promises.async;

import com.google.gwt.user.client.Timer;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;

/**
 * Helper class, allows to delay execution of a task that being requested often.
 *
 * @author Evgen Vidolob
 */
public class Delayer<T> {
    private final int                defaultDelay;
    private       Timer              timer;
    private       Promise<T>         completionPromise;
    private       ResolveFunction<T> resolveFunction;
    private       Task<T>            task;

    public Delayer(int defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public Promise<T> trigger(Task<T> task) {
        return trigger(task, defaultDelay);
    }

    public Promise<T> trigger(Task<T> task, int delay) {
        this.task = task;
        cancelTimer();

        if (completionPromise == null) {
            completionPromise = Promises.create(new Executor.ExecutorBody<T>() {

                @Override
                public void apply(ResolveFunction<T> resolve, RejectFunction reject) {
                    resolveFunction = resolve;
                }
            }).thenPromise(new Function<T, Promise<T>>() {
                @Override
                public Promise<T> apply(T arg) throws FunctionException {
                    completionPromise = null;
                    resolveFunction = null;
                    Task<T> t = Delayer.this.task;
                    Delayer.this.task = null;

                    return Promises.resolve(t.run());
                }
            });
        }

        timer = new Timer() {
            @Override
            public void run() {
                timer = null;
                resolveFunction.apply(null);
            }
        };
        timer.schedule(delay);
        return completionPromise;

    }

    public void cancel() {
        cancelTimer();
        completionPromise = null;
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

}
