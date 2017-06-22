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
package org.eclipse.che.ide.api.event.project;

import com.google.gwt.event.shared.EventHandler;

/**
 * Special handler which is called when selected other project.
 * @deprecated since 4.6.0 replaced by {@link org.eclipse.che.ide.api.event.SelectionChangedHandler}
 *
 * @author Dmitry Shnurenko
 */
@Deprecated
public interface CurrentProjectChangedHandler extends EventHandler {

    /**
     * Performs some actions when user clicks on different project.
     *
     * @param event
     *         contains information about project which was selected
     */
    void onCurrentProjectChanged(CurrentProjectChangedEvent event);
}
