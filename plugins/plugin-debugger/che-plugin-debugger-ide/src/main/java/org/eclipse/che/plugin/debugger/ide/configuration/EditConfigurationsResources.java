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
package org.eclipse.che.plugin.debugger.ide.configuration;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Contains references to the resources for correct displaying of 'Debug Configurations' dialog.
 *
 * @author Artem Zatsarynnyi
 */
public interface EditConfigurationsResources extends ClientBundle {

  /** Returns the CSS resource for the 'Debug Configurations' dialog. */
  @Source({"editConfigurations.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css getCss();

  @DataResource.MimeType("image/svg+xml")
  @Source("find-icon.svg")
  DataResource findIcon();

  @Source("add-configuration-button.svg")
  SVGResource addConfigurationButton();

  @Source("duplicate-configuration-button.svg")
  SVGResource duplicateConfigurationButton();

  @Source("remove-configuration-button.svg")
  SVGResource removeConfigurationButton();

  interface Css extends CssResource {

    String categoryHeader();

    String categorySubElementHeader();

    String hintLabel();

    String buttonArea();

    String filterPlaceholder();
  }
}
