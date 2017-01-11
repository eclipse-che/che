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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;

import javax.validation.constraints.NotNull;

/**
 * Special entity which represent tab and contains special header of tab and it's content.
 *
 * @author Dmitry Shnurenko
 */
public interface Tab {

    /** @return tab's header. */
    @NotNull
    TabHeader getHeader();

    /** @return tab's content. */
    @NotNull
    TabPresenter getContent();

    /** Performs handler when user clicks on tab. */
    void performHandler();

}
