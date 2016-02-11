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
package org.eclipse.che.ide.ext.java.jdi.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vitaly Parfonov */
public interface JavaRuntimeResources extends ClientBundle {

    @Source("resume.svg")
    SVGResource resumeButton();

    @Source("connect.svg")
    SVGResource connectButton();

    @Source("disconnect.svg")
    SVGResource disconnectButton();

    @Source("stepinto.svg")
    SVGResource stepIntoButton();

    @Source("stepover.svg")
    SVGResource stepOverButton();

    @Source("stepreturn.svg")
    SVGResource stepReturnButton();

    @Source("debug.svg")
    SVGResource debug();

    @Source("edit.svg")
    SVGResource changeVariableValue();

    @Source("evaluate.svg")
    SVGResource evaluate();

    @Source("breakpoint.svg")
    SVGResource breakpoint();

    @Source("remove.svg")
    SVGResource removeAllBreakpointsButton();

    @Source("separator.svg")
    SVGResource separator();

}
