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
package org.eclipse.che.ide.projecttype.wizard.categoriespage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardResources;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.Category;
import org.eclipse.che.ide.ui.list.CategoryRenderer;
import org.vectomatic.dom.svg.ui.SVGImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Evgen Vidolob
 * @author Oleksii Orel
 */
public class CategoriesPageViewImpl implements CategoriesPageView {

    private static MainPageViewImplUiBinder ourUiBinder = GWT.create(MainPageViewImplUiBinder.class);
    private final DockLayoutPanel rootElement;
    private final Category.CategoryEventDelegate<ProjectTemplateDescriptor> templateCategoryEventDelegate    =
            new Category.CategoryEventDelegate<ProjectTemplateDescriptor>() {
                @Override
                public void onListItemClicked(Element listItemBase, ProjectTemplateDescriptor itemData) {
                    selectNextWizardType(itemData);
                }
            };
    private final Category.CategoryEventDelegate<ProjectTypeDto>            projectTypeCategoryEventDelegate =
            new Category.CategoryEventDelegate<ProjectTypeDto>() {
                @Override
                public void onListItemClicked(Element listItemBase, ProjectTypeDto itemData) {
                    selectNextWizardType(itemData);
                }
            };
    private final CategoryRenderer<ProjectTypeDto>                          projectTypeCategoryRenderer      =
            new CategoryRenderer<ProjectTypeDto>() {
                @Override
                public void renderElement(Element element, ProjectTypeDto data) {
                    element.setInnerText(data.getDisplayName());
                }

                @Override
                public Element renderCategory(Category<ProjectTypeDto> category) {
                    return renderCategoryHeader(category.getTitle());
                }
            };
    private final CategoryRenderer<ProjectTemplateDescriptor>               templateCategoryRenderer         =
            new CategoryRenderer<ProjectTemplateDescriptor>() {
                @Override
                public void renderElement(Element element, ProjectTemplateDescriptor data) {
                    element.setInnerText(data.getDisplayName());
                }

                @Override
                public Element renderCategory(Category<ProjectTemplateDescriptor> category) {
                    return renderCategoryHeader(category.getTitle());
                }
            };
    private final IconRegistry iconRegistry;

    @UiField(provided = true)
    Style       styles;
    @UiField
    SimplePanel categoriesPanel;
    @UiField
    HTMLPanel   descriptionArea;
    @UiField
    Label       configurationAreaText;
    @UiField
    HTMLPanel   configurationArea;
    @UiField
    Label       projectType;
    @UiField
    TextBox     projectName;
    @UiField
    TextArea    projectDescription;
    @UiField
    TextBox     parentDirectory;

    private ActionDelegate                              delegate;
    private Map<String, Set<ProjectTypeDto>>            typesByCategory;
    private Map<String, Set<ProjectTemplateDescriptor>> templatesByCategory;
    private Resources                                   resources;
    private CategoriesList                              categoriesList;
    private List<ProjectTypeDto>                        availableProjectTypes;

    @Inject
    public CategoriesPageViewImpl(Resources resources,
                                  IconRegistry iconRegistry,
                                  ProjectWizardResources wizardResources) {
        this.resources = resources;
        this.iconRegistry = iconRegistry;
        styles = wizardResources.mainPageStyle();
        styles.ensureInjected();
        rootElement = ourUiBinder.createAndBindUi(this);
        reset();

        projectName.getElement().setAttribute("placeholder", "Define the name of your project...");
        projectName.getElement().setAttribute("maxlength", "128");
        projectName.getElement().setAttribute("spellcheck", "false");
        projectDescription.getElement().setAttribute("placeholder", "Add a description to your project...");
        projectDescription.getElement().setAttribute("maxlength", "256");
        setConfigOptions(null);
    }

    @UiHandler("projectName")
    void onProjectNameChanged(KeyUpEvent event) {
        final String name = projectName.getValue();
        if (name == null) {
            delegate.projectNameChanged("");
            return;
        }

        delegate.projectNameChanged(name.replaceAll("\\s", "_"));
    }

