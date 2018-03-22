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
package org.eclipse.che.ide.navigation;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_DOWN;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_PAGEDOWN;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_PAGEUP;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_UP;

import com.google.common.base.Strings;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.html.TableCellElement;
import elemental.html.TableElement;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.editor.codeassist.AutoCompleteResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * The implementation of {@link NavigateToFileView} view.
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NavigateToFileViewImpl extends PopupPanel implements NavigateToFileView {

  interface NavigateToFileViewImplUiBinder extends UiBinder<Widget, NavigateToFileViewImpl> {}

  interface Styles extends CssResource {

    String labelMargin();

    String suggestionsPanel();

    String noborder();
  }

  @UiField TextBox fileName;

  @UiField(provided = true)
  CoreLocalizationConstant locale;

  private ActionDelegate delegate;

  private final AutoCompleteResources.Css css;

  private final Resources resources;

  private SimpleList<SearchResultDto> list;

  @UiField FlowPanel suggestionsPanel;

  private HTML suggestionsContainer;

  @UiField Styles style;

  private HandlerRegistration resizeHandler;

  private String previosFileName;

  @Inject
  public NavigateToFileViewImpl(
      CoreLocalizationConstant locale,
      NavigateToFileViewImplUiBinder uiBinder,
      AutoCompleteResources autoCompleteResources,
      Resources resources) {
    this.locale = locale;
    this.resources = resources;

    css = autoCompleteResources.autocompleteComponentCss();
    css.ensureInjected();

    setWidget(uiBinder.createAndBindUi(this));
    setAutoHideEnabled(true);
    setAnimationEnabled(true);
    getElement().getStyle().setProperty("boxShadow", "0 2px 4px 0 rgba(0, 0, 0, 0.50)");
    getElement().getStyle().setProperty("borderRadius", "0px");
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showPopup() {
    fileName.getElement().setAttribute("placeholder", locale.navigateToFileSearchIsCaseSensitive());

    setPopupPositionAndShow(
        (offsetWidth, offsetHeight) -> {
          setPopupPosition(
              (Window.getClientWidth() / 2) - (offsetWidth / 2),
              (Window.getClientHeight() / 4) - (offsetHeight / 2));
          // Set 'clip' css property to auto when show animation is finished.
          new Timer() {
            @Override
            public void run() {
              getElement().getStyle().setProperty("clip", "auto");
              delegate.onFileNameChanged(fileName.getText());
            }
          }.schedule(300);
        });

    new Timer() {
      @Override
      public void run() {
        fileName.setFocus(true);
      }
    }.schedule(300);

    // Add window resize handler
    if (resizeHandler == null) {
      resizeHandler = Window.addResizeHandler(event -> updatePositionAndSize());
    }
  }

  private final SimpleList.ListItemRenderer<SearchResultDto> listItemRenderer =
      new SimpleList.ListItemRenderer<SearchResultDto>() {
        @Override
        public void render(Element itemElement, SearchResultDto itemData) {
          TableCellElement label = Elements.createTDElement();
          TableCellElement path = Elements.createTDElement();

          Path itemPath = Path.valueOf(itemData.getItemReference().getPath());

          label.setInnerHTML(itemPath.lastSegment());
          path.setInnerHTML("(" + itemPath.parent() + ")");

          itemElement.appendChild(label);
          itemElement.appendChild(path);

          path.getStyle().setProperty("opacity", "0.6");
        }

        @Override
        public Element createElement() {
          return Elements.createTRElement();
        }
      };

  @Override
  public void hidePopup() {
    if (suggestionsContainer != null) {
      suggestionsContainer.removeFromParent();
    }
    suggestionsPanel.setVisible(false);

    suggestionsPanel.getElement().getStyle().setWidth(400, Style.Unit.PX);
    suggestionsPanel.getElement().getStyle().setHeight(20, Style.Unit.PX);

    if (resizeHandler != null) {
      resizeHandler.removeHandler();
      resizeHandler = null;
    }

    super.hide();
  }

  @Override
  public void showItems(List<SearchResultDto> items) {
    // Hide popup if it is nothing to show
    if (items.isEmpty()) {
      if (suggestionsContainer == null) {
        return;
      }
      suggestionsContainer.getElement().setInnerHTML("");
      suggestionsContainer.removeFromParent();
      suggestionsPanel.setVisible(false);

      suggestionsPanel.getElement().getStyle().setWidth(400, Style.Unit.PX);
      suggestionsPanel.getElement().getStyle().setHeight(20, Style.Unit.PX);

      return;
    }

    // Show popup
    suggestionsPanel.setVisible(true);
    suggestionsContainer = new HTML();
    suggestionsContainer.addStyleName(style.noborder());
    suggestionsPanel.add(suggestionsContainer);

    // Create and show list of items
    final TableElement itemHolder = Elements.createTableElement();
    suggestionsContainer.getElement().appendChild(((com.google.gwt.dom.client.Element) itemHolder));
    if (list != null) {
      list.asWidget().removeFromParent();
    }
    list =
        SimpleList.create(
            suggestionsContainer.getElement().cast(),
            (Element) suggestionsContainer.getElement(),
            itemHolder,
            resources.defaultSimpleListCss(),
            listItemRenderer,
            eventDelegate);
    list.render(items);
    list.getSelectionModel().setSelectedItem(0);

    // Update popup position
    updatePositionAndSize();
  }

  @Override
  public String getFileName() {
    return fileName.getText();
  }

  private void updatePositionAndSize() {
    // Update position
    setPopupPosition(
        (com.google.gwt.user.client.Window.getClientWidth() / 2) - (getOffsetWidth() / 2),
        (com.google.gwt.user.client.Window.getClientHeight() / 4) - (getOffsetHeight() / 2));

    // Exit if suggestions is not shown
    if (!suggestionsPanel.isVisible()) {
      return;
    }

    // Update popup width
    int width = suggestionsContainer.getElement().getFirstChildElement().getOffsetWidth();
    int newWidth = Window.getClientWidth() - getElement().getAbsoluteLeft() - 50;
    if (width < newWidth) {
      newWidth = width;
    }
    suggestionsPanel.getElement().getStyle().setWidth(newWidth, Style.Unit.PX);

    // Update popup height
    int height = suggestionsContainer.getElement().getFirstChildElement().getOffsetHeight();
    int newHeight = height > 300 ? 300 : height;
    int bottom = getElement().getAbsoluteTop() + getElement().getOffsetHeight();

    if (bottom + newHeight > Window.getClientHeight() - 10) {
      newHeight = Window.getClientHeight() - 10 - bottom;
    }

    if (newHeight < 50) {
      newHeight = 50;
    }

    suggestionsPanel.getElement().getStyle().setHeight(newHeight, Style.Unit.PX);
  }

  private final SimpleList.ListEventDelegate<SearchResultDto> eventDelegate =
      new SimpleList.ListEventDelegate<SearchResultDto>() {
        @Override
        public void onListItemClicked(Element listItemBase, SearchResultDto itemData) {
          list.getSelectionModel().setSelectedItem(itemData);
        }

        @Override
        public void onListItemDoubleClicked(Element listItemBase, SearchResultDto itemData) {
          delegate.onFileSelected(Path.valueOf(itemData.getItemReference().getPath()));
        }
      };

  @UiHandler("fileName")
  void handleKeyDown(KeyDownEvent event) {
    int nativeKeyCode = event.getNativeKeyCode();
    switch (nativeKeyCode) {
      case KEY_UP:
        event.stopPropagation();
        event.preventDefault();
        if (list != null) {
          list.getSelectionModel().selectPrevious();
        }
        break;

      case KEY_DOWN:
        event.stopPropagation();
        event.preventDefault();
        if (list != null) {
          list.getSelectionModel().selectNext();
        }
        break;

      case KEY_PAGEUP:
        event.stopPropagation();
        event.preventDefault();
        if (list != null) {
          list.getSelectionModel().selectPreviousPage();
        }
        break;

      case KEY_PAGEDOWN:
        event.stopPropagation();
        event.preventDefault();
        if (list != null) {
          list.getSelectionModel().selectNextPage();
        }
        break;

      case KEY_ENTER:
        event.stopPropagation();
        event.preventDefault();
        SearchResultDto selectedItem = list.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
          delegate.onFileSelected(Path.valueOf(selectedItem.getItemReference().getPath()));
        }
        break;

      case KEY_ESCAPE:
        event.stopPropagation();
        event.preventDefault();
        hidePopup();
        break;
      default:
        // here need some delay to be sure input box initiated with given value
        // in manually testing hard to reproduce this problem but it reproduced with selenium tests
        new Timer() {
          @Override
          public void run() {
            String fileName = NavigateToFileViewImpl.this.fileName.getText();
            if (Strings.isNullOrEmpty(fileName)) {
              showItems(Collections.emptyList());
              return;
            }
            if (!fileName.equalsIgnoreCase(previosFileName)) {
              previosFileName = fileName;
              delegate.onFileNameChanged(fileName);
            }
          }
        }.schedule(300);
        break;
    }
  }
}
