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
package org.eclipse.che.ide.ui.listbox;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Oleksii Orel */
public interface CustomListBoxResources extends ClientBundle {
  @Source("arrow.svg")
  SVGResource arrow();

  @Source({"ListBox.css", "org/eclipse/che/ide/api/ui/style.css"})
  CSS getCSS();

  interface CSS extends CssResource {
    String listBox();
  }
}
