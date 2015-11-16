#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

@ExtensionGinModule
public class ${yourPrefix}GinModule extends AbstractGinModule {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(${yourPrefix}Wizard.class);
    }
}