    @UiHandler("projectDescription")
    void onProjectDescriptionChanged(KeyUpEvent event) {
        final String description = projectDescription.getValue();
        if (description == null) {
            delegate.projectDescriptionChanged("");
            return;
        }

        delegate.projectDescriptionChanged(description);
    }

    @UiHandler("parentDirectory")
    void onParentDirectoryChanged(KeyUpEvent event) {
        delegate.onParentDirectoryChanged();
    }

    private void selectNextWizardType(Object itemData) {
        if (itemData instanceof ProjectTemplateDescriptor) {
            delegate.projectTemplateSelected((ProjectTemplateDescriptor)itemData);
            descriptionArea.getElement().setInnerText(((ProjectTemplateDescriptor)itemData).getDescription());
            projectType.setText(((ProjectTemplateDescriptor)itemData).getDisplayName());
        } else if (itemData instanceof ProjectTypeDto) {
            delegate.projectTypeSelected((ProjectTypeDto)itemData);
            descriptionArea.getElement().setInnerText(((ProjectTypeDto)itemData).getDisplayName());
            projectType.setText(((ProjectTypeDto)itemData).getDisplayName());
        } else {
            projectType.setText("");
            descriptionArea.getElement().setInnerText("");
            configurationArea.getElement().setInnerText("");
        }
    }

    private Element renderCategoryHeader(String category) {
        SpanElement categoryElement = Document.get().createSpanElement();
        categoryElement.setClassName(resources.defaultCategoriesListCss().headerText());

        SpanElement iconElement = Document.get().createSpanElement();
        categoryElement.appendChild(iconElement);

        SpanElement textElement = Document.get().createSpanElement();
        categoryElement.appendChild(textElement);
        textElement.setInnerText(category);

        Icon icon = iconRegistry.getIconIfExist(category + ".samples.category.icon");
        if (icon != null) {
            final SVGImage iconSVG = icon.getSVGImage();
            if (iconSVG != null) {
                iconElement.appendChild(iconSVG.getElement());
                return categoryElement;
            }
        }

        return categoryElement;
    }

    @Override
    public void setConfigOptions(List<String> options) {
        StringBuilder optionsHTMLBuilder = new StringBuilder();
        if (options != null) {
            for (String option : options) {
                if (option != null && option.length() > 0) {
                    optionsHTMLBuilder.append("<p>- ").append(option).append("</p>\n");
                }
            }
        }
        if (optionsHTMLBuilder.length() > 0) {
            configurationArea.getElement().setInnerHTML(optionsHTMLBuilder.toString());
            configurationAreaText.setVisible(true);
            configurationArea.setVisible(true);
        } else {
            configurationAreaText.setVisible(false);
            configurationArea.setVisible(false);
            configurationArea.getElement().setInnerText("");
        }
    }

    @Override
    public void setName(String name) {
        focusName();
        if (name == null) {
            projectName.setValue("", false);
            delegate.projectNameChanged("");
            return;
        }
        projectName.setValue(name, false);
        delegate.projectNameChanged(name.replaceAll("\\s", "_"));
    }

    @Override
    public String getParentDirectory() {
        return parentDirectory.getText();
    }

    @Override
    public String getName() {
        return projectName.getText();
    }

    @Override
    public void setParentDirectory(String parentDirectory) {
        this.parentDirectory.setText(parentDirectory);
    }

    /** {@inheritDoc} */
    @Override
    public void setDescription(String description) {
        projectDescription.setValue(description);
    }

    @Override
    public void removeNameError() {
        projectName.removeStyleName(styles.inputError());
    }

    @Override
    public void showNameError() {
        projectName.addStyleName(styles.inputError());
    }

