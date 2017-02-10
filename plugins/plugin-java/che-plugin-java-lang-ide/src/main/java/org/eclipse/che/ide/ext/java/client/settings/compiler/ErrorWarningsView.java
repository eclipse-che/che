/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget;

import javax.validation.constraints.NotNull;

/**
 * Provides methods to control panel of properties.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ErrorWarningsViewImpl.class)
public interface ErrorWarningsView extends IsWidget {

    /**
     * Adds special property widget on special panel on view.
     *
     * @param propertyWidget
     *         widget which will be added
     */
    void addProperty(@NotNull PropertyWidget propertyWidget);
}
