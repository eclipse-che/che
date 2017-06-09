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
package org.eclipse.che.ide.status.message;

import java.util.ArrayList;
import java.util.List;

/**
 * Reporter to notify all interested objects about Orion
 * editor status message.
 *
 * @author Alexander Andrienko
 */
public class StatusMessageReporter {

    private List<StatusMessageObserver> observers = new ArrayList<>();

    /**
     * Register {@code observer}.
     *
     * @param observer
     *         object to notify about new editor status message.
     */
    public void registerObserver(StatusMessageObserver observer) {
        observers.add(observer);
    }

    /**
     * Notify all observers about new editor status message.
     *
     * @param message
     *         message about editor status.
     * @param type
     *         message type
     * @param isAccessible
     *         specified for orion attribute, describes screen
     *         reader ability to read new status message.
     */
    public void notifyObservers(String message, String type, boolean isAccessible) {
        for (StatusMessageObserver observer : observers) {
            observer.update(new StatusMessage(message, type, isAccessible));
        }
    }
}
