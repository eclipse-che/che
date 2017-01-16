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
package org.eclipse.che.ide.menu;

import org.eclipse.che.ide.api.mvp.View;


/** Main Menu View */
public interface MainMenuView extends View<MainMenuView.ActionDelegate> {
    /** Needs for delegate some function into MainMenu view. */
    public interface ActionDelegate {
    }

}