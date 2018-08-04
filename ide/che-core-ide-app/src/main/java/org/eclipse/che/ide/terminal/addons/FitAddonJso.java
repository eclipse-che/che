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
package org.eclipse.che.ide.terminal.addons;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.terminal.TerminalGeometryJso;

/**
 * Wrapper for xterm.js Fit addon. Fit addon uses for calculation terminal size on resize operation.
 *
 * @author Oleksandr Andriienko
 */
public class FitAddonJso extends JavaScriptObject {
  protected FitAddonJso() {}

  public static native FitAddonJso create(JavaScriptObject fitJSO) /*-{
        return fitJSO;
    }-*/;

  public static native TerminalGeometryJso proposeGeometry() /*-{
        return this.proposeGeometry();
    }-*/;
}
