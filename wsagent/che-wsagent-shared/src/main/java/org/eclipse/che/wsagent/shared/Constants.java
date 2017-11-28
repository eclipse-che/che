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
package org.eclipse.che.wsagent.shared;

/**
 * Constants related to Workspace Agent
 *
 * @author Roman Nikitenko
 */
public final class Constants {
  public static final String WS_AGENT_TRACK_CONNECTION_SUBSCRIBE =
      "ws-agent/track/connection/subscribe";
  public static final String WS_AGENT_TRACK_CONNECTION_UNSUBSCRIBE =
      "ws-agent/track/connection/unsubscribe";
  public static final String WS_AGENT_TRACK_CONNECTION_HEARTBEAT =
      "ws-agent/track/connection/heartbeat";
  public static final int WS_AGENT_TRACK_CONNECTION_PERIOD_MILLISECONDS = 3_000;
  public static final int WS_AGENT_TRACK_CONNECTION_CLEANUP_PERIOD_MINUTES = 5;

  private Constants() {}
}
