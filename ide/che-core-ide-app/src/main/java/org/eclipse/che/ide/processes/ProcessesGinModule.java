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
package org.eclipse.che.ide.processes;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.processes.actions.ConsoleTreeContextMenuFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelView;
import org.eclipse.che.ide.processes.panel.ProcessesPanelViewImpl;

/**
 * GIN module for configuring process panel.
 */
public class ProcessesGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(ProcessesPanelView.class).to(ProcessesPanelViewImpl.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().build(ConsoleTreeContextMenuFactory.class));
    }
}
