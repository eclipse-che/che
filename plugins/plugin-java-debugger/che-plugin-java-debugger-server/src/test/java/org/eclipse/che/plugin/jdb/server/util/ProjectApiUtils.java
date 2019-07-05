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
package org.eclipse.che.plugin.jdb.server.util;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.EndpointIdConfigurator;
import org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.eclipse.che.api.editor.server.EditorApiModule;
import org.eclipse.che.api.fs.server.FsApiModule;
import org.eclipse.che.api.project.server.ProjectApiModule;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.ProjectServiceApi;
import org.eclipse.che.api.project.server.impl.ProjectServiceApiFactory;
import org.eclipse.che.api.project.server.impl.ProjectServiceVcsStatusInjector;
import org.eclipse.che.api.project.server.impl.WorkspaceProjectSynchronizer;
import org.eclipse.che.api.search.server.SearchApiModule;
import org.eclipse.che.api.watcher.server.FileWatcherApiModule;
import org.eclipse.che.plugin.java.server.inject.JavaModule;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatolii Bazko */
public class ProjectApiUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectApiUtils.class);
  private static final AtomicBoolean initialized = new AtomicBoolean();

  /** Ensures that project api has been initialized only once. */
  public static void ensure() throws Exception {
    if (!initialized.get()) {
      synchronized (initialized) {
        if (!initialized.get()) {
          try {
            init();
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
          }
          initialized.set(true);
        }
      }
    }
  }

  /** Initialize project API for tests. */
  private static void init() throws Exception {
    File root = new File("target/test-classes/workspace").getAbsoluteFile();
    File indexDir = new File("target/test-classes/workspace/index");

    Injector injector =
        Guice.createInjector(
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(File.class)
                    .annotatedWith(Names.named("che.user.workspaces.storage"))
                    .toInstance(root);
                bind(File.class)
                    .annotatedWith(Names.named("vfs.local.fs_index_root_dir"))
                    .toInstance(indexDir);
                bind(String.class).annotatedWith(Names.named("che.api")).toInstance("api-endpoint");
                bind(String.class)
                    .annotatedWith(Names.named("project.importer.default_importer_id"))
                    .toInstance("git");
                bind(String.class)
                    .annotatedWith(Names.named("che.core.jsonrpc.processor_max_pool_size"))
                    .toInstance("100");

                install(
                    new FactoryModuleBuilder()
                        .implement(ProjectServiceApi.class, ProjectServiceApi.class)
                        .build(ProjectServiceApiFactory.class));

                bind(ProjectServiceVcsStatusInjector.class)
                    .toInstance(
                        mock(
                            ProjectServiceVcsStatusInjector.class,
                            Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)));
                bind(RequestHandlerManager.class)
                    .toInstance(
                        mock(
                            RequestHandlerManager.class,
                            Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)));

                bind(EndpointIdConfigurator.class)
                    .toInstance(
                        mock(
                            EndpointIdConfigurator.class,
                            Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)));

                bind(WebSocketMessageTransmitter.class)
                    .toInstance(
                        mock(
                            WebSocketMessageTransmitter.class,
                            Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)));

                bind(WorkspaceProjectSynchronizer.class)
                    .toInstance(
                        mock(
                            WorkspaceProjectSynchronizer.class,
                            Mockito.withSettings().defaultAnswer(RETURNS_DEEP_STUBS)));

                install(new ProjectApiModule());
                install(new FsApiModule());
                install(new SearchApiModule());
                install(new EditorApiModule());
                install(new FileWatcherApiModule());
                install(new JsonRpcModule());
                install(new JavaModule());
                install(new SearchApiModule());
              }
            });

    ProjectManager projectManager = injector.getInstance(ProjectManager.class);
    projectManager.setType("/test", "java", false);
  }
}
