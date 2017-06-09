/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.navigation;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
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
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.autocomplete.AutoCompleteResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

import java.util.List;

/**
 * The implementation of {@link NavigateToFileView} view.
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NavigateToFileViewImpl extends PopupPanel implements NavigateToFileView {

    interface NavigateToFileViewImplUiBinder extends UiBinder<Widget, NavigateToFileViewImpl> {
    }

    @UiField
    TextBox fileName;

    @UiField(provided = true)
    CoreLocalizationConstant locale;

    private ActionDelegate      delegate;

    private final AutoCompleteResources.Css css;

    private final Resources resources;

    private SimpleList<ItemReference> list;

    @UiField
    FlowPanel suggestionsPanel;

    @UiField
    HTML suggestionsContainer;

    private HandlerRegistration resizeHandler;

    @Inject
    public NavigateToFileViewImpl(CoreLocalizationConstant locale,
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

        setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                setPopupPosition((com.google.gwt.user.client.Window.getClientWidth() / 2) - (offsetWidth / 2),
                        (com.google.gwt.user.client.Window.getClientHeight() / 4) - (offsetHeight / 2));
                // Set 'clip' css property to auto when show animation is finished.
                new Timer() {
                    @Override
                    public void run() {
                        getElement().getStyle().setProperty("clip", "auto");
                        delegate.onFileNameChanged(fileName.getText());
                    }
                }.schedule(300);
            }
        });

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                fileName.setFocus(true);
            }
        });

        // Add window resize handler
        if (resizeHandler == null) {
            resizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    updatePositionAndSize();
                }
            });
        }
    }

    private final SimpleList.ListItemRenderer<ItemReference> listItemRenderer =
            new SimpleList.ListItemRenderer<ItemReference>() {
                @Override
                public void render(Element itemElement, ItemReference itemData) {
                    TableCellElement label = Elements.createTDElement();
                    TableCellElement path = Elements.createTDElement();

                    Path itemPath = Path.valueOf(itemData.getPath());

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
        suggestionsContainer.getElement().setInnerHTML("");
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
    public void showItems(List<ItemReference> items) {
        // Hide popup if it is nothing to show
        if (items.isEmpty()) {
            suggestionsContainer.getElement().setInnerHTML("");
            suggestionsPanel.setVisible(false);

            suggestionsPanel.getElement().getStyle().setWidth(400, Style.Unit.PX);
            suggestionsPanel.getElement().getStyle().setHeight(20, Style.Unit.PX);

            return;
        }

        // Show popup
        suggestionsPanel.setVisible(true);
        suggestionsContainer.getElement().setInnerHTML("");

        // Create and show list of items
        final TableElement itemHolder = Elements.createTableElement();
        suggestionsContainer.getElement().appendChild(((com.google.gwt.dom.client.Element) itemHolder));
        list = SimpleList.create((SimpleList.View) suggestionsContainer.getElement().cast(),
                (Element)suggestionsContainer.getElement(),
                itemHolder,
                resources.defaultSimpleListCss(),
                listItemRenderer,
                eventDelegate);
        list.render(items);
        list.getSelectionModel().setSelectedItem(0);

        // Update popup position
        updatePositionAndSize();
    }

    private void updatePositionAndSize() {
        // Update position
        setPopupPosition((com.google.gwt.user.client.Window.getClientWidth() / 2) - (getOffsetWidth() / 2),
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

    private final SimpleList.ListEventDelegate<ItemReference> eventDelegate = new SimpleList.ListEventDelegate<ItemReference>() {
        @Override
        public void onListItemClicked(Element listItemBase, ItemReference itemData) {
            list.getSelectionModel().setSelectedItem(itemData);
        }

        @Override
        public void onListItemDoubleClicked(Element listItemBase, ItemReference itemData) {
            delegate.onFileSelected(Path.valueOf(itemData.getPath()));;
        }
    };

    @UiHandler("fileName")
    void handleKeyDown(KeyDownEvent event) {
        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_UP:
                event.stopPropagation();
                event.preventDefault();
                if (list != null) {
                    list.getSelectionModel().selectPrevious();
                }
                return;

            case KeyCodes.KEY_DOWN:
                event.stopPropagation();
                event.preventDefault();
                if (list != null) {
                    list.getSelectionModel().selectNext();
                }
                return;

            case KeyCodes.KEY_PAGEUP:
                event.stopPropagation();
                event.preventDefault();
                if (list != null) {
                    list.getSelectionModel().selectPreviousPage();
                }
                return;

            case KeyCodes.KEY_PAGEDOWN:
                event.stopPropagation();
                event.preventDefault();
                if (list != null) {
                    list.getSelectionModel().selectNextPage();
                }
                return;

            case KeyCodes.KEY_ENTER:
                event.stopPropagation();
                event.preventDefault();
                ItemReference selectedItem = list.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    delegate.onFileSelected(Path.valueOf(selectedItem.getPath()));;
                }
                return;

            case KeyCodes.KEY_ESCAPE:
                event.stopPropagation();
                event.preventDefault();
                hidePopup();
                return;
        }

        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                delegate.onFileNameChanged(fileName.getText());
            }
        });
    }

}
