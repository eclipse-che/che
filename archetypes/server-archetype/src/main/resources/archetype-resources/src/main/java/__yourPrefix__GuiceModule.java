#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.inject.DynaModule;

import com.google.inject.AbstractModule;

@DynaModule
public class ${yourPrefix}GuiceModule extends AbstractModule {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(${yourPrefix}Service.class);
    }
}
