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
package org.eclipse.che.plugin.jdb.ide.debug;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFileNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.debugger.ide.debug.FileResourceLocationHandler;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class ExternalResourceLocationHandler extends FileResourceLocationHandler {

  private JavaLanguageExtensionServiceClient service;
  private final JavaNodeFactory nodeFactory;

  @Inject
  public ExternalResourceLocationHandler(
      EditorAgent editorAgent,
      AppContext appContext,
      JavaLanguageExtensionServiceClient service,
      JavaNodeFactory nodeFactory) {
    super(editorAgent, appContext);
    this.service = service;

    this.nodeFactory = nodeFactory;
  }

  @Override
  public boolean isSuitedFor(Location location) {
    return location.isExternalResource() && location.getExternalResourceId() != null;
  }

  @Override
  public void find(Location location, AsyncCallback<VirtualFile> callback) {
    findInOpenedEditors(
        location,
        new AsyncCallback<VirtualFile>() {
          @Override
          public void onFailure(Throwable caught) {
            findExternalResource(location, callback);
          }

          @Override
          public void onSuccess(VirtualFile result) {
            callback.onSuccess(result);
          }
        });
  }

  private void findExternalResource(
      final Location location, final AsyncCallback<VirtualFile> callback) {

    Resource resource = appContext.getResource();
    if (resource == null) {
      callback.onFailure(new IllegalStateException("Resource is undefined"));
      return;
    }

    Project project = resource.getProject();
    if (project == null) {
      callback.onFailure(new IllegalStateException("Project is undefined"));
      return;
    }

    service
        .libraryEntry(location.getExternalResourceId())
        .then(
            jarEntry -> {
              final JarFileNode file =
                  nodeFactory.newJarFileNode(
                      jarEntry,
                      location.getExternalResourceId(),
                      Path.valueOf(project.getPath()),
                      null);
              callback.onSuccess(file);
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }
}
