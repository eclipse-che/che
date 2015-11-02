#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;

@Singleton
@Extension(title = "${yourPrefix} Extension", version = "1.0.0")
public class ${yourPrefix}Extension {

    @Inject
    public ${yourPrefix}Extension(${yourPrefix}Resources resources, ActionManager actionManager, ${yourPrefix}Action ${yourPrefix}Action) {

        DefaultActionGroup mainMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_MENU);

        DefaultActionGroup ${yourPrefix}Menu = new DefaultActionGroup("${yourPrefix} Menu", true, actionManager);
        actionManager.registerAction("${yourPrefix}MenuID", ${yourPrefix}Menu);
        mainMenu.add(${yourPrefix}Menu, Constraints.LAST);

        actionManager.registerAction("${yourPrefix}ActionID", ${yourPrefix}Action);
        ${yourPrefix}Menu.add(${yourPrefix}Action);

    }
}
