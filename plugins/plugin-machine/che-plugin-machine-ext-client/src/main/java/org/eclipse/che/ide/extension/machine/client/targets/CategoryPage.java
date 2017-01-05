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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Page allows to configure specific target parameters.
 *
 * @author Oleksii Orel
 */
public interface CategoryPage extends Presenter {

    /**
     * This method is called every time when user selects a target.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    void go(final AcceptsOneWidget container);

    /** Returns the target category. */
    String getCategory();

    /** Returns the target manager. */
    TargetManager getTargetManager();

    /**
     * Sets targets tree manager.
     *
     * @param targetsTreeManager
     *            manager for all targets tree
     */
    void setTargetsTreeManager(TargetsTreeManager targetsTreeManager);

    /**
     * Sets current selection.
     *
     * @param target
     *            selected target
     */
    void setCurrentSelection(Target target);
}
