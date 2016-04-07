/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdi.server;

import com.sun.jdi.event.EventSet;

/**
 * Handle events from {@link EventsCollector}.
 *
 * @author andrew00x
 * @see EventsCollector#run()
 */
public interface EventsHandler {
    void handleEvents(EventSet events) throws DebuggerException;
}
