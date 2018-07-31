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
package org.eclipse.che.ide.factory;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.eclipse.che.ide.ui.Styles;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Factory extension resources (css styles, images).
 *
 * @author Ann Shumilova
 * @author Anton Korneta
 */
public interface FactoryResources extends ClientBundle {
  interface FactoryCSS extends CssResource, Styles {
    String label();

    String createFactoryButton();

    String labelErrorPosition();
  }

  interface Style extends CssResource {
    String launchIcon();

    String configureIcon();
  }

  @Source({
    "Factory.css",
    "org/eclipse/che/ide/api/ui/style.css",
    "org/eclipse/che/ide/ui/Styles.css"
  })
  FactoryCSS factoryCSS();

  @Source("export-config.svg")
  SVGResource exportConfig();

  @Source("import-config.svg")
  SVGResource importConfig();

  @Source("execute.svg")
  SVGResource execute();

  @Source("cog-icon.svg")
  SVGResource configure();
}
