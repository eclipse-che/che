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
package org.eclipse.che.ide.ui.buttonLoader;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

/**
 * Resources for ButtonLoader.
 *
 * @author Oleksii Orel
 */
public interface ButtonLoaderResources extends ClientBundle {

  interface ButtonLoaderCss extends CssResource {
    String buttonLoader();
  }

  @MimeType("image/png")
  @Source("loader.png")
  DataResource loader();

  @Source({"buttonLoader.css", "org/eclipse/che/ide/api/ui/style.css"})
  ButtonLoaderCss buttonLoaderCss();
}