    @Override
    public void focusName() {
        new Timer() {
            @Override
            public void run() {
                projectName.setFocus(true);
            }
        }.schedule(300);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public void selectProjectType(final String projectTypeId) {
        ProjectTypeDto typeDescriptor = null;
        for (Entry<String, Set<ProjectTypeDto>> entry : typesByCategory.entrySet()) {
            for (ProjectTypeDto typeDefinition : entry.getValue()) {
                if (typeDefinition.getId().equals(projectTypeId)) {
                    typeDescriptor = typeDefinition;
                    break;
                }
            }
            if (typeDescriptor != null) {
                break;
            }
        }

        if (typeDescriptor != null) {
            for (ProjectTypeDto existingProjectTypeDescriptor : availableProjectTypes) {
                if (existingProjectTypeDescriptor.getId().equals(typeDescriptor.getId())) {
                    categoriesList.selectElement(typeDescriptor);
                    selectNextWizardType(typeDescriptor);
                }
            }
        }
        projectName.setFocus(true);
    }

    @Override
    public void setProjectTypes(List<ProjectTypeDto> availableProjectTypes) {
        this.availableProjectTypes = availableProjectTypes;
    }

    @Override
    public void setCategories(Map<String, Set<ProjectTypeDto>> typesByCategory,
                              Map<String, Set<ProjectTemplateDescriptor>> templatesByCategory) {
        this.typesByCategory = typesByCategory;
        this.templatesByCategory = templatesByCategory;
    }

    @Override
    public void updateCategories(boolean includeTemplates) {
        List<Category<?>> categories = new ArrayList<>();

        for (Entry<String, Set<ProjectTypeDto>> entry : typesByCategory.entrySet()) {
            final Set<ProjectTypeDto> projectTypes = entry.getValue();

            List<ProjectTypeDto> projectTypeDescriptors = new ArrayList<>();
            projectTypeDescriptors.addAll(projectTypes);
            Collections.sort(projectTypeDescriptors, new Comparator<ProjectTypeDto>() {
                @Override
                public int compare(ProjectTypeDto o1, ProjectTypeDto o2) {
                    return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
                }
            });
            categories.add(new Category<>(entry.getKey(),
                                          projectTypeCategoryRenderer,
                                          projectTypeDescriptors,
                                          projectTypeCategoryEventDelegate));
        }

        // Sort project type categories only. Project templates categories should be in the end.
        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(Category o1, Category o2) {
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
        });

        if (includeTemplates) {
            for (Entry<String, Set<ProjectTemplateDescriptor>> entry : templatesByCategory.entrySet()) {
                final Set<ProjectTemplateDescriptor> projectTemplates = entry.getValue();

                List<ProjectTemplateDescriptor> templateDescriptors = new ArrayList<>();
                templateDescriptors.addAll(projectTemplates);
                Collections.sort(templateDescriptors, new Comparator<ProjectTemplateDescriptor>() {
                    @Override
                    public int compare(ProjectTemplateDescriptor o1, ProjectTemplateDescriptor o2) {
                        return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
                    }
                });
                categories.add(new Category<>(entry.getKey(),
                                              templateCategoryRenderer,
                                              templateDescriptors,
                                              templateCategoryEventDelegate));
            }
        }

        categoriesList.render(categories);
    }

    @Override
    public void reset() {
        categoriesPanel.clear();
        categoriesList = new CategoriesList(resources);
        categoriesPanel.add(categoriesList);

        projectName.setText("");
        projectDescription.setText("");
        descriptionArea.getElement().setInnerHTML("");
        configurationArea.getElement().setInnerText("");

        focusName();
    }

    interface MainPageViewImplUiBinder extends UiBinder<DockLayoutPanel, CategoriesPageViewImpl> {
    }

    public interface Style extends CssResource {
        String mainPanel();

        String leftPart();

        String rightPart();

        String namePanel();

        String labelPosition();

        String inputFieldPosition();

        String categories();

        String description();

        String configuration();

        String label();

        String labelTitle();

        String inputError();
    }
}
