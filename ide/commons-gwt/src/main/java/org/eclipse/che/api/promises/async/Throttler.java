/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.promises.async;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;

/**
 * Helper class, allows prevent accumulation of sequential async tasks.
 *
 * @author Evgen Vidolob
 */
public class Throttler {
  private Promise current = Promises.resolve(null);

  @SuppressWarnings("unchecked")
  public <T> Promise<T> queue(final Task<Promise<T>> promiseFactory) {
    return current =
        current.thenPromise(
            new Function<Object, Promise>() {
              @Override
              public Promise apply(Object arg) throws FunctionException {
                return promiseFactory.run();
              }
            });
  }
}
