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

import org.eclipse.che.api.promises.client.Promise;

/**
 * Combine the two strategies from {@link Delayer} and {@link Throttler} helpers
 *
 * @author Evgen Vidolob
 */
@SuppressWarnings("unchecked")
public class ThrottledDelayer<T> extends Delayer<Promise<T>> {

  private final Throttler throttler;

  public ThrottledDelayer(int defaultDelay) {
    super(defaultDelay);
    throttler = new Throttler();
  }

  @Override
  public Promise trigger(final Task<Promise<T>> task) {
    return super.trigger(
        new Task<Promise<T>>() {
          @Override
          public Promise<T> run() {
            return throttler.queue(task);
          }
        });
  }
}
