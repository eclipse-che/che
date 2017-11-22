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
package org.eclipse.che.plugin.jdb.server.util;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.EndpointIdConfigurator;
import org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.eclipse.che.api.editor.server.EditorApiModule;
import org.eclipse.che.api.fs.server.FsApiModule;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectApiModule;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.NewProjectConfigImpl;
import org.eclipse.che.api.project.server.impl.ProjectServiceApi;
import org.eclipse.che.api.project.server.impl.ProjectServiceApiFactory;
import org.eclipse.che.api.project.server.impl.ProjectServiceVcsStatusInjector;
import org.eclipse.che.api.project.server.impl.WorkspaceProjectSynchronizer;
import org.eclipse.che.api.search.server.SearchApiModule;
import org.eclipse.che.api.watcher.server.FileWatcherApiModule;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.mockito.Mockito;

/** @author Anatolii Bazko */
public class ProjectApiUtils {

  private static final AtomicBoolean initialized = new AtomicBoolean();

  /** Ensures that project api has been initialized only once. */
  public static void ensure() throws Exception {
    if (!initialized.get()) {
      synchronized (initialized) {
        if (!initialized.get()) {
          init();
          initialized.set(true);
        }
      }
    }
  }

  /** Initialize project API for tests. */
  private static void init() throws Exception {
    File root = new File("target/test-classes/workspace");
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
              }
            });

    ProjectManager projectManager = injector.getInstance(ProjectManager.class);
    FsManager fsManager = injector.getInstance(FsManager.class);
    PathTransformer pathTransformer = injector.getInstance(PathTransformer.class);

    ResourcesPlugin resourcesPlugin =
        new ResourcesPlugin(
            "target/test-classes/workspace/index",
            root.getAbsolutePath(),
            () -> projectManager,
            () -> pathTransformer,
            () -> fsManager);
    resourcesPlugin.start();

    JavaPlugin javaPlugin =
        new JavaPlugin(root.getAbsolutePath() + "/.settings", resourcesPlugin, projectManager);
    javaPlugin.start();

    projectManager.create(new NewProjectConfigImpl("/test"), Collections.emptyMap());
    projectManager.setType("/test", "java", false);

    JavaModelManager.getDeltaState().initializeRoots(true);
  }
}
