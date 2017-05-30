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
package org.eclipse.che.ide.api.action;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Defines the requirements for an action that gives a {@link Promise} to be performed.
 * The purpose of the returned {@link Promise} is to allow for interested parties to be
 * notified when performing action has completed or rejected.
 *
 * @author Artem Zatsarynnyi
 */
public interface PromisableAction {

    /**
     * Perform action and returns {@link Promise} to notify about action has performed or it has failed to perform.
     *
     * @param event
     *         an {@link ActionEvent}
     * @return {@link Promise} that should be fulfilled immediately after action has performed
     * or rejected if action has failed to perform
     */
    Promise<Void> promise(ActionEvent event);
}
