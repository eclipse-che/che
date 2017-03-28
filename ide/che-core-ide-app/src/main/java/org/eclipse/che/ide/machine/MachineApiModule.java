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
package org.eclipse.che.ide.machine;

import com.google.gwt.inject.client.AbstractGinModule;

import org.eclipse.che.ide.machine.chooser.MachineChooserView;
import org.eclipse.che.ide.machine.chooser.MachineChooserViewImpl;

/**
 * GIN module for configuring Machine API related components.
 *
 * @author Artem Zatsarynnyi
 */
public class MachineApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(MachineChooserView.class).to(MachineChooserViewImpl.class);
    }
}
