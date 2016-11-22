/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.hotkeys.dialog;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.hotkeys.HotKeyItem;
import org.eclipse.che.ide.hotkeys.HotKeyResources;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation {@link HotKeysDialogView}
 *
 * @author Alexander Andrienko
 * @author Artem Zatsarynnyi
 */
@Singleton
public class HotKeysDialogViewImpl extends Window implements HotKeysDialogView {

    interface KeyMapViewImplUiBinder extends UiBinder<Widget, HotKeysDialogViewImpl> {
    }

    private final HotKeyResources hotKeyResources;

    private final Category.CategoryEventDelegate<HotKeyItem> keyBindingsEventDelegate =
            new Category.CategoryEventDelegate<HotKeyItem>() {

                @Override
                public void onListItemClicked(Element listItemBase, HotKeyItem hotKeyItem) {
                }
            };

    private final CategoryRenderer<HotKeyItem> keyBindingsRenderer =
            new CategoryRenderer<HotKeyItem>() {
                @Override
                public void renderElement(Element element, HotKeyItem hotKeyItem) {
                    element.setInnerText(hotKeyItem.getActionDescription());
                    element.addClassName(hotKeyResources.css().description());

                    DivElement hotKeyElem = Document.get().createDivElement();
                    hotKeyElem.setInnerText(hotKeyItem.getHotKey());
                    hotKeyElem.addClassName(hotKeyResources.css().hotKey());
                    hotKeyElem.addClassName(hotKeyResources.css().floatRight());

                    element.appendChild(hotKeyElem);
                }

                @Override
                public SpanElement renderCategory(Category<HotKeyItem> category) {
                    SpanElement spanElement = Document.get().createSpanElement();
                    spanElement.setInnerText(category.getTitle());
                    return spanElement;
                }
            };

    private CategoriesList    list;
    private List<Category<?>> categoriesList;
    private ActionDelegate    delegate;
    private String            filteredValue;

    Button okButton;

    Button printButton;

    @UiField
    FlowPanel category;

    @UiField
    TextBox filterInput;

    @Inject
    public HotKeysDialogViewImpl(KeyMapViewImplUiBinder uiBinder,
                                 CoreLocalizationConstant locale,
                                 org.eclipse.che.ide.Resources res,
                                 HotKeyResources hotKeyResources) {
        hotKeyResources.css().ensureInjected();

        this.hotKeyResources = hotKeyResources;

        this.setTitle(locale.keyBindingsDialogTitle());
        this.setWidget(uiBinder.createAndBindUi(this));

        okButton = createButton(locale.ok(), "keybindings-okButton-btn", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                delegate.onOkClicked();
            }
        });
        addButtonToFooter(okButton);
        okButton.addStyleName(resources.windowCss().primaryButton());

        printButton = createButton(locale.print(), "keybindings-printButton-btn", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                delegate.onPrintClicked();
            }
        });
        addButtonToFooter(printButton);

        list = new CategoriesList(res);
        categoriesList = new ArrayList<>();
        category.add(list);
        filterInput.getElement().setAttribute("placeholder", "Search");
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void hide() {
        super.hide();
        resetFilter();
    }
    
    @Override
    public void renderKeybindings() {
        list.clear();
        list.render(categoriesList, true);
    }

    @Override
    public void setData(Map<String, List<HotKeyItem>> data) {
        categoriesList.clear();
        for (Map.Entry<String, List<HotKeyItem>> elem: data.entrySet()) {
            categoriesList.add(new Category<>(elem.getKey(), keyBindingsRenderer, elem.getValue(), keyBindingsEventDelegate));
        }
    }

    @UiHandler("filterInput")
    public void onKeyUp(KeyUpEvent keyUpEvent) {
        String value = filterInput.getText();
        if (!filterInput.getText().equals(filteredValue)) {
            filteredValue = value;
            delegate.onFilterValueChanged(value);
        }
    }

    private void resetFilter() {
        filterInput.setText("");
        filterInput.setFocus(true);
    }
}
