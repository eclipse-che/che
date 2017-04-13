package org.eclipse.che.api.workspace.server.spi.dummy;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;

import java.util.Collections;

@Singleton
public class DummyRuntimeInfrastructure extends RuntimeInfrastructure {

    public DummyRuntimeInfrastructure() {
        super("dummy", Collections.singletonList("dummy"));
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException, InfrastructureException {
        return null;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException, InfrastructureException {
        return null;
    }
}
