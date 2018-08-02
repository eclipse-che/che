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
package org.eclipse.che.ide.part.editor.multipart;

import static com.google.gwt.dom.client.Style.Unit.PCT;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.parts.EditorMultiPartStackState;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.part.editor.EmptyEditorsPanel;
import org.eclipse.che.ide.util.loging.Log;

/** @author Roman Nikitenko */
public class EditorMultiPartStackViewImpl extends ResizeComposite
    implements EditorMultiPartStackView {

  private LayoutPanel contentPanel;

  private final BiMap<EditorPartStack, SplitEditorPartView> splitEditorParts;
  private final SplitEditorPartViewFactory splitEditorPartViewFactory;
  private final EmptyEditorsPanel emptyEditorsPanel;

  private SplitEditorPartView rootView;

  private EditorPlaceholderWidget editorPlaceholderWidget;

  @Inject
  public EditorMultiPartStackViewImpl(
      SplitEditorPartViewFactory splitEditorPartViewFactory,
      EmptyEditorsPanel emptyEditorsPanel,
      EditorPlaceholderWidget editorPlaceholderWidget) {
    this.splitEditorPartViewFactory = splitEditorPartViewFactory;
    this.emptyEditorsPanel = emptyEditorsPanel;
    this.splitEditorParts = HashBiMap.create();
    this.editorPlaceholderWidget = editorPlaceholderWidget;

    contentPanel = new LayoutPanel();
    contentPanel.setSize("100%", "100%");
    contentPanel.ensureDebugId("editorMultiPartStack-contentPanel");
    initWidget(contentPanel);
    contentPanel.add(emptyEditorsPanel);
  }

  @Override
  public void addPartStack(
      @NotNull final EditorPartStack partStack,
      final EditorPartStack relativePartStack,
      final Constraints constraints,
      final double size) {
    AcceptsOneWidget partViewContainer =
        new AcceptsOneWidget() {
          @Override
          public void setWidget(IsWidget widget) {
            if (relativePartStack == null) {
              rootView = splitEditorPartViewFactory.create(widget);
              splitEditorParts.put(partStack, rootView);
              contentPanel.remove(emptyEditorsPanel);
              contentPanel.add(rootView);
              return;
            }

            SplitEditorPartView relativePartStackView = splitEditorParts.get(relativePartStack);
            if (relativePartStackView == null) {
              Log.error(getClass(), "Can not find container for specified editor");
              return;
            }

            relativePartStackView.split(widget, constraints.direction, size);
            splitEditorParts.put(partStack, relativePartStackView.getReplica());
            splitEditorParts.put(relativePartStack, relativePartStackView.getSpecimen());
          }
        };
    partStack.go(partViewContainer);
  }

  @Override
  public void removePartStack(@NotNull EditorPartStack partStack) {
    SplitEditorPartView splitEditorPartView = splitEditorParts.remove(partStack);
    if (splitEditorPartView != null) {
      splitEditorPartView.removeFromParent();
    }
    if (splitEditorParts.size() == 0) {
      contentPanel.remove(rootView);
      contentPanel.add(emptyEditorsPanel);
      rootView = null;
    }
  }

  @Override
  public EditorMultiPartStackState getState() {
    if (rootView == null) {
      return null;
    }
    return rootView.getState(splitEditorParts.inverse());
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    Style style = getElement().getParentElement().getStyle();
    style.setHeight(100, PCT);
    style.setWidth(100, PCT);
  }

  @Override
  public void showPlaceholder(boolean placeholder) {
    if (placeholder) {
      if (!editorPlaceholderWidget.getElement().hasParentElement()) {
        getElement().appendChild(editorPlaceholderWidget.getElement());
      }
    } else {
      if (editorPlaceholderWidget.getElement().hasParentElement()) {
        getElement().removeChild(editorPlaceholderWidget.getElement());
      }
    }
  }
}
