// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.ui;

import com.google.gwt.dom.client.Node;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import org.eclipse.che.ide.runtime.Assert;
import org.eclipse.che.ide.ui.menu.AutoHideComponent;
import org.eclipse.che.ide.ui.menu.AutoHideView;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.util.dom.Elements;

/** Represents a floating popup, that can be attached to any element. */
public class Popup extends AutoHideComponent<Popup.View, AutoHideComponent.AutoHideModel>
    implements IsWidget {

  public interface Css extends CssResource {
    String root();

    String contentHolder();
  }

  public interface Resources extends ClientBundle {
    @Source({
      "org/eclipse/che/ide/ui/constants.css",
      "Popup.css",
      "org/eclipse/che/ide/api/ui/style.css"
    })
    Css popupCss();
  }

  /** The View for the Popup component. */
  public static class View extends AutoHideView<Void> {
    private final Css css;

    private final Element contentHolder;

    View(Resources resources) {
      this.css = resources.popupCss();

      contentHolder = Elements.createDivElement(css.contentHolder());

      Element rootElement = Elements.createDivElement(css.root());
      rootElement.appendChild(contentHolder);
      setElement(rootElement);
    }

    void setContentElement(Element contentElement) {
      contentHolder.setInnerHTML("");
      if (contentElement != null) {
        contentHolder.appendChild(contentElement);
      }
    }
  }

  public static Popup create(Resources resources) {
    View view = new View(resources);
    return new Popup(view);
  }

  private PositionController positionController;

  private HTML widget;

  private Popup(View view) {
    super(view, new AutoHideModel());
  }

  @Override
  public void show() {
    Assert.isNotNull(
        positionController, "You cannot show this popup without using a position controller");
    positionController.updateElementPosition();

    cancelPendingHide();
    super.show();
  }

  /** Shows the popup anchored to a given element. */
  public void show(PositionController.Positioner positioner) {
    positionController = new PositionController(positioner, getView().getElement());
    show();
  }

  /**
   * Sets the popup's content element.
   *
   * @param contentElement the DOM element to show in the popup, or {@code null} to clean up the
   *     popup's DOM
   */
  public void setContentElement(Element contentElement) {
    getView().setContentElement(contentElement);
  }

  public void destroy() {
    forceHide();
    setContentElement(null);
    positionController = null;
  }

  /** {@inheritDoc} */
  @Override
  public Widget asWidget() {
    if (widget == null) {
      widget = new HTML();
      widget.getElement().appendChild((Node) getView().getElement());
    }

    return widget;
  }
}
