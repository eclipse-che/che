/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core;

import com.google.gson.JsonObject;
import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Set;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;

/**
 * Used to send some maven messages to client via WebSocket
 *
 * @author Evgen Vidolob
 */
@ImplementedBy(MavenWebSocketCommunication.class)
@Deprecated
public interface MavenCommunication {

  void sendUpdateMassage(Set<MavenProject> updated, List<MavenProject> removed);

  void sendNotification(NotificationMessage message);

  void send(JsonObject object, MessageType type);
}
