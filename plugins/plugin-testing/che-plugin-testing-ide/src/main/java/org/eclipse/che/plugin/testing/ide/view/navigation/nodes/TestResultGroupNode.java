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

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.SpanElement;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Tree node for display the failing class.
 *
 * @author Mirage Abeysekara
 */
@Deprecated
public class TestResultGroupNode extends AbstractTreeNode implements HasPresentation {

  private NodePresentation nodePresentation;
  private final TestResources testResources;
  private final TreeStyles treeStylesResources;
  private final Runnable showOnlyFailuresDelegate;
  private int failureCount;
  private boolean showFailuresOnly;

  @Inject
  public TestResultGroupNode(
      TestResources testResources,
      TreeStyles treeStylesResources,
      @Assisted TestResult result,
      @Assisted boolean showFailuresOnly,
      @Assisted Runnable showOnlyFailuresDelegate) {
    failureCount = result.getFailureCount();
    this.testResources = testResources;
    this.treeStylesResources = treeStylesResources;
    this.showOnlyFailuresDelegate = showOnlyFailuresDelegate;
    this.showFailuresOnly = showFailuresOnly;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return Promises.resolve(children);
  }

  @Override
  public String getName() {
    if (failureCount > 0) {
      return "There are " + failureCount + " test failures.";
    } else {
      return "Test passed.";
    }
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    if (failureCount > 0) {
      presentation.setPresentableIcon(testResources.testResultsFail());
    } else {
      presentation.setPresentableIcon(testResources.testResultsPass());
    }

    SpanElement root = Elements.createSpanElement();
    SpanElement textElement =
        Elements.createSpanElement(treeStylesResources.styles().presentableTextContainer());
    textElement.setTextContent(getName());
    root.appendChild(textElement);
    SpanElement button = Elements.createSpanElement();
    SVGResource svg =
        showFailuresOnly
            ? testResources.showAllTestsButtonIcon()
            : testResources.showFailuresOnlyButtonIcon();
    String tooltip = showFailuresOnly ? "Include successful tests" : "Hide successful tests";

    Tooltip.create(button, BOTTOM, MIDDLE, tooltip);

    button.appendChild((elemental.dom.Node) new SVGImage(svg).getElement());
    button.getStyle().setProperty("float", "right");
    button.getStyle().setProperty("padding-right", "9px");
    button.getStyle().setProperty("padding-left", "8px");

    if (failureCount == 0) {
      button.getStyle().setDisplay("none");
    } else {
      button.addEventListener(
          Event.CLICK,
          new EventListener() {
            @Override
            public void handleEvent(Event event) {
              event.stopPropagation();
              event.preventDefault();
              showOnlyFailuresDelegate.run();
            }
          },
          true);
      button.getStyle().setDisplay("inline");
    }

    /**
     * This listener cancels mouse events on '+' button and prevents the jitter of the selection in
     * the tree.
     */
    EventListener blockMouseListener =
        new EventListener() {
          @Override
          public void handleEvent(Event event) {
            event.stopPropagation();
            event.preventDefault();
          }
        };

    /** Prevent jitter when pressing mouse on '+' button. */
    button.addEventListener(Event.MOUSEDOWN, blockMouseListener, true);
    button.addEventListener(Event.MOUSEUP, blockMouseListener, true);
    button.addEventListener(Event.CLICK, blockMouseListener, true);
    button.addEventListener(Event.DBLCLICK, blockMouseListener, true);

    root.appendChild(button);

    presentation.setUserElement((Element) root);
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
}
