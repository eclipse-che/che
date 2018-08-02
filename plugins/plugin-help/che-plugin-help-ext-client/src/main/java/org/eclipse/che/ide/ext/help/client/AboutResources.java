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
package org.eclipse.che.ide.ext.help.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

public interface AboutResources extends ClientBundle {

  @Source("actions/about.svg")
  SVGResource about();

  @Source("actions/support.svg")
  SVGResource getSupport();

  @Source({"org/eclipse/che/ide/api/ui/style.css", "About.css"})
  AboutCss aboutCss();

  interface AboutCss extends CssResource {
    String emptyBorder();

    String label();

    String spacing();

    String value();

    String mainText();

    String logo();

    String debugSummaryTextArea();
  }
}
