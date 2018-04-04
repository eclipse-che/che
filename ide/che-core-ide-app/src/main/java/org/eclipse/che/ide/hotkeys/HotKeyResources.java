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
package org.eclipse.che.ide.hotkeys;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;

/**
 * Resources for KeyBindings widget
 *
 * @author Alexander Andrienko
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
public interface HotKeyResources extends ClientBundle {

  @Source({"HotKeysCss.css", "org/eclipse/che/ide/api/ui/style.css"})
  HotKeyCss css();

  interface HotKeyCss extends CssResource {
    String item();

    String floatRight();

    String hotKey();

    String emptyBorder();

    String blackBorder();

    String filter();

    String categories();

    String description();

    String isGlobal();

    /** Returns the CSS class name for scheme selection text label in 'Key Bindings' form. */
    String selectionLabel();

    /** Returns the CSS class name for scheme selection list box in 'Key Bindings' form. */
    String selectionListBox();

    /** Returns the CSS class name for scheme selection panel in 'Key Bindings' form. */
    String selectionPanel();
  }

  @DataResource.MimeType("image/svg+xml")
  @Source("find-icon.svg")
  DataResource findIcon();
}
