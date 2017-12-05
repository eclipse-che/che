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

package org.eclipse.che.ide.js.api.workspace;

import jsinterop.annotations.JsType;
import org.eclipse.che.ide.js.api.Disposable;
import org.eclipse.che.ide.js.api.event.Listener;
import org.eclipse.che.ide.js.api.workspace.event.JsExecAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsExecAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsTerminalAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsTerminalAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsWsAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsWsAgentServerStoppedEvent;

/** @author Yevhen Vydolob */
@JsType
public interface WorkspaceRuntime {

  Disposable addServerRunningListener(Listener<JsServerRunningEvent> listener);

  Disposable addWsAgentServerRunningListener(Listener<JsWsAgentServerRunningEvent> listener);

  Disposable addTerminalAgentServerRunningListener(
      Listener<JsTerminalAgentServerRunningEvent> listener);

  Disposable addExecAgentServerRunningListener(Listener<JsExecAgentServerRunningEvent> listener);

  Disposable addServerStoppedListener(Listener<JsServerStoppedEvent> listener);

  Disposable addWsAgentServerStoppedListener(Listener<JsWsAgentServerStoppedEvent> listener);

  Disposable addTerminalAgentServerStoppedListener(
      Listener<JsTerminalAgentServerStoppedEvent> listener);

  Disposable addExecAgentServerStoppedListener(Listener<JsExecAgentServerStoppedEvent> listener);
}
