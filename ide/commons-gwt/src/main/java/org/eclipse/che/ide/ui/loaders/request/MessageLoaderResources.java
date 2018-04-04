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
package org.eclipse.che.ide.ui.loaders.request;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Resources for request loader.
 *
 * @author Andrey Plotnikov
 * @author Oleksii Orel
 */
public interface MessageLoaderResources extends ClientBundle {

  interface LoaderCss extends CssResource {
    String loader();

    String loaderSvg();

    String label();

    String glass();
  }

  @Source("progress.svg")
  SVGResource loader();

  @Source({"RequestLoader.css", "org/eclipse/che/ide/api/ui/style.css"})
  LoaderCss Css();
}
