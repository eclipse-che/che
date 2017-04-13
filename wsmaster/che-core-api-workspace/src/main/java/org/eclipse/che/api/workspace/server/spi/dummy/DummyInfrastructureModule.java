package org.eclipse.che.api.workspace.server.spi.dummy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;

public class DummyInfrastructureModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<RuntimeInfrastructure> mb = Multibinder.newSetBinder(binder(), RuntimeInfrastructure.class);
        mb.addBinding().to(DummyRuntimeInfrastructure.class);
    }
}
