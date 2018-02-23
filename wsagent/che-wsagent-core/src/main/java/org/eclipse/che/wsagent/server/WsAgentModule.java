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
package org.eclipse.che.wsagent.server;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.LivenessProbeService;
import org.eclipse.che.inject.DynaModule;

/**
 * Mandatory modules of workspace agent
 *
 * @author Evgen Vidolob
 * @author Sergii Kabashniuk
 */
@DynaModule
public class WsAgentModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ApiInfoService.class);
    bind(LivenessProbeService.class);
    install(new org.eclipse.che.security.oauth.OAuthAgentModule());
    install(new org.eclipse.che.api.core.rest.CoreRestModule());
    install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
    install(new org.eclipse.che.api.project.server.ProjectApiModule());
    install(new org.eclipse.che.api.editor.server.EditorApiModule());
    install(new org.eclipse.che.api.fs.server.FsApiModule());
    install(new org.eclipse.che.api.search.server.SearchApiModule());
    install(new org.eclipse.che.api.watcher.server.FileWatcherApiModule());
    install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());
    install(new org.eclipse.che.plugin.ssh.key.SshModule());
    install(new org.eclipse.che.api.languageserver.LanguageServerModule());
    install(new org.eclipse.che.api.debugger.server.DebuggerModule());
    install(new org.eclipse.che.api.git.GitModule());
    install(new org.eclipse.che.git.impl.jgit.JGitModule());
    install(new org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule());
    install(new org.eclipse.che.api.core.websocket.impl.WebSocketModule());
  }
}
