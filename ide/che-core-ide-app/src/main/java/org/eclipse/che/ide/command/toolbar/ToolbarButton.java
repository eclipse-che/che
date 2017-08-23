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
package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FocusWidget;

/** Button for the commands toolbar. */
class ToolbarButton extends FocusWidget {

  private static final Resources RESOURCES;

  ToolbarButton(SafeHtml content) {
    super(Document.get().createDivElement());
    getElement().setInnerSafeHtml(content);
    addStyleName(RESOURCES.css().button());
  }

  public interface Resources extends ClientBundle {
    @Source({"button.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css css();
  }

  public interface Css extends CssResource {
    String button();
  }

  static {
    RESOURCES = GWT.create(Resources.class);
    RESOURCES.css().ensureInjected();
  }
}
