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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent;

/**
 * A wrapper over {@link MavenOutputEvent} to send data over {@link EventService}.
 * Contains type as identifier of the target maven.
 */
@Deprecated
public class MavenMessage {
    private final MavenOutputEvent mavenOutputEvent;
    private final String           notificationType;

    public MavenMessage(MavenOutputEvent mavenOutputEvent, String notificationType) {
        this.mavenOutputEvent = mavenOutputEvent;
        this.notificationType = notificationType;
    }

    public MavenOutputEvent getMavenOutputEvent() {
        return mavenOutputEvent;
    }

    public String getNotificationType() {
        return notificationType;
    }
}
