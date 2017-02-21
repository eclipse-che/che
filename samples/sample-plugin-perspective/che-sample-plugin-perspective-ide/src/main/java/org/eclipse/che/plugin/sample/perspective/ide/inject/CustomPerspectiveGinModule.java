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
package org.eclipse.che.plugin.sample.perspective.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.plugin.sample.perspective.ide.NavigationView;
import org.eclipse.che.plugin.sample.perspective.ide.NavigationViewImpl;
import org.eclipse.che.plugin.sample.perspective.ide.SampleView;
import org.eclipse.che.plugin.sample.perspective.ide.SampleViewImpl;
import org.eclipse.che.plugin.sample.perspective.ide.InformationView;
import org.eclipse.che.plugin.sample.perspective.ide.InformationViewImpl;
import org.eclipse.che.plugin.sample.perspective.ide.CustomPerspective;

import static org.eclipse.che.plugin.sample.perspective.ide.CustomPerspective.OPERATIONS_PERSPECTIVE_ID;

/**
 * Gin module binding the {@link NavigationView} to the {@link NavigationViewImpl} implementation class.
 *
 * @author Edgar Mueller
 */
@ExtensionGinModule
public class CustomPerspectiveGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(NavigationView.class).to(NavigationViewImpl.class);
        bind(InformationView.class).to(InformationViewImpl.class);
        bind(SampleView.class).to(SampleViewImpl.class);
        GinMapBinder.newMapBinder(binder(), String.class, Perspective.class)
                    .addBinding(OPERATIONS_PERSPECTIVE_ID)
                    .to(CustomPerspective.class);
    }

}
