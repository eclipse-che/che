package org.eclipse.che.plugin.nativeaccessexample.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.nativeaccessexample.ide.action.RunNativeCommandAction;

/**
 * Native access example extension which registers exactly one sample action.
 *
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@Extension(title = "Native access example")
@Singleton
public class NativeAccessExampleExtension {

    @Inject
    private void configureActions(final ActionManager actionManager,
                                  final RunNativeCommandAction runNativenCommandAction) {

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        DefaultActionGroup naGroup = new DefaultActionGroup("Native Access Example", true, actionManager);
        mainContextMenuGroup.add(naGroup);

        actionManager.registerAction(runNativenCommandAction.ACTION_ID, runNativenCommandAction);
        naGroup.addAction(runNativenCommandAction);
    }

}
