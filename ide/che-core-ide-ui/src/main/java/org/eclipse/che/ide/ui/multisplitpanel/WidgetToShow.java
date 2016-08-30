/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
