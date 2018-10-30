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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JavaScript overlay over Orion Service Registration.
 *
 * @author Vladyslav Zhukovskyi
 */
public class OrionServiceRegistrationOverlay extends JavaScriptObject {

  protected OrionServiceRegistrationOverlay() {}

  public final native void doUnregister() /*-{
    this.unregister();
  }-*/;
}
