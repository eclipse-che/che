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
package org.eclipse.che.ide.resources.tree;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;

/**
 * Node that represents file node in the project tree.
 *
 * @author Vlad Zhukovskiy
 * @see File
 * @see ResourceNode
 * @since 4.4.0
 */
@Beta
public class FileNode extends ResourceNode<File> implements HasAction {
  protected final Set<NodeIconProvider> nodeIconProvider;
  private final EditorAgent editorAgent;

  @Inject
  public FileNode(
      @Assisted File resource,
      @Assisted NodeSettings nodeSettings,
      NodeFactory nodeFactory,
      NodesResources nodesResources,
      EventBus eventBus,
      Set<NodeIconProvider> nodeIconProvider,
      EditorAgent editorAgent) {
    super(resource, nodeSettings, nodesResources, nodeFactory, eventBus, nodeIconProvider);
    this.nodeIconProvider = nodeIconProvider;
    this.editorAgent = editorAgent;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed() {
    editorAgent.openEditor(getData());
  }
}
