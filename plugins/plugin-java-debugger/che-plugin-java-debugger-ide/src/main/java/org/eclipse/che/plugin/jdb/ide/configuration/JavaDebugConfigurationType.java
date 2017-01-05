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
package org.eclipse.che.plugin.jdb.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.jdb.ide.debug.JavaDebugger;
import org.eclipse.che.plugin.jdb.ide.JavaDebuggerResources;

/**
 * Java debug configuration type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class JavaDebugConfigurationType implements DebugConfigurationType {

    public static final String DISPLAY_NAME = "Java";

    private final JavaDebugConfigurationPagePresenter page;

    @Inject
    public JavaDebugConfigurationType(JavaDebugConfigurationPagePresenter page,
                                      IconRegistry iconRegistry,
                                      JavaDebuggerResources resources) {
        this.page = page;
        iconRegistry.registerIcon(new Icon(JavaDebugger.ID + ".debug.configuration.type.icon", resources.javaDebugConfigurationType()));
    }

    @Override
    public String getId() {
        return JavaDebugger.ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage() {
        return page;
    }
}
