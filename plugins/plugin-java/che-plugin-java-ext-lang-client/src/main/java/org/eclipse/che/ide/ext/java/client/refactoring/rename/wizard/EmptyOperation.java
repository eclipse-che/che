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

package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import static org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.Operation.Status.INITIAL;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;

/** Empty implementation of {@link Operation} */
public class EmptyOperation implements Operation {

  @Override
  public Status getStatus() {
    return INITIAL;
  }

  @Override
  public Promise<Void> perform(CancelOperationHandler handler) {
    return Promises.create((resolve, reject) -> resolve.apply(null));
  }

  @Override
  public Promise<Void> cancel() {
    return Promises.create((resolve, reject) -> resolve.apply(null));
  }
}
