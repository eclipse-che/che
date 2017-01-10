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
package org.eclipse.che.plugin.maven.server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;
import org.eclipse.che.plugin.maven.shared.dto.StartStopNotification;
import org.eclipse.che.maven.server.MavenServerProgressNotifier;

/**
 * Default implementation of {@link MavenServerProgressNotifier}
 *
 * @author Evgen Vidolob
 */
@Singleton
public class MavenServerNotifier implements MavenProgressNotifier {

    private final MavenCommunication communication;

    @Inject
    public MavenServerNotifier(MavenCommunication communication) {
        this.communication = communication;
    }

    @Override
    public void setText(String text) {
        NotificationMessage dto = DtoFactory.newDto(NotificationMessage.class);
        dto.setText(text);
        communication.sendNotification(dto);
    }

    @Override
    public void setPercent(double percent) {
        NotificationMessage dto = DtoFactory.newDto(NotificationMessage.class);
        dto.setPercent(percent);
        communication.sendNotification(dto);
    }

    @Override
    public void setPercentUndefined(boolean undefined) {
        NotificationMessage dto = DtoFactory.newDto(NotificationMessage.class);
        dto.setPercentUndefined(undefined);
        communication.sendNotification(dto);
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void stop() {
        sendStartStop(false);
    }

    private void sendStartStop(boolean isStart) {
        StartStopNotification dto = DtoFactory.newDto(StartStopNotification.class);
        dto.setStart(isStart);
        communication.send(DtoFactory.getInstance().toJsonElement(dto).getAsJsonObject(), MessageType.START_STOP);
    }

    @Override
    public void start() {
        sendStartStop(true);
    }

}
