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
package org.eclipse.che.ide.resources.selector;

import static com.google.gwt.dom.client.Style.Overflow.AUTO;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;
import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.resources.tree.SkipHiddenNodesInterceptor;
import org.eclipse.che.ide.resources.tree.SkipLeafsInterceptor;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.HasAttributes;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.eclipse.che.ide.ui.window.Window;

/** @author Vlad Zhukovskiy */
@Singleton
public class SelectPathViewImpl extends Window implements SelectPathView {

  public static final String CUSTOM_BACKGROUND_FILL = "background";

  private final SkipLeafsInterceptor skipLeafsInterceptor;

  interface SelectParentViewImplUiBinder extends UiBinder<Widget, SelectPathViewImpl> {}

  private static SelectParentViewImplUiBinder uiBinder =
      GWT.create(SelectParentViewImplUiBinder.class);

  @UiField DockLayoutPanel content;

  private Tree tree;
  private SelectPathView.ActionDelegate delegate;

  private Button submitBtn;
  private Button cancelButton;

  @Inject
  public SelectPathViewImpl(
      Styles styles,
      SkipHiddenNodesInterceptor skipHiddenNodesInterceptor,
      SkipLeafsInterceptor skipLeafsInterceptor) {
    this.skipLeafsInterceptor = skipLeafsInterceptor;
    Widget widget = uiBinder.createAndBindUi(this);
    widget.addStyleName(styles.css().window());
    setWidget(widget);

    styles.css().ensureInjected();

    final NodeStorage storage = new NodeStorage();
    final NodeLoader loader =
        new NodeLoader(Collections.<NodeInterceptor>singleton(skipHiddenNodesInterceptor));
    tree = new Tree(storage, loader);
    tree.setPresentationRenderer(
        new DefaultPresentationRenderer<Node>(tree.getTreeStyles()) {
          @Override
          public Element render(Node node, String domID, Tree.Joint joint, int depth) {
            Element element = super.render(node, domID, joint, depth);

            element.setAttribute("name", node.getName());

            if (node instanceof ResourceNode) {
              element.setAttribute(
                  "path", ((ResourceNode) node).getData().getLocation().toString());
            }

            if (node instanceof HasAttributes
                && ((HasAttributes) node).getAttributes().containsKey(CUSTOM_BACKGROUND_FILL)) {
              element
                  .getFirstChildElement()
                  .getStyle()
                  .setBackgroundColor(
                      ((HasAttributes) node).getAttributes().get(CUSTOM_BACKGROUND_FILL).get(0));
            }

            return element;
          }
        });

    tree.setAutoSelect(true);
    tree.getSelectionModel().setSelectionMode(SINGLE);
    tree.getSelectionModel()
        .addSelectionChangedHandler(
            new SelectionChangedEvent.SelectionChangedHandler() {
              @Override
              public void onSelectionChanged(SelectionChangedEvent event) {
                final List<Node> selection = event.getSelection();
                if (selection == null || selection.isEmpty()) {
                  delegate.onPathSelected(Path.ROOT);
                  return;
                }

                final Node head = selection.get(0);

                if (head instanceof ResourceNode) {
                  final Path path = ((ResourceNode) head).getData().getLocation();
                  delegate.onPathSelected(path);
                }
              }
            });

    tree.ensureDebugId("select-path");
    tree.getElement().getStyle().setOverflowY(AUTO);

    content.add(tree);

    KeyboardNavigationHandler handler =
        new KeyboardNavigationHandler() {
          @Override
          public void onEnter(NativeEvent evt) {
            evt.preventDefault();
            hide();
            delegate.onSubmit();
          }
        };

    handler.bind(tree);

    submitBtn =
        addFooterButton(
            "Select",
            "select-path-submit-button",
            event -> {
              delegate.onSubmit();
              hide();
            },
            true);

    cancelButton =
        addFooterButton(
            "Cancel",
            "select-path-cancel-button",
            event -> {
              delegate.onCancel();
              hide();
            });

    setTitle("Select Path");
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    super.onEnterPress(evt);

    if (isWidgetOrChildFocused(submitBtn)) {
      delegate.onSubmit();
      hide();
    } else if (isWidgetOrChildFocused(cancelButton)) {
      delegate.onCancel();
      hide();
    }
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setStructure(List<Node> nodes, boolean showFiles) {
    if (showFiles) {
      tree.getNodeLoader().getNodeInterceptors().remove(skipLeafsInterceptor);
    } else {
      tree.getNodeLoader().getNodeInterceptors().add(skipLeafsInterceptor);
    }

    tree.getNodeStorage().clear();
    tree.getNodeStorage().add(nodes);
  }

  @Override
  public void showDialog() {
    show(tree);
  }

  @Override
  protected void onShow() {
    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
  }

  @Override
  protected void onHide() {
    delegate.onCancel();
  }

  public interface Styles extends ClientBundle {
    interface Css extends CssResource {
      String window();

      String label();
    }

    @Source("pathSelector.css")
    Css css();
  }
}
