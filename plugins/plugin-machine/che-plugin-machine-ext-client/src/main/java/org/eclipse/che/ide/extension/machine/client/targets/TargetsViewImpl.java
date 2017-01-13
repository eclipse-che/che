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
package org.eclipse.che.ide.extension.machine.client.targets;

import elemental.events.KeyboardEvent;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandResources;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeRenderer.LABELS;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/**
 * The implementation of {@link TargetsView}.
 *
 * @author Vitaliy Guliy
 * @author Oleksii Orel
 */
@Singleton
public class TargetsViewImpl extends Window implements TargetsView {

    interface TargetsViewImplUiBinder extends UiBinder<Widget, TargetsViewImpl> {
    }

    private EditCommandResources commandResources;
    private MachineResources     machineResources;
    private IconRegistry         iconRegistry;

    private ActionDelegate delegate;

    @UiField(provided = true)
    MachineLocalizationConstant machineLocale;

    @UiField
    SimplePanel targetsPanel;

    private CategoriesList list;

    @UiField
    FlowPanel hintPanel;

    @UiField
    FlowPanel propertiesPanel;

    private Button closeButton;

    @Inject
    public TargetsViewImpl(org.eclipse.che.ide.Resources resources,
                           MachineLocalizationConstant machineLocale,
                           MachineResources machineResources,
                           CoreLocalizationConstant coreLocale,
                           EditCommandResources commandResources,
                           IconRegistry iconRegistry,
                           TargetsViewImplUiBinder uiBinder) {
        this.machineLocale = machineLocale;
        this.machineResources = machineResources;
        this.commandResources = commandResources;
        this.iconRegistry = iconRegistry;

        setWidget(uiBinder.createAndBindUi(this));
        getWidget().getElement().getStyle().setPadding(0, Style.Unit.PX);
        setTitle(machineLocale.targetsViewTitle());

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

        closeButton = createButton(coreLocale.close(), "targets.button.close",
                                   new ClickHandler() {
                                       @Override
                                       public void onClick(ClickEvent event) {
                                           delegate.onCloseClicked();
                                       }
                                   });
        addButtonToFooter(closeButton);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void clear() {
        list.clear();

        hintPanel.setVisible(true);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void showHintPanel() {
        hintPanel.setVisible(true);
        propertiesPanel.setVisible(false);
    }

    @Override
    public void setPropertiesPanel(IsWidget widget) {
        hintPanel.setVisible(false);
        propertiesPanel.setVisible(true);

        propertiesPanel.clear();
        propertiesPanel.add(widget);
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
            List<Target> categoryTargets = categories.get(target.getCategory());
            if (categoryTargets == null) {
                categoryTargets = new ArrayList<>();
                categories.put(target.getCategory(), categoryTargets);
            }
            categoryTargets.add(target);
        }

        List<Category<?>> categoriesList = new ArrayList<>();
        for (Map.Entry<String, List<Target>> entry : categories.entrySet()) {
            categoriesList.add(new Category<>(entry.getKey(), categoriesRenderer, entry.getValue(), categoriesEventDelegate));
        }

        ensureSSHCategoryExists(categoriesList);

        list.clear();
        list.render(categoriesList, true);
    }

    @Override
    public boolean selectTarget(Target target) {
        return list.selectElement(target);
    }

    private void ensureSSHCategoryExists(List<Category<?>> categoriesList) {
        for (Category<?> category : categoriesList) {
            if (machineLocale.targetsViewCategorySsh().equalsIgnoreCase(category.getTitle())) {
                return;
            }
        }

        categoriesList.add(new Category<>(machineLocale.targetsViewCategorySsh(), categoriesRenderer, new ArrayList<Target>(),
                                          categoriesEventDelegate));
    }

    private SpanElement createMachineLabel(String machineCategory) {
        final SpanElement machineLabel = Document.get().createSpanElement();

        Icon icon = iconRegistry.getIconIfExist(machineCategory + ".machine.icon");
        if (icon != null) {
            machineLabel.appendChild(icon.getSVGImage().getElement());
            return machineLabel;
        }

        if (LABELS.containsKey(machineCategory)) {
            machineLabel.setInnerText(LABELS.get(machineCategory));
            machineLabel.setClassName(this.machineResources.getCss().dockerMachineLabel());
            return machineLabel;
        }

        machineLabel.setInnerText(machineCategory.substring(0, 3));
        machineLabel.setClassName(this.machineResources.getCss().differentMachineLabel());
        return machineLabel;

    }

    private SpanElement renderCategoryHeader(final Category<Target> category) {
        SpanElement categoryHeaderElement = Document.get().createSpanElement();
        categoryHeaderElement.setClassName(commandResources.getCss().categoryHeader());
        categoryHeaderElement.appendChild(createMachineLabel(category.getTitle()));

        SpanElement textElement = Document.get().createSpanElement();
        categoryHeaderElement.appendChild(textElement);
        textElement.setInnerText(category.getTitle());

        if (machineLocale.targetsViewCategorySsh().equalsIgnoreCase(category.getTitle())) {
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
                    element.getStyle().setPaddingLeft(54, PX);
                    element.addClassName(commandResources.getCss().categorySubElementHeader());
                    element.setId("target-" + target.getName());
                    if (target.isConnected()) {
                        DivElement running = Document.get().createDivElement();
                        running.setClassName(commandResources.getCss().running());
                        element.appendChild(running);

                        Tooltip.create((elemental.dom.Element)running,
                                       BOTTOM,
                                       MIDDLE,
                                       "Connected");
                    }

                    if (target.getRecipe() == null) {
                        element.getStyle().setProperty("color", "gray");
                    }
                    if (!machineLocale.targetsViewCategoryDevelopment().equalsIgnoreCase(target.getCategory())) {
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
}
