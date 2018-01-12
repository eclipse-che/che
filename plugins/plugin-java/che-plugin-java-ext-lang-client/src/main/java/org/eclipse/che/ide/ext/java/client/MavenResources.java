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
package org.eclipse.che.ide.ext.java.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Client resources.
 *
 * @author Ann Shumilova
 */
public interface MavenResources extends ClientBundle {
  @Source("svg/maven.svg")
  SVGResource maven();

  @Source("svg/maven-command-type.svg")
  SVGResource mavenCommandType();

  @Source({"maven.css", "org/eclipse/che/ide/api/ui/style.css"})
  MavenCss css();

  interface MavenCss extends CssResource {

    String editorInfoPanel();

    String downloadLink();

    String editorMessage();
  }
}
