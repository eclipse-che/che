/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import zend.com.che.plugin.zdb.ide.ZendDebuggerResources;
import zend.com.che.plugin.zdb.ide.debug.ZendDebugger;

import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

/**
 * Zend debugger configuration type.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDebugConfigurationType implements DebugConfigurationType {

    public static final String DISPLAY_NAME = "PHP - Zend Debugger";
    
    public static final String ATTR_CLIENT_HOST_IP = "client-host-ip";
    public static final String ATTR_DEBUG_PORT = "debug-port";
    public static final String ATTR_BREAK_AT_FIRST_LINE = "break-at-first-line";
    public static final String ATTR_USE_SSL_ENCRYPTION = "use-ssl-encryption";
    
    public static final String DEFAULT_CLIENT_HOST_IP = "127.0.0.1";
    public static final String DEFAULT_DEBUG_PORT = "10137";
    public static final String DEFAULT_BREAK_AT_FIRST_LINE = "true";
    public static final String DEFAULT_USE_SSL_ENCRYPTION = "false";

    private final ZendDebugConfigurationPagePresenter page;

    @Inject
    public ZendDebugConfigurationType(ZendDebugConfigurationPagePresenter page,
                                      IconRegistry iconRegistry,
                                      ZendDebuggerResources resources) {
        this.page = page;
        iconRegistry.registerIcon(new Icon(ZendDebugger.ID + ".debug.configuration.type.icon", resources.zendDebugConfigurationType()));
    }

    @Override
    public String getId() {
        return ZendDebugger.ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage() {
        return page;
    }
    
    public static void setDefaults(DebugConfiguration configuration) {
		configuration.getConnectionProperties().put(ATTR_CLIENT_HOST_IP, DEFAULT_CLIENT_HOST_IP);
		configuration.getConnectionProperties().put(ATTR_DEBUG_PORT, DEFAULT_DEBUG_PORT);
		configuration.getConnectionProperties().put(ATTR_BREAK_AT_FIRST_LINE, DEFAULT_BREAK_AT_FIRST_LINE);
		configuration.getConnectionProperties().put(ATTR_USE_SSL_ENCRYPTION, DEFAULT_USE_SSL_ENCRYPTION);
    }
    
}
