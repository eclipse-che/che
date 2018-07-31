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
package org.eclipse.che.ide.ui.multisplitpanel;

import com.google.gwt.user.client.ui.IsWidget;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Contract for the widget to display on the {@link SubPanel}.
 *
 * @author Artem Zatsarynnyi
 */
public interface WidgetToShow {

  /** Returns widget to display on the panel. */
  IsWidget getWidget();

  /** Returns the text for displaying as corresponding tab title. */
  String getTitle();

  /** Returns the associated icon for displaying on the corresponding tab. */
  SVGResource getIcon();
}
