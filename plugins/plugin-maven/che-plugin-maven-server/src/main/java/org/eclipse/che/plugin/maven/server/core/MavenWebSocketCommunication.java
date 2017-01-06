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

import com.google.gson.JsonObject;
import com.google.inject.Singleton;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EncodeException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenWebSocketCommunication implements MavenCommunication {

    private static final Logger LOG = LoggerFactory.getLogger(MavenWebSocketCommunication.class);

    @Override
    public void sendUpdateMassage(Set<MavenProject> updated, List<MavenProject> removed) {
        ProjectsUpdateMessage dto = DtoFactory.newDto(ProjectsUpdateMessage.class);

        List<String> updatedPaths =
                updated.stream().map(project -> project.getProject().getFullPath().toOSString()).collect(Collectors.toList());
        dto.setUpdatedProjects(updatedPaths);

        List<String> removedPaths =
                removed.stream().map(project -> project.getProject().getFullPath().toOSString()).collect(Collectors.toList());

        dto.setDeletedProjects(removedPaths);

        send(DtoFactory.getInstance().toJsonElement(dto).getAsJsonObject(), MessageType.UPDATE);
    }

    @Override
    public void sendNotification(NotificationMessage message) {
        send(DtoFactory.getInstance().toJsonElement(message).getAsJsonObject(), MessageType.NOTIFICATION);
    }

    @Override
    public void send(JsonObject dto, MessageType type) {
        try {

            ChannelBroadcastMessage message = new ChannelBroadcastMessage();
            message.setChannel(MavenAttributes.MAVEN_CHANEL_NAME);


            dto.addProperty("$type", type.getType());

            message.setBody(dto.toString());

            WSConnectionContext.sendMessage(message);
        } catch (EncodeException | IOException e) {
            LOG.error("Can't send maven message:", e);
        }
    }
}
