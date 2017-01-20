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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.api.mvp.Presenter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class ProcessesListPresenter implements Presenter {

    private ProcessesListView view;

    @Inject
    public ProcessesListPresenter(ProcessesListView view) {
        this.view = view;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
