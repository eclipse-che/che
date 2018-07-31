/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
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
   * @description Registers a service with this registry. This function will notify clients
   *     registered for <code>registered</code> service events.
   * @function
   * @public
   * @param the name of the service being registered
   * @param service The service implementation
   * @param properties A JSON collection of declarative service properties
   * @see orion.serviceregistry.ServiceEvent
   */
  public final native void doRegisterService(
      String name, JavaScriptObject service, JavaScriptObject properties) /*-{
        this.registerService(name, service, properties);
    }-*/;
}
