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
package org.eclipse.che.ide.api.action;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Defines the requirements for an action that gives a {@link Promise} to be performed. The purpose
 * of the returned {@link Promise} is to allow for interested parties to be notified when performing
 * action has completed or rejected.
 *
 * @author Artem Zatsarynnyi
 */
public interface PromisableAction {

  /**
   * Perform action and returns {@link Promise} to notify about action has performed or it has
   * failed to perform.
   *
   * @param event an {@link ActionEvent}
   * @return {@link Promise} that should be fulfilled immediately after action has performed or
   *     rejected if action has failed to perform
   */
  Promise<Void> promise(ActionEvent event);
}
