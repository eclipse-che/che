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
package org.eclipse.che.ide.api.factory;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for FactoryAcceptedEvent.
 * You can use this handler in case need to do some action on after accepting factory
 *
 * @author Vitalii Parfonov
 */
public interface FactoryAcceptedHandler extends EventHandler {

    /**
     * Will be called the factory accepted on IDE side.
     * Project already imported, actions performed.
     * @param event
     */
    void onFactoryAccepted(FactoryAcceptedEvent event);
}
