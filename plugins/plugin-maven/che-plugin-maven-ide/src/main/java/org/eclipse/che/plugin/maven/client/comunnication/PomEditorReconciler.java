/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.comunnication;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;

/** @author Evgen Vidolob */
@Singleton
public class PomEditorReconciler {

  private final EditorAgent editorAgent;
  private MavenServerServiceClient serverService;

  @Inject
  public PomEditorReconciler(EditorAgent editorAgent, MavenServerServiceClient serverService) {
    this.editorAgent = editorAgent;
    this.serverService = serverService;
  }

  public void reconcilePoms(final List<String> updatedProjects) {
    new Timer() {

      @Override
      public void run() {
        Set<String> pomPaths = getPomPath(updatedProjects);
        List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        for (EditorPartPresenter openedEditor : openedEditors) {
          String path = openedEditor.getEditorInput().getFile().getLocation().toString();
          if (pomPaths.contains(path)) {
            serverService.reconcilePom(path);
          }
        }
      }
    }.schedule(2000);
  }

  private Set<String> getPomPath(List<String> updatedProjects) {
    Set<String> result = new HashSet<>();
    for (String projectPath : updatedProjects) {
      String pomPath =
          projectPath.endsWith("/") ? projectPath + "pom.xml" : projectPath + "/pom.xml";
      result.add(pomPath);
    }

    return result;
  }
}
