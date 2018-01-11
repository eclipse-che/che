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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Nikolay Zamosenchuk */
public interface PartStackUIResources extends ClientBundle {

  interface PartStackCss extends CssResource {

    @ClassName("ide-PartStack-Tab-Line-Warning")
    String lineWarning();

    @ClassName("ide-PartStack-Tab-Line-Error")
    String lineError();

    String listItemPanel();
  }

  @Source({"partstack.css", "org/eclipse/che/ide/api/ui/style.css"})
  PartStackCss partStackCss();

  @Source("collapse-expand-icon.svg")
  SVGResource collapseExpandIcon();

  @Source("arrow-bottom.svg")
  SVGResource arrowBottom();

  @Source("erase.svg")
  SVGResource erase();

  @Source("close-icon.svg")
  SVGResource closeIcon();

  @Source("maximize-part.svg")
  SVGResource maximizePart();
}
