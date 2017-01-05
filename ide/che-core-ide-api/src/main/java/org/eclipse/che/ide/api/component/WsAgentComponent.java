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
package org.eclipse.che.ide.api.component;

import com.google.gwt.core.client.Callback;

/**
 * Defines the requirements for a component that have to be started after starting ws-agent.
 * <p>Component should be registered via GIN MapBinder:
 * <pre>
 *   GinMapBinder&lt;String, WsAgentComponent&gt; mapBinder =
 *       GinMapBinder.newMapBinder(binder(), String.class, WsAgentComponent.class);
 *   mapBinder.addBinding("component key").to(YourComponent.class);
 * </pre>
 * <b>Note:</b> Ws Agent Component start order will be calculated upon component key.
 * All keys will be sorted in alphabet order.
 *
 * @author Artem Zatsarynnyi
 */
public interface WsAgentComponent {

    /**
     * Starts component. Must do not throw any exceptions.
     * <p>All exceptions must be passed in {@code Callback.onFailure(Exception)}
     */
    void start(Callback<WsAgentComponent, Exception> callback);
}
