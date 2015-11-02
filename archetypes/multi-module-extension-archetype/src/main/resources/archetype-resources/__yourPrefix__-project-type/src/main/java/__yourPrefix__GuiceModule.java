#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.inject.DynaModule;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

@DynaModule
public class ${yourPrefix}GuiceModule extends AbstractModule {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        Multibinder<ProjectType> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectType.class);
        projectTypeMultibinder.addBinding().to(${yourPrefix}ProjectType.class);
	Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(${yourPrefix}ProjectGenerator.class);
    }
}
