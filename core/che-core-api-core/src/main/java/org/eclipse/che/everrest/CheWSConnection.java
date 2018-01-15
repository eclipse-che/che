/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.everrest;

import static org.eclipse.che.everrest.ServerContainerInitializeListener.ENVIRONMENT_CONTEXT;

import java.util.Map;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.everrest.websockets.WSConnectionImpl;

/** @author Sergii Kabashniuk */
public class CheWSConnection extends WSConnectionImpl {
  @Override
  public void onOpen(Session session, EndpointConfig config) {
    final Map<String, Object> userProperties = config.getUserProperties();
    setAttribute(ENVIRONMENT_CONTEXT, userProperties.get(ENVIRONMENT_CONTEXT));
    super.onOpen(session, config);
  }
}
