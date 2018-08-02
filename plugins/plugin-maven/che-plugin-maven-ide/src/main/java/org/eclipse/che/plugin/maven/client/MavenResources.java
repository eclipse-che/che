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
package org.eclipse.che.plugin.maven.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Client resources.
 *
 * @author Ann Shumilova
 */
public interface MavenResources extends ClientBundle {
  @Source("maven.svg")
  SVGResource maven();

  @Source("command/maven-command-type.svg")
  SVGResource mavenCommandType();

  @Source({"Maven.css", "org/eclipse/che/ide/api/ui/style.css"})
  MavenCss css();

  interface MavenCss extends CssResource {

    String editorInfoPanel();

    String downloadLink();

    String editorMessage();
  }
}
