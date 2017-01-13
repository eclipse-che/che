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
import com.google.inject.ImplementedBy;

import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;

import java.util.List;
import java.util.Set;

/**
 * Used to send some maven messages to client via WebSocket
 *
 * @author Evgen Vidolob
 */
@ImplementedBy(MavenWebSocketCommunication.class)
public interface MavenCommunication {

    void sendUpdateMassage(Set<MavenProject> updated, List<MavenProject> removed);

    void sendNotification(NotificationMessage message);

    void send(JsonObject object, MessageType type);
}
