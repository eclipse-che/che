/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.debug;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/** Resources interface for the breakpoints marks. */
public interface BreakpointResources extends ClientBundle {
  @Source({"breakpoint.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css getCss();

  /** The CssResource interface for the breakpoints */
  interface Css extends CssResource {

    /** Returns the CSS class name for active breakpoint mark */
    String active();

    /** Returns the CSS class name for inactive breakpoint mark */
    String inactive();

    /** Returns the CSS class name for condition breakpoint mark */
    String condition();

    /** Returns the CSS class name for disabled breakpoint mark */
    String disabled();

    String breakpoint();
  }
}
