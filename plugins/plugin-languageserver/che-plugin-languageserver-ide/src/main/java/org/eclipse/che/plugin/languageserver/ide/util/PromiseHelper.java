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
package org.eclipse.che.plugin.languageserver.ide.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Iterator;
import java.util.function.Consumer;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;

@Singleton
public class PromiseHelper {
  private PromiseProvider promiseProvider;

  @Inject
  PromiseHelper(PromiseProvider promiseProvider) {
    this.promiseProvider = promiseProvider;
  }

  public <P, R> Promise<Void> forEach(
      Iterator<P> elements,
      java.util.function.Function<P, Promise<R>> promiseSource,
      Consumer<R> resultConsumer) {
    if (elements.hasNext()) {
      Promise<R> elementPromise = promiseSource.apply(elements.next());
      return elementPromise.thenPromise(
          (R r) -> {
            resultConsumer.accept(r);
            return forEach(elements, promiseSource, resultConsumer);
          });
    } else {
      return promiseProvider.resolve(null);
    }
  }
}
