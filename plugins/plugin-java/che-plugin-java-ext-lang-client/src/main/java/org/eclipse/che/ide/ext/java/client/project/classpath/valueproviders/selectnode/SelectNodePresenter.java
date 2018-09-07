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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.ClasspathNodeInterceptor;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.SettingsProvider;

/**
 * Presenter for choosing directory for searching a node.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodePresenter implements SelectNodeView.ActionDelegate {
  private final SelectNodeView view;
  private final ResourceNode.NodeFactory nodeFactory;
  private final SettingsProvider settingsProvider;
  private final AppContext appContext;

  private ClasspathPagePresenter classpathPagePresenter;
  private ClasspathNodeInterceptor interceptor;

  @Inject
  public SelectNodePresenter(
      SelectNodeView view,
      ResourceNode.NodeFactory nodeFactory,
      SettingsProvider settingsProvider,
      AppContext appContext) {
    this.view = view;
    this.nodeFactory = nodeFactory;
    this.settingsProvider = settingsProvider;
    this.appContext = appContext;
    this.view.setDelegate(this);
  }

  /**
   * Show tree view with all needed nodes of the workspace.
   *
   * @param pagePresenter delegate from the property page
   * @param nodeInterceptor interceptor for showing nodes
   * @param forCurrent if is true - show only current project, otherwise - whole workspace
   */
  public void show(
      ClasspathPagePresenter pagePresenter,
      ClasspathNodeInterceptor nodeInterceptor,
      boolean forCurrent) {
    this.classpathPagePresenter = pagePresenter;
    this.interceptor = nodeInterceptor;
    if (forCurrent) {
      final Project project = appContext.getRootProject();

      view.setStructure(
          Collections.<Node>singletonList(
              nodeFactory.newContainerNode(project, settingsProvider.getSettings())),
          interceptor);
    } else {
      final List<Node> nodes = new ArrayList<>();
      for (Project project : appContext.getProjects()) {
        nodes.add(nodeFactory.newContainerNode(project, settingsProvider.getSettings()));
      }

      view.setStructure(nodes, interceptor);
    }

    view.showDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void setSelectedNode(String path) {
    classpathPagePresenter.addNode(path, interceptor.getKind());
  }
}
