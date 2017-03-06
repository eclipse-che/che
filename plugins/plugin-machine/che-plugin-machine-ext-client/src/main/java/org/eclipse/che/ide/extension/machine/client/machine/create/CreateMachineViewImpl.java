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
package org.eclipse.che.ide.extension.machine.client.machine.create;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.TableCellElement;
import elemental.html.TableElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.api.autocomplete.AutoCompleteResources;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.toolbar.ToolbarResources;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.dom.client.Style.Overflow.AUTO;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_DOWN;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_LEFT;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_RIGHT;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_UP;

/**
 * The implementation of {@link CreateMachineView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CreateMachineViewImpl extends Window implements CreateMachineView {

    private static final CreateMachineViewImplUiBinder UI_BINDER = GWT.create(CreateMachineViewImplUiBinder.class);

    private final MachineResources              machineResources;
    private final org.eclipse.che.ide.Resources coreResources;
    private final AutoCompleteResources.Css     css;
    private final SimpleList.ListItemRenderer<RecipeDescriptor> listItemRenderer =
            new SimpleList.ListItemRenderer<RecipeDescriptor>() {
                @Override
                public void render(Element itemElement, RecipeDescriptor itemData) {
                    final TableCellElement icon = Elements.createTDElement(css.proposalIcon());
                    final TableCellElement label = Elements.createTDElement(css.proposalLabel());
                    final TableCellElement group = Elements.createTDElement(css.proposalGroup());

                    final SVGImage image = new SVGImage(machineResources.recipe());
                    image.getElement().setAttribute("class", toolbarResources.toolbar().iconButtonIcon());
                    image.getElement().getStyle().setMargin(0, Style.Unit.PX);
                    icon.appendChild((Node)image.getElement());

                    label.setInnerHTML(itemData.getName());
                    group.setInnerHTML(itemData.getType());

                    itemElement.appendChild(icon);
                    itemElement.appendChild(label);
                    itemElement.appendChild(group);
                }

                @Override
                public Element createElement() {
                    return Elements.createTRElement();
                }
            };

    private final PopupPanel popupPanel;

    @UiField(provided = true)
    MachineLocalizationConstant localizationConstant;
    @UiField
    TextBox                     machineName;
    @UiField
    TextBox                     recipeURL;
    @UiField
    Label                       errorHint;
    @UiField
    TextBox                     tags;
    @UiField
    Label                       noRecipeHint;

    private ToolbarResources    toolbarResources;

    private SimpleList<RecipeDescriptor> list;
    private ActionDelegate               delegate;
    private final SimpleList.ListEventDelegate<RecipeDescriptor> eventDelegate = new SimpleList.ListEventDelegate<RecipeDescriptor>() {
        @Override
        public void onListItemClicked(Element listItemBase, RecipeDescriptor itemData) {
            list.getSelectionModel().setSelectedItem(itemData);
        }

        @Override
        public void onListItemDoubleClicked(Element listItemBase, RecipeDescriptor itemData) {
            delegate.onRecipeSelected(itemData);
            popupPanel.hide();
            tags.setFocus(true);
        }
    };

    private Button createButton;
    private Button replaceButton;
    private Button cancelButton;

    @Inject
    public CreateMachineViewImpl(MachineLocalizationConstant localizationConstant,
                                 MachineResources machineResources,
                                 org.eclipse.che.ide.Resources coreResources,
                                 AutoCompleteResources autoCompleteResources,
                                 ToolbarResources toolbarResources) {
        this.localizationConstant = localizationConstant;
        this.machineResources = machineResources;
        this.coreResources = coreResources;
        this.toolbarResources = toolbarResources;

        css = autoCompleteResources.autocompleteComponentCss();
        popupPanel = new PopupPanel();

        setWidget(UI_BINDER.createAndBindUi(this));
        setTitle(localizationConstant.viewCreateMachineTitle());

        machineName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                delegate.onNameChanged();
            }
        });

        recipeURL.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                delegate.onRecipeUrlChanged();
            }
        });

        createFooterButtons();
    }

    private void createFooterButtons() {
        createButton = createButton(localizationConstant.viewCreateMachineButtonCreate(), "window-create-machine-create",
                                    new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            delegate.onCreateClicked();
                                        }
                                    });

        replaceButton = createButton(localizationConstant.viewCreateMachineButtonReplace(), "window-create-machine-replace",
                                     new ClickHandler() {
                                         @Override
                                         public void onClick(ClickEvent event) {
                                             delegate.onReplaceDevMachineClicked();
                                         }
                                     });

        cancelButton = createButton(localizationConstant.cancelButton(), "window-create-machine-cancel",
                                    new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            delegate.onCancelClicked();
                                        }
                                    });

        addButtonToFooter(createButton);
        addButtonToFooter(replaceButton);
        addButtonToFooter(cancelButton);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show();

        new Timer() {
            @Override
            public void run() {
                machineName.setFocus(true);
            }
        }.schedule(300);
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    protected void onClose() {
        super.onClose();
        popupPanel.hide();
    }

    @Override
    public String getMachineName() {
        return machineName.getValue();
    }

    @Override
    public void setMachineName(String name) {
        machineName.setValue(name);
        delegate.onNameChanged();
    }

    @Override
    public String getRecipeURL() {
        return recipeURL.getValue();
    }

    @Override
    public void setRecipeURL(String url) {
        recipeURL.setValue(url);
        recipeURL.setTitle(url);

        delegate.onRecipeUrlChanged();
    }

    @Override
    public void setErrorHint(boolean show) {
        errorHint.setVisible(show);
    }

    @Override
    public List<String> getTags() {
        final List<String> tagList = new ArrayList<>();

        for (String tag : tags.getValue().split(" ")) {
            if (!tag.isEmpty()) {
                tagList.add(tag.trim());
            }
        }

        return tagList;
    }

    @Override
    public void setTags(String tags) {
        this.tags.setValue(tags);
    }

    @Override
    public void setNoRecipeHint(boolean show) {
        noRecipeHint.setVisible(show);
    }

    @Override
    public void setRecipes(List<RecipeDescriptor> recipes) {
        if (recipes.isEmpty()) {
            popupPanel.hide();
            return;
        }

        popupPanel.clear();

        final TableElement itemHolder = Elements.createTableElement();
        itemHolder.setClassName(css.items());

        final HTML html = new HTML();
        html.setStyleName(css.container());
        html.getElement().getStyle().setOverflow(AUTO);
        html.getElement().appendChild(((com.google.gwt.dom.client.Element)itemHolder));
        ((Element)html.getElement()).getStyle().setProperty("max-height", "200px");

        final HTML container = new HTML();
        container.getElement().appendChild(html.getElement());

        list = SimpleList.create((SimpleList.View)container.getElement().cast(),
                                 (Element)html.getElement(),
                                 itemHolder,
                                 coreResources.defaultSimpleListCss(),
                                 listItemRenderer,
                                 eventDelegate);

        list.render(recipes);

        popupPanel.add(container);
        popupPanel.setWidth(tags.getOffsetWidth() - 10 + "px");
        popupPanel.setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                popupPanel.setPopupPosition(tags.getAbsoluteLeft(), tags.getAbsoluteTop() + tags.getOffsetHeight());
            }
        });

        list.getSelectionModel().setSelectedItem(0);
    }

    @Override
    public void setCreateButtonState(boolean enabled) {
        createButton.setEnabled(enabled);
    }

    @Override
    public void setReplaceButtonState(boolean enabled) {
        replaceButton.setEnabled(enabled);
    }

    @UiHandler("tags")
    void handleKeyDown(KeyDownEvent event) {
        switch (event.getNativeKeyCode()) {
            case KEY_UP:
                if (popupPanel.isShowing()) {
                    event.preventDefault();

                    if (list.getSelectionModel().getSelectedIndex() == 0) {
                        list.getSelectionModel().setSelectedItem(list.getSelectionModel().size() - 1);
                    } else {
                        list.getSelectionModel().selectPrevious();
                    }
                }
                break;
            case KEY_DOWN:
                if (popupPanel.isShowing()) {
                    event.preventDefault();

                    if (list.getSelectionModel().getSelectedIndex() == list.getSelectionModel().size() - 1) {
                        list.getSelectionModel().setSelectedItem(0);
                    } else {
                        list.getSelectionModel().selectNext();
                    }
                }
                break;
            case KEY_ENTER:
                if (popupPanel.isShowing()) {
                    delegate.onRecipeSelected(list.getSelectionModel().getSelectedItem());
                    popupPanel.hide();
                }
                break;
            case KEY_ESCAPE:
                if (popupPanel.isShowing()) {
                    popupPanel.hide();
                }
                break;
            case KEY_LEFT:
            case KEY_RIGHT:
                break;
            default:
                Scheduler.get().scheduleDeferred(new Command() {
                    @Override
                    public void execute() {
                        delegate.onTagsChanged();
                    }
                });
                break;
        }
    }

    interface CreateMachineViewImplUiBinder extends UiBinder<Widget, CreateMachineViewImpl> {
    }
}
