/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.wsagent.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.util.concurrent.ExecutorService;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.LivenessProbeService;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.wsagent.server.jsonrpc.WsAgentWebSocketEndpointConfiguration;
import org.eclipse.che.wsagent.server.jsonrpc.WsAgentWebSocketEndpointExecutorServiceProvider;

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
    bind(ExecutorService.class)
        .annotatedWith(Names.named(WsAgentWebSocketEndpointConfiguration.EXECUTOR_NAME))
        .toProvider(WsAgentWebSocketEndpointExecutorServiceProvider.class);
    Multibinder<RequestProcessorConfigurationProvider.Configuration> configurationMultibinder =
        Multibinder.newSetBinder(
            binder(), RequestProcessorConfigurationProvider.Configuration.class);
    configurationMultibinder.addBinding().to(WsAgentWebSocketEndpointConfiguration.class);
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
    install(
        new org.eclipse.che.api.fs.server.impl.FreeDiskSpaceChecker.FreeDiskSpaceCheckerModule());
  }
}
