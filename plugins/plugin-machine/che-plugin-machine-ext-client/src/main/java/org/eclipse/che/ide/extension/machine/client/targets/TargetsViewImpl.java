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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.events.KeyboardEvent;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandResources;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.listbox.CustomListBox;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class TargetsViewImpl extends Window implements TargetsView {

    interface TargetsViewImplUiBinder extends UiBinder<Widget, TargetsViewImpl> {
    }

    private EditCommandResources    commandResources;
    private IconRegistry            iconRegistry;
    private ActionDelegate          delegate;

    @UiField(provided = true)
    MachineLocalizationConstant     machineLocale;

    @UiField
    TextBox                         filterTargets;

    @UiField
    SimplePanel                     targetsPanel;

    private CategoriesList          list;

    @UiField
    FlowPanel                       hintPanel;

    @UiField
    FlowPanel                       infoPanel;

    @UiField
    FlowPanel                       propertiesPanel;

    @UiField
    TextBox                         targetName;

    @UiField
    CustomListBox                   architectureListBox;

    @UiField
    TextBox                         host;

    @UiField
    TextBox                         port;

    @UiField
    TextBox                         userName;

    @UiField
    PasswordTextBox                 password;

    @UiField
    FlowPanel                       operationPanel;

    @UiField
    FlowPanel                       footer;

    private Button                  closeButton;

    private Button                  saveButton;
    private Button                  cancelButton;
    private Button                  connectButton;

    @Inject
    public TargetsViewImpl(org.eclipse.che.ide.Resources resources,
                           MachineLocalizationConstant machineLocale,
                           CoreLocalizationConstant coreLocale,
                           EditCommandResources commandResources,
                           IconRegistry iconRegistry,
                           TargetsViewImplUiBinder uiBinder) {
        this.machineLocale = machineLocale;
        this.commandResources = commandResources;
        this.iconRegistry = iconRegistry;

        setWidget(uiBinder.createAndBindUi(this));
        getWidget().getElement().getStyle().setPadding(0, Style.Unit.PX);
        setTitle(machineLocale.targetsViewTitle());

        filterTargets.getElement().setAttribute("placeholder", machineLocale.editCommandsViewPlaceholder());
        filterTargets.getElement().addClassName(commandResources.getCss().filterPlaceholder());

        list = new CategoriesList(resources);
        list.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                switch (event.getNativeKeyCode()) {
                    case KeyboardEvent.KeyCode.INSERT:
                        break;
                    case KeyboardEvent.KeyCode.DELETE:
                        break;
                }
            }
        }, KeyDownEvent.getType());
        targetsPanel.add(list);

        architectureListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                delegate.onArchitectureChanged(architectureListBox.getValue());
            }
        });

        closeButton = createButton(coreLocale.close(), "targets.button.close",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        delegate.onCloseClicked();
                    }
                });
        addButtonToFooter(closeButton);

        saveButton = createButton(coreLocale.save(), "targets.button.save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSaveClicked();
            }
        });
        saveButton.addStyleName(this.resources.windowCss().primaryButton());
        footer.add(saveButton);

        cancelButton = createButton(coreLocale.cancel(), "targets.button.cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        footer.add(cancelButton);

        connectButton = createButton("Connect", "targets.button.connect", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onConnectClicked();
            }
        });
        connectButton.addStyleName(this.resources.windowCss().primaryButton());
        connectButton.addStyleName(resources.Css().buttonLoader());

        operationPanel.add(connectButton);
        operationPanel.getElement().insertFirst(connectButton.getElement());

        targetName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onTargetNameChanged(targetName.getValue());
            }
        });

        host.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onHostChanged(host.getValue());
            }
        });

        port.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onPortChanged(port.getValue());
            }
        });

        userName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onUserNameChanged(userName.getValue());
            }
        });

        password.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onPasswordChanged(password.getValue());
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setAvailableArchitectures(List<String> architectures) {
        architectureListBox.clear();
        for (String architecture : architectures) {
            architectureListBox.addItem(architecture);
        }
    }

    @Override
    public void clear() {
        list.clear();

        hintPanel.setVisible(true);
        infoPanel.setVisible(false);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void showHintPanel() {
        hintPanel.setVisible(true);
        infoPanel.setVisible(false);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void showInfoPanel() {
        hintPanel.setVisible(false);
        infoPanel.setVisible(true);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void showPropertiesPanel() {
        hintPanel.setVisible(false);
        infoPanel.setVisible(false);
        propertiesPanel.setVisible(true);
    }

    @Override
    public void showTargets(List<Target> targets) {
        Collections.sort(targets, new Comparator<Target>() {
            @Override
            public int compare(Target target1, Target target2) {
                return target1.getName().compareTo(target2.getName());
            }
        });

        HashMap<String, List<Target>> categories = new HashMap<>();
        for (Target target : targets) {
            List<Target> categoryTargets = categories.get(target.getType());
            if (categoryTargets == null) {
                categoryTargets = new ArrayList<>();
                categories.put(target.getType(), categoryTargets);
            }
            categoryTargets.add(target);
        }

        List<Category<?>> categoriesList = new ArrayList<>();
        for (Map.Entry<String, List<Target>> entry : categories.entrySet()) {
            categoriesList.add(new Category<>(entry.getKey(), categoriesRenderer, entry.getValue(), categoriesEventDelegate));
        }

        ensureSSHCategoryExists(categoriesList);

        list.clear();
        list.render(categoriesList);
    }

    @Override
    public void selectTarget(Target target) {
        list.selectElement(target);
    }

    private void ensureSSHCategoryExists(List<Category<?>> categoriesList) {
        for (Category<?> category : categoriesList) {
            if ("ssh".equalsIgnoreCase(category.getTitle())) {
                return;
            }
        }

        categoriesList.add(new Category<>("ssh", categoriesRenderer, new ArrayList<Target>(), categoriesEventDelegate));
    }

    private SpanElement renderCategoryHeader(final Category<Target> category) {
        SpanElement categoryHeaderElement = Document.get().createSpanElement();
        categoryHeaderElement.setClassName(commandResources.getCss().categoryHeader());

        SpanElement iconElement = Document.get().createSpanElement();
        iconElement.getStyle().setPaddingRight(4, Style.Unit.PX);
        iconElement.getStyle().setPaddingLeft(2, Style.Unit.PX);
        categoryHeaderElement.appendChild(iconElement);

        Icon icon = iconRegistry.getIconIfExist(category.getTitle() + ".runtime.icon");
        if (icon != null) {
            iconElement.appendChild(icon.getSVGImage().getElement());
        }

        SpanElement textElement = Document.get().createSpanElement();
        categoryHeaderElement.appendChild(textElement);
        textElement.setInnerText(category.getTitle());

        if (!"docker".equalsIgnoreCase(category.getTitle())) {
            // Add button to create a target
            SpanElement buttonElement = Document.get().createSpanElement();
            buttonElement.appendChild(commandResources.addCommandButton().getSvg().getElement());
            categoryHeaderElement.appendChild(buttonElement);

            Event.sinkEvents(buttonElement, Event.ONCLICK);
            Event.setEventListener(buttonElement, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    event.stopPropagation();
                    event.preventDefault();
                    delegate.onAddTarget(category.getTitle());
                }
            });
        } else {
            // Add empty span for properly aligning items
            categoryHeaderElement.appendChild(Document.get().createSpanElement());
        }

        return categoryHeaderElement;
    }

    private final CategoryRenderer<Target> categoriesRenderer =
            new CategoryRenderer<Target>() {
                @Override
                public void renderElement(Element element, final Target target) {
                    element.setInnerText(target.getName());

                    element.addClassName(commandResources.getCss().categorySubElementHeader());
                    element.setId("target-" + target.getName());

                    if (target.getRecipe() == null) {
                        element.getStyle().setProperty("color", "gray");
                    } else {
                        if (target.isConnected()) {
                            DivElement running = Document.get().createDivElement();
                            running.setClassName(commandResources.getCss().running());
                            element.appendChild(running);

                            Tooltip.create((elemental.dom.Element) running,
                                    BOTTOM,
                                    MIDDLE,
                                    "Connected");
                        }

                        SpanElement categorySubElement = Document.get().createSpanElement();
                        categorySubElement.setClassName(commandResources.getCss().buttonArea());
                        element.appendChild(categorySubElement);

                        SpanElement removeCommandButtonElement = Document.get().createSpanElement();
                        categorySubElement.appendChild(removeCommandButtonElement);
                        removeCommandButtonElement.appendChild(commandResources.removeCommandButton().getSvg().getElement());
                        Event.sinkEvents(removeCommandButtonElement, Event.ONCLICK);
                        Event.setEventListener(removeCommandButtonElement, new EventListener() {
                            @Override
                            public void onBrowserEvent(Event event) {
                                if (Event.ONCLICK == event.getTypeInt()) {
                                    event.stopPropagation();
                                    event.preventDefault();
                                    delegate.onDeleteTarget(target);
                                }
                            }
                        });
                    }
                }

                @Override
                public SpanElement renderCategory(Category<Target> category) {
                    return renderCategoryHeader(category);
                }
            };

    private final Category.CategoryEventDelegate<Target> categoriesEventDelegate =
            new Category.CategoryEventDelegate<Target>() {
                @Override
                public void onListItemClicked(Element listItemBase, Target target) {
                    delegate.onTargetSelected(target);
                }
            };

    @Override
    public void setTargetName(String targetName) {
        this.targetName.setValue(targetName);
    }

    @Override
    public String getTargetName() {
        return targetName.getValue();
    }

    @Override
    public void setArchitecture(String architecture) {
        architectureListBox.select(architecture);
    }

    @Override
    public String getArchitecture() {
        return architectureListBox.getValue();
    }

    @Override
    public void setHost(String host) {
        this.host.setValue(host);
    }

    @Override
    public String getHost() {
        return host.getValue();
    }

    @Override
    public void setPort(String port) {
        this.port.setValue(port);
    }

    @Override
    public String getPort() {
        return port.getValue();
    }

    @Override
    public void setUserName(String userName) {
        this.userName.setValue(userName);
    }

    @Override
    public String getUserName() {
        return userName.getValue();
    }

    @Override
    public void setPassword(String password) {
        this.password.setValue(password);
    }

    @Override
    public String getPassword() {
        return password.getValue();
    }

    @Override
    public void enableSaveButton(boolean enable) {
        saveButton.setEnabled(enable);
    }

    @Override
    public void enableCancelButton(boolean enable) {
        cancelButton.setEnabled(enable);
    }

    @Override
    public void enableConnectButton(boolean enable) {
        connectButton.setEnabled(enable);
    }

    @Override
    public void setConnectButtonText(String title) {
        if (title == null || title.isEmpty()) {
            connectButton.setText("");
            connectButton.setHTML("<i></i>");
        } else {
            connectButton.setText(title);
        }
    }

    @Override
    public void selectTargetName() {
        targetName.setFocus(true);
        targetName.selectAll();
    }

}
