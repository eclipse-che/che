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
package org.eclipse.che.ide.workspace;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import javax.validation.constraints.NotNull;

/**
 * Special factory for creating instances of {@link WorkBenchPartController}. Each call of factory method returns new instance.
 *
 * @author Dmitry Shnurenko
 */
public interface WorkBenchControllerFactory {

    /**
     * Creates special controller using throwing parameters.
     *
     * @param parentPanel
     *         parent panel
     * @param simplePanel
     *         child panel,changes of which should be controlled
     * @return an instance of {@link WorkBenchPartController}
     */
    WorkBenchPartController createController(@NotNull SplitLayoutPanel parentPanel, @NotNull SimplePanel simplePanel);
}
