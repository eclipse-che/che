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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import elemental.events.KeyboardEvent;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The implementation of {@link ProjectClasspathView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProjectClasspathViewImpl extends Window implements ProjectClasspathView {

    private static final PropertiesViewImplUiBinder UI_BINDER = GWT.create(PropertiesViewImplUiBinder.class);

    private final JavaLocalizationConstant  localization;
    private final ProjectClasspathResources commandResources;

    private Button doneButton;

    private ActionDelegate delegate;
    private CategoriesList list;

    @UiField
    SimplePanel categoriesPanel;
    @UiField
    SimplePanel contentPanel;

    @Inject
    protected ProjectClasspathViewImpl(org.eclipse.che.ide.Resources resources,
                                       JavaLocalizationConstant localization,
                                       ProjectClasspathResources commandResources) {
        this.localization = localization;
        this.commandResources = commandResources;

        Map<Object, List<Object>> categories = new HashMap<>();

        commandResources.getCss().ensureInjected();

        setWidget(UI_BINDER.createAndBindUi(this));
        setTitle(localization.projectClasspathTitle());
        getWidget().getElement().setId("classpathManagerView");

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
        categoriesPanel.add(list);

        contentPanel.clear();

        createButtons();

        getWidget().getElement().getStyle().setPadding(0, Style.Unit.PX);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show();
        super.setHideOnEscapeEnabled(true);

        doneButton.setFocus(true);
    }

    @Override
    public void hideWindow() {
        this.hide();
    }

    @Override
    public AcceptsOneWidget getConfigurationsContainer() {
        return contentPanel;
    }

    @Override
    public void setDoneButtonInFocus() {
        doneButton.setFocus(true);
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
    public boolean isDoneButtonInFocus() {
        return isWidgetFocused(doneButton);
    }

    @Override
    public void setPages(Map<String, Set<ClasspathPagePresenter>> properties) {
        List<Category<?>> categoriesList = new ArrayList<>();
        for (Map.Entry<String, Set<ClasspathPagePresenter>> entry : properties.entrySet()) {
            categoriesList.add(new Category<>(entry.getKey(),
                                              projectPropertiesRenderer,
                                              entry.getValue(),
                                              projectPropertiesDelegate));
        }

        list.render(categoriesList, true);
    }

    @Override
    public void selectPage(ClasspathPagePresenter property) {
        list.selectElement(property);
    }

    private void createButtons() {

        doneButton = createPrimaryButton(localization.buttonDone(), "window-edit-configurations-close",
                                         new ClickHandler() {
                                             @Override
                                             public void onClick(ClickEvent event) {
                                                 delegate.onDoneClicked();
                                             }
                                         });
        addButtonToFooter(doneButton);

        Element dummyFocusElement = DOM.createSpan();
        dummyFocusElement.setTabIndex(0);
        getFooter().getElement().appendChild(dummyFocusElement);
    }

    private final CategoryRenderer<ClasspathPagePresenter> projectPropertiesRenderer =
            new CategoryRenderer<ClasspathPagePresenter>() {
                @Override
                public void renderElement(Element element, ClasspathPagePresenter data) {
                    element.setInnerText(data.getTitle());
                }

                @Override
                public SpanElement renderCategory(Category<ClasspathPagePresenter> category) {
                    SpanElement spanElement = Document.get().createSpanElement();
                    spanElement.setClassName(commandResources.getCss().categoryHeader());
                    spanElement.setInnerText(category.getTitle());
                    return spanElement;
                }
            };

    private final Category.CategoryEventDelegate<ClasspathPagePresenter> projectPropertiesDelegate =
            new Category.CategoryEventDelegate<ClasspathPagePresenter>() {
                @Override
                public void onListItemClicked(Element listItemBase, ClasspathPagePresenter itemData) {
                    delegate.onConfigurationSelected(itemData);
                }
            };

    interface PropertiesViewImplUiBinder extends UiBinder<Widget, ProjectClasspathViewImpl> {
    }
}
