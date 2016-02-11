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
package org.eclipse.che.ide.ext.java.client.settings.property;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * The interface provides methods to control property's widget which contains name of property and list box with all possible values.
 *
 * @author Dmitry Shnurenko
 */
public interface PropertyWidget extends View<PropertyWidget.ActionDelegate> {

    /**
     * Selects need values in list box.
     *
     * @param value
     *         value which will be selected
     */
    void selectPropertyValue(@NotNull String value);

    public interface ActionDelegate {
        /**
         * Performs some action when user change value of property.
         *
         * @param propertyId
         *         property id which was changed
         * @param value
         *         value which was set to property
         */
        void onPropertyChanged(@NotNull String propertyId, @NotNull String value);
    }
}
