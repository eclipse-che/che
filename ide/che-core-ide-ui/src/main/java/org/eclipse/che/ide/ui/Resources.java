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
