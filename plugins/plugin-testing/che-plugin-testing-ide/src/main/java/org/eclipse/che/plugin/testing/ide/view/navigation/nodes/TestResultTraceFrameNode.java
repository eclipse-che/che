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
package org.eclipse.che.plugin.testing.ide.view.navigation.nodes;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.testing.shared.dto.SimpleLocationDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceFrameDto;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.view.navigation.SimpleLocationHandler;

/**
 * Test result trace frame node.
 *
 * @author Bartlomiej Laczkowski
 */
public class TestResultTraceFrameNode extends AbstractTreeNode
    implements HasPresentation, HasAction {

  private final TestResources testResources;
  private final SimpleLocationHandler simpleLocationHandler;
  private final TestResultTraceFrameDto testResultTraceFrameDto;
  private NodePresentation nodePresentation;

  @Inject
  public TestResultTraceFrameNode(
      TestResources testResources,
      SimpleLocationHandler simpleLocationHandler,
      @Assisted TestResultTraceFrameDto testResultTraceFrameDto) {
    this.testResources = testResources;
    this.simpleLocationHandler = simpleLocationHandler;
    this.testResultTraceFrameDto = testResultTraceFrameDto;
  }

  @Override
  public String getName() {
    return testResultTraceFrameDto.getTraceFrame();
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableIcon(testResources.testResultTraceFrameIcon());
    presentation.setPresentableText(getName());
  }

  @Override
  public NodePresentation getPresentation(boolean update) {
    if (nodePresentation == null) {
      nodePresentation = new NodePresentation();
      updatePresentation(nodePresentation);
    }
    if (update) {
      updatePresentation(nodePresentation);
    }
    return nodePresentation;
  }

  @Override
  public void actionPerformed() {
    SimpleLocationDto location = testResultTraceFrameDto.getLocation();
    if (location != null) {
      simpleLocationHandler.openFile(location);
    }
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return null;
  }
}
