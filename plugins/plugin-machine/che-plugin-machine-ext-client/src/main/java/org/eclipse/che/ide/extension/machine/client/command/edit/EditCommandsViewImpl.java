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
package org.eclipse.che.ide.extension.machine.client.command.edit;

import elemental.events.KeyboardEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;
import org.vectomatic.dom.svg.ui.SVGImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation of {@link EditCommandsView}.
 *
 * @author Artem Zatsarynnyi
 * @author Oleksii Orel
 */
@Singleton
public class EditCommandsViewImpl extends Window implements EditCommandsView {

    private static final EditCommandsViewImplUiBinder UI_BINDER = GWT.create(EditCommandsViewImplUiBinder.class);

    private final EditCommandResources     commandResources;
    private final CoreLocalizationConstant coreLocale;
    private final IconRegistry             iconRegistry;

    private final Label hintLabel;

    private final CategoryRenderer<CommandImpl>               commandRenderer;
    private final Category.CategoryEventDelegate<CommandImpl> commandEventDelegate;

    @UiField(provided = true)
    MachineLocalizationConstant machineLocale;
    @UiField
    TextBox                     filterInputField;
    @UiField
    SimplePanel                 categoriesPanel;
    @UiField
    FlowPanel                   namePanel;
    @UiField
    TextBox                     commandName;
    @UiField
    SimplePanel                 commandPageContainer;
    @UiField
    FlowPanel                   previewUrlPanel;
    @UiField
    TextBox                     commandPreviewUrl;
    @UiField
    FlowPanel                   buttonsPanel;

    private Button cancelButton;
    private Button saveButton;
    private Button closeButton;

    private ActionDelegate delegate;

    private CategoriesList                      categoriesList;
    private Map<CommandType, List<CommandImpl>> categories;

    private String      selectedType;
    private CommandImpl selectedCommand;

    private String filterTextValue;

