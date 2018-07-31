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
