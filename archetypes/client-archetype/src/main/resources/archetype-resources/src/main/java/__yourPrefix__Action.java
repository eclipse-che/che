#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.notification.NotificationManager;

public class ${yourPrefix}Action extends Action {

private NotificationManager notificationManager;

    @Inject
    public ${yourPrefix}Action(${yourPrefix}Resources resources, NotificationManager notificationManager) {
        super("${yourPrefix} Action", "${yourPrefix} Action Description", null, resources.${yourPrefix}ProjectTypeIcon());
        this.notificationManager = notificationManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            notificationManager.showInfo("It's ${yourPrefix} action !");
    }
}