    @Inject
    protected EditCommandsViewImpl(org.eclipse.che.ide.Resources resources,
                                   final EditCommandResources commandResources,
                                   MachineLocalizationConstant machineLocale,
                                   CoreLocalizationConstant coreLocale,
                                   IconRegistry iconRegistry) {
        this.commandResources = commandResources;
        this.machineLocale = machineLocale;
        this.coreLocale = coreLocale;
        this.iconRegistry = iconRegistry;

        commandRenderer = new CategoryRenderer<CommandImpl>() {
            @Override
            public void renderElement(Element element, CommandImpl data) {
                UIObject.ensureDebugId(element, "commandsManager-type-" + data.getType());

                element.addClassName(commandResources.getCss().categorySubElementHeader());
                element.setInnerText(data.getName().trim().isEmpty() ? "<none>" : data.getName());
                element.appendChild(createCommandButtons());
            }

            @Override
            public SpanElement renderCategory(Category<CommandImpl> category) {
                return createCategoryHeaderForCommandType(category.getTitle());
            }
        };

        commandEventDelegate = new Category.CategoryEventDelegate<CommandImpl>() {
            @Override
            public void onListItemClicked(Element listItemBase, CommandImpl itemData) {
                onCommandSelected(itemData);
            }
        };

        categories = new HashMap<>();

        commandResources.getCss().ensureInjected();

        setWidget(UI_BINDER.createAndBindUi(this));
        setTitle(machineLocale.editCommandsViewTitle());
        getWidget().getElement().setId("commandsManagerView");

        hintLabel = new Label(machineLocale.editCommandsViewHint());
        hintLabel.addStyleName(commandResources.getCss().hintLabel());

        filterInputField.getElement().setAttribute("placeholder", machineLocale.editCommandsViewPlaceholder());
        filterInputField.getElement().addClassName(commandResources.getCss().filterPlaceholder());

        categoriesList = new CategoriesList(resources);
        categoriesList.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                switch (event.getNativeKeyCode()) {
                    case KeyboardEvent.KeyCode.INSERT:
                        delegate.onAddClicked();
                        resetFilter();
                        break;
                    case KeyboardEvent.KeyCode.DELETE:
                        delegate.onRemoveClicked();
                        break;
                }
            }
        }, KeyDownEvent.getType());
        categoriesPanel.add(categoriesList);

        createButtons();

        getWidget().getElement().getStyle().setPadding(0, Style.Unit.PX);
    }

    /** Creates and returns buttons for duplicating and removing command. */
    private SpanElement createCommandButtons() {
        final SpanElement removeCommandButton = Document.get().createSpanElement();
        removeCommandButton.appendChild(commandResources.removeCommandButton().getSvg().getElement());
        Event.sinkEvents(removeCommandButton, Event.ONCLICK);
        Event.setEventListener(removeCommandButton, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (Event.ONCLICK == event.getTypeInt()) {
                    event.stopPropagation();
                    onCommandSelected(selectedCommand);
                    delegate.onRemoveClicked();
                }
            }
        });

        final SpanElement duplicateCommandButton = Document.get().createSpanElement();
        duplicateCommandButton.appendChild(commandResources.duplicateCommandButton().getSvg().getElement());
        Event.sinkEvents(duplicateCommandButton, Event.ONCLICK);
        Event.setEventListener(duplicateCommandButton, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (Event.ONCLICK == event.getTypeInt()) {
                    event.stopPropagation();
                    delegate.onDuplicateClicked();
                }
            }
        });

        final SpanElement buttonsPanel = Document.get().createSpanElement();
        buttonsPanel.setClassName(commandResources.getCss().buttonArea());

        buttonsPanel.appendChild(removeCommandButton);
        buttonsPanel.appendChild(duplicateCommandButton);

        return buttonsPanel;
    }

    private SpanElement createCategoryHeaderForCommandType(final String commandTypeId) {
        final SpanElement categoryHeaderElement = Document.get().createSpanElement();
        categoryHeaderElement.setClassName(commandResources.getCss().categoryHeader());

        final SpanElement iconElement = Document.get().createSpanElement();
        categoryHeaderElement.appendChild(iconElement);

        final SpanElement nameElement = Document.get().createSpanElement();
        categoryHeaderElement.appendChild(nameElement);
        final CommandType currentCommandType = getTypeById(commandTypeId);
        nameElement.setInnerText(currentCommandType != null ? currentCommandType.getDisplayName() : commandTypeId);

        final SpanElement buttonElement = Document.get().createSpanElement();
        buttonElement.appendChild(commandResources.addCommandButton().getSvg().getElement());
        categoryHeaderElement.appendChild(buttonElement);

        Event.sinkEvents(buttonElement, Event.ONCLICK);
        Event.setEventListener(buttonElement, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (Event.ONCLICK == event.getTypeInt()) {
                    event.stopPropagation();
                    namePanel.setVisible(true);
                    previewUrlPanel.setVisible(true);
                    selectedType = commandTypeId;
                    delegate.onAddClicked();
                    resetFilter();
                }
            }
        });

        final Icon icon = iconRegistry.getIconIfExist(commandTypeId + ".commands.category.icon");
        if (icon != null) {
            final SVGImage iconSVG = icon.getSVGImage();
            if (iconSVG != null) {
                iconElement.appendChild(iconSVG.getElement());
                return categoryHeaderElement;
            }
        }

        return categoryHeaderElement;
    }

    private void createButtons() {
        saveButton = createButton(coreLocale.save(), "window-edit-commands-save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSaveClicked();
            }
        });
        saveButton.addStyleName(Window.resources.windowCss().primaryButton());
        buttonsPanel.add(saveButton);

        cancelButton = createButton(coreLocale.cancel(), "window-edit-commands-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        buttonsPanel.add(cancelButton);

        closeButton = createButton(coreLocale.close(), "window-edit-commands-close",
                                   new ClickHandler() {
                                       @Override
                                       public void onClick(ClickEvent event) {
                                           delegate.onCloseClicked();
                                       }
                                   });
        closeButton.addDomHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent blurEvent) {
                //set default focus
                selectText(filterInputField.getElement());
            }
        }, BlurEvent.getType());

        addButtonToFooter(closeButton);

        Element dummyFocusElement = DOM.createSpan();
        dummyFocusElement.setTabIndex(0);
        getFooter().getElement().appendChild(dummyFocusElement);
    }

    private void resetFilter() {
        filterInputField.setText("");
        filterTextValue = "";
    }

    private CommandType getTypeById(String commandId) {
        for (CommandType type : categories.keySet()) {
            if (type.getId().equals(commandId)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Render the commands list.
     * It also takes into account the name filter and restores the selected command.
     */
    private void renderCategoriesList(Map<CommandType, List<CommandImpl>> categories) {
        final List<Category<?>> categoriesToRender = new ArrayList<>();

        for (CommandType type : categories.keySet()) {
            List<CommandImpl> commands = new ArrayList<>();
            if (filterTextValue.isEmpty()) {
                commands = categories.get(type);
            } else {  // filtering List
                for (final CommandImpl command : categories.get(type)) {
                    if (command.getName().contains(filterTextValue)) {
                        commands.add(command);
                    }
                }
            }

            Category<CommandImpl> category = new Category<>(type.getId(), commandRenderer, commands, commandEventDelegate);
            categoriesToRender.add(category);
        }

        categoriesList.clear();
        categoriesList.render(categoriesToRender, true);

        if (selectedCommand != null) {
            categoriesList.selectElement(selectedCommand);
            if (filterTextValue.isEmpty()) {
                selectText(commandName.getElement());
            }
        } else {
            resetView();
        }
    }

    @Override
    public void selectNeighborCommand(CommandImpl command) {
        final CommandImpl commandToSelect;
        final List<CommandImpl> commandsOfType = categories.get(getTypeById(command.getType()));

        int selectedCommandIndex = commandsOfType.indexOf(command);
        if (commandsOfType.size() < 2 || selectedCommandIndex == -1) {
            commandToSelect = null;
        } else {
            if (selectedCommandIndex > 0) {
                selectedCommandIndex--;
            } else {
                selectedCommandIndex++;
            }
            commandToSelect = commandsOfType.get(selectedCommandIndex);
        }

        if (commandToSelect != null) {
            selectCommand(commandToSelect);
        } else {
            resetView();
        }
    }

    /** Set the given command selected in the categories list. */
    @Override
    public void selectCommand(CommandImpl command) {
        selectedCommand = command;
        selectedType = command.getType();

        categoriesList.selectElement(command);
    }

    @Override
    public void setData(Map<CommandType, List<CommandImpl>> commandsByTypes) {
        this.categories = commandsByTypes;

        renderCategoriesList(commandsByTypes);
    }

    /** Select all text in the given inputElement. */
    private native void selectText(Element inputElement) /*-{
        inputElement.focus();
        inputElement.setSelectionRange(0, inputElement.value.length);
    }-*/;

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show();

        resetView();
        resetFilter();
    }

    /** Reset view's state to default. */
    private void resetView() {
        selectedCommand = null;
        selectedType = null;

        commandName.setText("");
        commandPreviewUrl.setText("");

        namePanel.setVisible(false);
        previewUrlPanel.setVisible(false);

        commandPageContainer.clear();
        commandPageContainer.add(hintLabel);
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public AcceptsOneWidget getCommandPageContainer() {
        return commandPageContainer;
    }

    @Override
    public void clearCommandPageContainer() {
        commandPageContainer.clear();
    }

    public String getCommandName() {
        return commandName.getText().trim();
    }

    public void setCommandName(String name) {
        commandName.setText(name);
    }

    @Override
    public String getCommandPreviewUrl() {
        return commandPreviewUrl.getText().trim();
    }

    public void setCommandPreviewUrl(String commandPreviewUrl) {
        this.commandPreviewUrl.setText(commandPreviewUrl);
    }

    @Override
    public void setPreviewUrlState(boolean enabled) {
        previewUrlPanel.setVisible(enabled);
    }

    @Override
    public void setCancelButtonState(boolean enabled) {
        cancelButton.setEnabled(enabled);
    }

    @Override
    public void setSaveButtonState(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    @Override
    public void setFilterState(boolean enabled) {
        filterInputField.setEnabled(enabled);
    }

    @Nullable
    @Override
    public String getSelectedCommandType() {
        return selectedType;
    }

    @Nullable
    @Override
    public CommandImpl getSelectedCommand() {
        return selectedCommand;
    }

    private void onCommandSelected(CommandImpl command) {
        selectedType = command.getType();
        selectedCommand = command;

        namePanel.setVisible(true);
        previewUrlPanel.setVisible(true);

        delegate.onCommandSelected(command);
    }

    @Override
    public void setCloseButtonInFocus() {
        closeButton.setFocus(true);
    }

    @UiHandler("commandName")
    public void onNameKeyUp(KeyUpEvent event) {
        delegate.onNameChanged();
    }

    @UiHandler("commandPreviewUrl")
    public void onPreviewUrlKeyUp(KeyUpEvent event) {
        delegate.onPreviewUrlChanged();
    }

    @UiHandler("filterInputField")
    public void onFilterKeyUp(KeyUpEvent event) {
        if (!filterTextValue.equals(filterInputField.getText())) {
            filterTextValue = filterInputField.getText();
            renderCategoriesList(categories);
        }
    }

    @Override
    protected void onEnterClicked() {
        delegate.onEnterClicked();
    }

    @Override
    protected void onClose() {
        delegate.onCloseClicked();
    }

    @Override
    public boolean isCancelButtonInFocus() {
        return isWidgetFocused(cancelButton);
    }

    @Override
    public boolean isCloseButtonInFocus() {
        return isWidgetFocused(closeButton);
    }

    interface EditCommandsViewImplUiBinder extends UiBinder<Widget, EditCommandsViewImpl> {
    }
}
