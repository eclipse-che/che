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
package org.eclipse.che.ide.ui.window;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.inject.ImplementedBy;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Window resource bundle.
 *
 * @author Vlad Zhukovskyi
 * @since 6.0.0
 */
@ImplementedBy(CompositeWindowView.ResourceClientBundle.class)
public interface WindowClientBundle extends ClientBundle {

  Style getStyle();

  SVGResource closeIcon();

  interface Style extends CssResource {
    String windowFrame();

    String windowFrameTitleBar();

    String windowFrameTitle();

    String windowFrameCloseButton();

    String windowFrameBody();

    String windowFrameButtonBar();

    String windowFrameFooterButtonRight();

    String windowFrameFooterButtonLeft();

    String windowFrameFooterButton();

    String windowFrameFooterButtonPrimary();
  }
}
