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
package org.eclipse.che.plugin.debugger.ide;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vitaly Parfonov */
public interface DebuggerResources extends ClientBundle {

    @Source("resume.svg")
    SVGResource resumeExecution();

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
    SVGResource changeVariableValue();

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

    /** Returns the CSS resource for the Debugger extension. */
    @Source({"debugger.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css getCss();

    /** The CssResource interface for the Debugger extension. */
    interface Css extends CssResource {

        String selectConfigurationBox();

        String selectConfigurationsBoxIconPanel();
    }
}
