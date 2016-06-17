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
package org.eclipse.che.ide.api.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * A handler for receiving {@link NodeChangedEvent}s.
 *
 * @author Artem Zatsarynnyi
 * @deprecated tree performs renaming data objects in nodes by self,
 * so this event is useless after new tree implementation
 */
@Deprecated
public interface NodeChangedHandler extends EventHandler {

    /**
     * Invoked when a node was renamed.
     *
     * @param event
     *         an event specifying the details about the renamed node
     */
    void onNodeRenamed(NodeChangedEvent event);
}