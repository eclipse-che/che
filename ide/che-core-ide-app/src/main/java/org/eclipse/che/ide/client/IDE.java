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
package org.eclipse.che.ide.client;

import org.eclipse.che.ide.client.inject.IDEInjector;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;


/** The EntryPoint of the IDE application */
public class IDE implements EntryPoint {
    /** This is the entry point method. */
    @Override
    public void onModuleLoad() {
        IDEInjector injector = GWT.create(IDEInjector.class);
        @SuppressWarnings("unused")
        BootstrapController bootstrap = injector.getBootstrapController();
    }
}
