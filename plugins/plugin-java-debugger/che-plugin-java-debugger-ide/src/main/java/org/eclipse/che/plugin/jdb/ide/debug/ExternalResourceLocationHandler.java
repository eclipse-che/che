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
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFileNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.plugin.debugger.ide.debug.FileResourceLocationHandler;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class ExternalResourceLocationHandler extends FileResourceLocationHandler {

  private DtoFactory dtoFactory;
  private JavaLanguageExtensionServiceClient service;
  private final JavaNodeFactory nodeFactory;

  @Inject
  public ExternalResourceLocationHandler(
      EditorAgent editorAgent,
      DtoFactory dtoFactory,
      AppContext appContext,
      JavaLanguageExtensionServiceClient service,
      JavaNodeFactory nodeFactory) {
    super(editorAgent, appContext);
    this.dtoFactory = dtoFactory;
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

    final String className = extractOuterClassFqn(location.getTarget());
    final String libId = location.getExternalResourceId();
    final Path projectPath = new Path(location.getResourceProjectPath());

    ExternalLibrariesParameters params = dtoFactory.createDto(ExternalLibrariesParameters.class);
    params.setProjectUri(location.getResourceProjectPath());
    params.setNodeId(libId);
    params.setNodePath(className);
    service
        .libraryEntry(params)
        .then(
            jarEntry -> {
              final JarFileNode file =
                  nodeFactory.newJarFileNode(jarEntry, libId, projectPath, null);
              callback.onSuccess(file);
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  private String extractOuterClassFqn(String fqn) {
    // handle fqn in case of nested classes
    if (fqn.contains("$")) {
      return fqn.substring(0, fqn.indexOf("$"));
    }
    // handle fqn in case lambda expressions
    if (fqn.contains("$$")) {
      return fqn.substring(0, fqn.indexOf("$$"));
    }
    return fqn;
  }
}
