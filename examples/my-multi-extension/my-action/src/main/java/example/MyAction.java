/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package example;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.notification.NotificationManager;

public class MyAction extends Action {

private NotificationManager notificationManager;

    @Inject
    public MyAction(MyResources resources, NotificationManager notificationManager) {
        super("My Action", "My Action Description", null, resources.MyProjectTypeIcon());
        this.notificationManager = notificationManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            notificationManager.showInfo("It's My action !");
    }
}
