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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.parts.PartStackView;

import javax.validation.constraints.NotNull;

/**
 * Provides methods to control view representation of info container.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@ImplementedBy(MachineApplianceViewImpl.class)
public interface MachineApplianceView extends PartStackView {

    /**
     * Shows tabs container on main view.
     *
     * @param tabContainer
     *         container which need add
     */
    void showContainer(@NotNull IsWidget tabContainer);

    /**
     * Shows special stub when available machines are absent.
     *
     * @param message
     *         message which will be shown on stub panel
     */
    void showStub(String message);

    /**
     * Adds container to main panel which contains all containers.
     *
     * @param tabContainer
     *         container which will be added
     */
    void addContainer(@NotNull IsWidget tabContainer);
}
