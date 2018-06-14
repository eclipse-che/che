/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
