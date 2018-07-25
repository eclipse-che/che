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
package org.eclipse.che.ide.ui;

import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vitaly Parfonov */
public interface Resources extends com.google.gwt.resources.client.ClientBundle {

  @Source("logo/che-logo.svg")
  SVGResource logo();

  @Source("logo/water-mark-logo.svg")
  SVGResource waterMarkLogo();

  @Source({"Styles.css", "org/eclipse/che/ide/api/ui/style.css"})
  Styles styles();
}
