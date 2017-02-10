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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings.MachStrategy;

/**
 * The visual part of Similar name wizard.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SimilarNamesConfigurationViewImpl.class)
interface SimilarNamesConfigurationView extends View<SimilarNamesConfigurationView.ActionDelegate> {
    /** Hide Move panel. */
    void hide();

    MachStrategy getMachStrategy();

    /** Show Similar Names Configuration panel. */
    void show();

    interface ActionDelegate {
    }
}
