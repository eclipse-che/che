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
package org.eclipse.che.plugin.sample.perspective.ide;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.menu.PartMenu;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.PartsComparator;
import org.eclipse.che.ide.part.widgets.TabItemFactory;

/**
 *
 */
@Singleton
public class SamplePresenter extends PartStackPresenter implements ActivePartChangedHandler {

    private final SampleView view;


    @Inject
    public SamplePresenter(EventBus eventBus,
                           PartMenu partMenu,
                           PartsComparator partsComparator,
                           PartStackEventHandler partStackEventHandler,
                           SampleView view,
                           TabItemFactory tabItemFactory) {
        super(eventBus, partMenu, partStackEventHandler, tabItemFactory, partsComparator, view, null);

        this.view = view;
        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }


    /** {@inheritDoc} */
    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
