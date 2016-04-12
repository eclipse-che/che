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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistry;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.NameUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;

/**
 * Main page for project wizard.
 *
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
public class CategoriesPagePresenter extends AbstractWizardPage<ProjectConfigDto> implements CategoriesPageView.ActionDelegate {
    private final static String DEFAULT_PARENT_DIRECTORY  = "/";
    public final static  String DEFAULT_TEMPLATE_CATEGORY = "Samples";

    private final CategoriesPageView            view;
    private final ProjectTypeRegistry           projectTypeRegistry;
    private final ProjectTemplateRegistry       projectTemplateRegistry;
    private final ProjectWizardRegistry         wizardRegistry;
    private final PreSelectedProjectTypeManager preSelectedProjectTypeManager;
    private final SelectionAgent                selectionAgent;

    private ProjectTypeSelectionListener     projectTypeSelectionListener;
    private ProjectTemplateSelectionListener projectTemplateSelectionListener;
    private ProjectTypeDto                   selectedProjectType;
    private ProjectTemplateDescriptor        selectedProjectTemplate;
    private boolean                          initialized;

    @Inject
    public CategoriesPagePresenter(CategoriesPageView view,
                                   ProjectTypeRegistry projectTypeRegistry,
                                   ProjectTemplateRegistry projectTemplateRegistry,
                                   ProjectWizardRegistry wizardRegistry,
                                   PreSelectedProjectTypeManager preSelectedProjectTypeManager,
                                   SelectionAgent selectionAgent) {
        super();
        this.view = view;
        this.projectTypeRegistry = projectTypeRegistry;
        this.projectTemplateRegistry = projectTemplateRegistry;
        this.wizardRegistry = wizardRegistry;
        this.preSelectedProjectTypeManager = preSelectedProjectTypeManager;
        this.selectionAgent = selectionAgent;

        view.setDelegate(this);
        loadProjectTypesAndTemplates();
    }

    @Override
    public void init(ProjectConfigDto dataObject) {
        super.init(dataObject);
        // this page may be reused so need to init it only once
        if (initialized) {
            return;
        }
        initialized = true;

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        if (CREATE == wizardMode) {
            // set pre-selected project type
            final String preSelectedProjectTypeId;
            if (dataObject.getType() != null) {
                preSelectedProjectTypeId = dataObject.getType();
            } else {
                preSelectedProjectTypeId = preSelectedProjectTypeManager.getPreSelectedProjectTypeId();
            }
            if (wizardRegistry.getWizardRegistrar(preSelectedProjectTypeId) != null) {
                dataObject.setType(preSelectedProjectTypeId);
            }
        }

        view.setParentDirectory(getPathToParent());
        view.updateCategories(CREATE == wizardMode);
    }

    private String getPathToParent() {
        Selection<?> selection = selectionAgent.getSelection();
        if (selection == null || selection.isEmpty()) {
            return DEFAULT_PARENT_DIRECTORY;
        }

        if (selection.getAllElements().size() > 1) {
            return DEFAULT_PARENT_DIRECTORY;
        }

        Object selectedElement = selection.getHeadElement();

        if (selectedElement instanceof FolderReferenceNode) {
            Node parent = ((FolderReferenceNode)selectedElement).getParent();
            return getPath(parent);
        } else if (selectedElement instanceof ProjectNode) {
            Node parent = ((ProjectNode)selectedElement).getParent();
            return getPath(parent);
        }

        return DEFAULT_PARENT_DIRECTORY;
    }

    @Override
    public boolean isCompleted() {
        final String projectName = dataObject.getName();
        return projectName != null && NameUtils.checkProjectName(projectName) &&
               (selectedProjectType != null || selectedProjectTemplate != null);
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(view);
        updateView();
    }

    /** Updates view from data-object. */
    private void updateView() {
        if (dataObject.getType() != null
            && (selectedProjectType == null || !selectedProjectType.getId().equals(dataObject.getType()))) {
            view.selectProjectType(dataObject.getType());
        }
        view.setName(dataObject.getName());
        view.setDescription(dataObject.getDescription());
    }

    @Override
    public void projectTypeSelected(ProjectTypeDto typeDescriptor) {
        selectedProjectType = typeDescriptor;
        selectedProjectTemplate = null;

        if (projectTypeSelectionListener != null) {
            projectTypeSelectionListener.onProjectTypeSelected(typeDescriptor);
        }
        updateDelegate.updateControls();
    }

    @Override
    public void projectTemplateSelected(ProjectTemplateDescriptor templateDescriptor) {
        selectedProjectType = null;
        selectedProjectTemplate = templateDescriptor;

        if (projectTemplateSelectionListener != null) {
            projectTemplateSelectionListener.onProjectTemplateSelected(templateDescriptor);
        }
        updateDelegate.updateControls();
    }

    @Override
    public void onParentDirectoryChanged() {
        setPathInProjectConfig();
    }

    private void setPathInProjectConfig() {
        String pathToParent = Path.valueOf(view.getParentDirectory())
                                  .makeAbsolute()
                                  .addTrailingSeparator()
                                  .toString();

        String pathToProject = pathToParent + view.getName();
        dataObject.setPath(pathToProject);
    }

    @Override
    public void projectNameChanged(String name) {
        setPathInProjectConfig();

        dataObject.setName(name);
        updateDelegate.updateControls();

        if (NameUtils.checkProjectName(name)) {
            view.removeNameError();
        } else {
            view.showNameError();
        }
    }

    private String getPath(Node parent) {
        if (parent instanceof ProjectNode) {
            return ((ProjectNode)parent).getStorablePath() + '/';
        } else if (parent instanceof FolderReferenceNode) {
            return ((FolderReferenceNode)parent).getData().getPath() + '/';
        }

        return DEFAULT_PARENT_DIRECTORY;
    }

    @Override
    public void projectDescriptionChanged(String projectDescription) {
        dataObject.setDescription(projectDescription);
        updateDelegate.updateControls();
    }

    public void setProjectTypeSelectionListener(ProjectTypeSelectionListener listener) {
        projectTypeSelectionListener = listener;
    }

    public void setProjectTemplateSelectionListener(ProjectTemplateSelectionListener listener) {
        projectTemplateSelectionListener = listener;
    }

    private void loadProjectTypesAndTemplates() {
        List<ProjectTypeDto> projectTypes = projectTypeRegistry.getProjectTypes();
        Map<String, Set<ProjectTypeDto>> typesByCategory = new HashMap<>();
        Map<String, Set<ProjectTemplateDescriptor>> templatesByCategory = new HashMap<>();
        for (ProjectTypeDto type : projectTypes) {
            if (wizardRegistry.getWizardRegistrar(type.getId()) != null) {
                final String category = wizardRegistry.getWizardCategory(type.getId());
                if (!typesByCategory.containsKey(category)) {
                    typesByCategory.put(category, new HashSet<ProjectTypeDto>());
                }
                typesByCategory.get(category).add(type);
            }

            List<ProjectTemplateDescriptor> templateDescriptors = projectTemplateRegistry.getTemplateDescriptors(type.getId());
            for (ProjectTemplateDescriptor template : templateDescriptors) {
                final String category = template.getCategory() == null ? DEFAULT_TEMPLATE_CATEGORY : template.getCategory();
                if (!templatesByCategory.containsKey(category)) {
                    templatesByCategory.put(category, new HashSet<ProjectTemplateDescriptor>());
                }
                templatesByCategory.get(category).add(template);
            }
        }

        view.setProjectTypes(projectTypes);
        view.setCategories(typesByCategory, templatesByCategory);
    }

    public interface ProjectTypeSelectionListener {
        /** Called when project type selected. */
        void onProjectTypeSelected(ProjectTypeDto projectTypeDto);
    }

    public interface ProjectTemplateSelectionListener {
        /** Called when project template selected. */
        void onProjectTemplateSelected(ProjectTemplateDescriptor projectTemplateDescriptor);
    }
}
