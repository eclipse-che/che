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
package org.eclipse.che.plugin.debugger.ide;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vitaly Parfonov */
public interface DebuggerResources extends ClientBundle {

  @Source("resume.svg")
  SVGResource resumeExecution();

  @Source("runtocursor.svg")
  SVGResource runToCursor();

  @Source("connect.svg")
  SVGResource connectButton();

  @Source("disconnect.svg")
  SVGResource disconnectDebugger();

  @Source("stepinto.svg")
  SVGResource stepInto();

  @Source("stepover.svg")
  SVGResource stepOver();

  @Source("stepout.svg")
  SVGResource stepOut();

  @Source("debug.svg")
  SVGResource debug();

  @Source("edit.svg")
  SVGResource editDebugNode();

  @Source("evaluate.svg")
  SVGResource evaluateExpression();

  @Source("breakpoint.svg")
  SVGResource breakpoint();

  @Source("remove.svg")
  SVGResource deleteAllBreakpoints();

  @Source("separator.svg")
  SVGResource separator();

  /** Returns the icon for debug configurations list on central toolbar. */
  @Source("debug-icon.svg")
  SVGResource debugIcon();

  @Source("add-watch-expression-button.svg")
  SVGResource addWatchExpressionBtn();

  @Source("remove-watch-expression-button.svg")
  SVGResource removeWatchExpressionBtn();

  @Source("watch-expression-icon.svg")
  SVGResource watchExpressionIcon();

  /** Returns the CSS resource for the Debugger extension. */
  @Source({"debugger.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css getCss();

  /** The CssResource interface for the Debugger extension. */
  interface Css extends CssResource {

    String selectConfigurationBox();

    String selectConfigurationsBoxIconPanel();

    String watchExpressionsPanel();
  }
}
