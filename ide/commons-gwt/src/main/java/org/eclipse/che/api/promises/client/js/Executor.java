/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.promises.client.js;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The executor is the conclusion callback, a js function with two parameters, usually named resolve
 * and reject. The first argument fulfills the promise, the second argument rejects it.
 *
 * @param <V> the type of the promised value
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class Executor<V> extends JavaScriptObject {

  /** JSO mandated protected constructor. */
  protected Executor() {}

  /**
   * Creates an executor.
   *
   * @param executorBody the body of the executor
   * @param <V> the fulfillment value
   * @return the new executor
   */
  public static final native <V> Executor<V> create(ExecutorBody<V> executorBody) /*-{
        return function (resolve, reject) {
            try {
                executorBody.@org.eclipse.che.api.promises.client.js.Executor.ExecutorBody::apply(*)(resolve, reject);
            } catch (e) {
                reject(e);
            }
        }
    }-*/;

  /**
   * The definition of an executor.
   *
   * @param <V> the type of the fulfillment value
   */
  public interface ExecutorBody<V> {
    /**
     * The executor describes what the promise must do in order to be fulfilled. It will execute
     * some code to process or retrieve some value, then use the {@code resolve} or {@code reject}
     * callback to conclude.
     *
     * @param resolve what to do on success
     * @param reject what to do on failure
     */
    void apply(ResolveFunction<V> resolve, RejectFunction reject);
  }
}
