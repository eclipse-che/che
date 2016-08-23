package org.eclipse.che.plugin.embedjsexample.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.embedjsexample.ide.action.HelloWorldAction;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@Extension(title = "Hello world from JavaScript example")
@Singleton
public class HelloWorldViewExampleExtension {

    @Inject
    private void configureActions(final ActionManager actionManager,
                                  final HelloWorldAction helloWorldAction) {

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        DefaultActionGroup jsGroup = new DefaultActionGroup("JavaScript View Example", true, actionManager);
        mainContextMenuGroup.add(jsGroup);

        actionManager.registerAction(helloWorldAction.ACTION_ID, helloWorldAction);
        jsGroup.addAction(helloWorldAction);
    }

}
