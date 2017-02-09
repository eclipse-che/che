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
package org.eclipse.che.ide.ext.java.client.navigation.paraminfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;

import java.util.List;

/**
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ParametersHintsViewImpl.class)
public interface ParametersHintsView extends IsWidget {

    /**
     * Shows popup which contains parameters hints for particular method. Each group of parameters are displayed in separated widget.
     *
     * @param parameters
     *         parameters which will be displayed
     * @param x
     *         x coordinate of popup
     * @param y
     *         y coordinate of popup
     */
    void show(List<MethodParameters> parameters, int x, int y);
}
