package org.eclipse.che.plugin.embedjsexample.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.embedjsexample.ide.view.HelloWorldView;
import org.eclipse.che.plugin.embedjsexample.ide.view.HelloWorldViewImpl;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@ExtensionGinModule
public class HelloWorldViewExampleGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(HelloWorldView.class).to(HelloWorldViewImpl.class);
    }
    
}
