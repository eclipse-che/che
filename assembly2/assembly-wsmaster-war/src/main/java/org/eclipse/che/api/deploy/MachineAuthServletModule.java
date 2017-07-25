package org.eclipse.che.api.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.machine.authentication.server.MachineLoginFilter;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com).
 */
@DynaModule
public class MachineAuthServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter("/*").through(MachineLoginFilter.class);
    }
}
