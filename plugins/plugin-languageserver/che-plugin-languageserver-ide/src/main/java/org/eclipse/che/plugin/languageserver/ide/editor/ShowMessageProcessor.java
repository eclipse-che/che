/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.editor;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.util.loging.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.typefox.lsapi.MessageParams;

/**
 * A processor for incoming <code>window/showMessage</code> notifications sent
 * by a language server.
 * 
 * @author xcoulon
 */
@Singleton
public class ShowMessageProcessor {

	private final NotificationManager notificationManager;

	@Inject
	public ShowMessageProcessor(final NotificationManager notificationManager) {
		this.notificationManager = notificationManager;
	}

	public void processNotification(final MessageParams messageParams) {
		Log.debug(getClass(), "Received a 'ShowMessage' message: " + messageParams.getMessage());
		switch(messageParams.getType()) {
		case Error:
			this.notificationManager.notify(messageParams.getMessage(), StatusNotification.Status.FAIL, FLOAT_MODE);
			break;
		case Warning:
			this.notificationManager.notify(messageParams.getMessage(), StatusNotification.Status.WARNING, FLOAT_MODE);
			break;
		case Info:
		case Log:
		default:
			this.notificationManager.notify(messageParams.getMessage(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
			break;
		}
	}

}
