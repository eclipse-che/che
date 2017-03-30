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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JavaScript overlay over Orion Service Registry.
 *
 * @author Sven Efftinge
 */
public class OrionServiceRegistryOverlay extends JavaScriptObject {

    protected OrionServiceRegistryOverlay() {}
    /**
     * @name registerService
     * @description Registers a service with this registry. This function will notify clients registered
     * for <code>registered</code> service events.
     * @function
     * @public
     * @param the name of the service being registered
     * @param service The service implementation
     * @param properties A JSON collection of declarative service properties
     * 
     * @see orion.serviceregistry.ServiceEvent
     */
    public final native void doRegisterService(String name, JavaScriptObject service, JavaScriptObject properties) /*-{
        this.registerService(name, service, properties);
    }-*/;
}
