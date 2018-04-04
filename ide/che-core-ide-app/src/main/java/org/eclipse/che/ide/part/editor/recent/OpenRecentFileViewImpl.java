/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.editor.recent;

import static com.google.gwt.dom.client.Style.Overflow.AUTO;
import static org.eclipse.che.ide.part.editor.recent.RecentFileStore.getShortPath;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.HasAttributes;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation for the {@link OpenRecentFilesView}.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class OpenRecentFileViewImpl extends Window implements OpenRecentFilesView {

  public static final String CUSTOM_BACKGROUND_FILL = "background";

  interface OpenRecentFileViewImplUiBinder extends UiBinder<Widget, OpenRecentFileViewImpl> {}

  private static OpenRecentFileViewImplUiBinder uiBinder =
      GWT.create(OpenRecentFileViewImplUiBinder.class);

  @UiField DockLayoutPanel content;

  private Tree tree;
  private Label pathLabel;

  @Inject
  public OpenRecentFileViewImpl(CoreLocalizationConstant locale, Styles styles) {

    Widget widget = uiBinder.createAndBindUi(this);
    widget.addStyleName(styles.css().window());
    setWidget(widget);

    styles.css().ensureInjected();

    pathLabel = new Label();
    pathLabel.setStyleName(styles.css().label());

    final NodeStorage storage = new NodeStorage();
    final NodeLoader loader = new NodeLoader();
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
              element.setAttribute(
                  "project",
                  ((ResourceNode) node)
                      .getData()
                      .getRelatedProject()
                      .get()
                      .getLocation()
                      .toString());
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
            event -> {
              List<Node> selection = event.getSelection();
              if (selection == null || selection.isEmpty()) {
                pathLabel.setText("");
                pathLabel.setTitle("");
                return;
              }

              Node head = selection.get(0);

              if (head instanceof ResourceNode) {
                String path =
                    getShortPath(((ResourceNode) head).getData().getLocation().toString());
                pathLabel.setText(path);
                pathLabel.setTitle(path);
                return;
              }

              pathLabel.setText("");
              pathLabel.setTitle("");
            });

    KeyboardNavigationHandler handler =
        new KeyboardNavigationHandler() {
          @Override
          public void onEnter(NativeEvent evt) {
            hide();
          }
        };

    handler.bind(tree);

    tree.addDomHandler(event -> hide(), DoubleClickEvent.getType());

    tree.ensureDebugId("recent-files");
    tree.getElement().getStyle().setOverflowY(AUTO);

    content.addSouth(pathLabel, 20.);
    content.add(tree);

    setTitle(locale.openRecentFilesTitle());
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {}

  /** {@inheritDoc} */
  @Override
  public void setRecentFiles(List<FileNode> recentFiles) {
    tree.getNodeStorage().clear();
    for (FileNode recentFile : recentFiles) {
      tree.getNodeStorage().add(recentFile);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clearRecentFiles() {
    tree.getNodeStorage().clear();
  }

  /** {@inheritDoc} */
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

  public interface Styles extends ClientBundle {
    interface Css extends CssResource {
      String window();

      String label();
    }

    @Source("recent.css")
    Css css();
  }
}
