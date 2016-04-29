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
package org.eclipse.che.plugin.maven.client.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.GeneratorDescription;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.maven.client.MavenArchetype;
import org.eclipse.che.plugin.maven.client.MavenExtension;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ModuleNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.NameUtils;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARCHETYPE_GENERATION_STRATEGY;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_VERSION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.VERSION;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class CreateMavenModulePresenter implements CreateMavenModuleView.ActionDelegate {

    private final AppContext                appContext;
    private final CreateMavenModuleView     view;
    private final ProjectServiceClient      projectService;
    private final DtoFactory                dtoFactory;
    private final DialogFactory             dialogFactory;
    private final ProjectExplorerPresenter  projectExplorer;
    private final MavenLocalizationConstant locale;
    private final SelectionAgent            selectionAgent;
    private final DtoUnmarshallerFactory    unmarshallerFactory;

    private String moduleName;

    private String         artifactId;
    private CurrentProject parentProject;

    @Inject
    public CreateMavenModulePresenter(AppContext appContext,
                                      CreateMavenModuleView view,
                                      ProjectServiceClient projectServiceClient,
                                      DtoFactory dtoFactory,
                                      DialogFactory dialogFactory,
                                      ProjectExplorerPresenter projectExplorer,
                                      MavenLocalizationConstant locale,
                                      SelectionAgent selectionAgent,
                                      DtoUnmarshallerFactory unmarshallerFactory) {
        this.view = view;
        this.view.setDelegate(this);

        this.appContext = appContext;
        this.projectService = projectServiceClient;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        this.projectExplorer = projectExplorer;
        this.locale = locale;
        this.selectionAgent = selectionAgent;
        this.unmarshallerFactory = unmarshallerFactory;
    }

    public void showDialog(@NotNull CurrentProject project) {
        parentProject = project;
        view.setParentArtifactId(project.getAttributeValue(ARTIFACT_ID));
        view.setGroupId(project.getAttributeValue(GROUP_ID));
        view.setVersion(project.getAttributeValue(VERSION));
        view.reset();
        view.show();
        updateViewState();
    }

    @Override
    public void onClose() {
    }

    @Override
    public void create() {
        ProjectConfigDto projectConfig = dtoFactory.createDto(ProjectConfigDto.class);
        projectConfig.setType(MAVEN_ID);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ARTIFACT_ID, Arrays.asList(artifactId));
        attributes.put(GROUP_ID, Arrays.asList(view.getGroupId()));
        attributes.put(VERSION, Arrays.asList(view.getVersion()));
        attributes.put(PACKAGING, Arrays.asList(view.getPackaging()));
        attributes.put(PARENT_ARTIFACT_ID, Arrays.asList(parentProject.getAttributeValue(ARTIFACT_ID)));
        attributes.put(PARENT_GROUP_ID, Arrays.asList(parentProject.getAttributeValue(GROUP_ID)));
        attributes.put(PARENT_VERSION, Arrays.asList(parentProject.getAttributeValue(VERSION)));

        projectConfig.setAttributes(attributes);
        projectConfig.setName(view.getName());

        view.showButtonLoader(true);

        String pathToSelectedNode = getPathToSelectedNode();

        if (pathToSelectedNode.isEmpty()) {
            showErrorDialog(locale.mavenCreateModuleMultySelectionError());

            return;
        }

        projectService.createModule(appContext.getDevMachine(),
                                    pathToSelectedNode,
                                    projectConfig,
                                    new AsyncRequestCallback<ProjectConfigDto>(
                                            unmarshallerFactory.newUnmarshaller(ProjectConfigDto.class)) {
                                        @Override
                                        protected void onSuccess(ProjectConfigDto addedModule) {
                                            view.close();
                                            view.showButtonLoader(false);

                                            Selection<?> selection = selectionAgent.getSelection();

                                            HasStorablePath parentFolder = (HasStorablePath)selection.getHeadElement();

                                            boolean isModule = parentFolder instanceof ModuleNode;
                                            boolean isProject = parentFolder instanceof ProjectNode;

                                            // TODO: rework after new Project API
                                            ProjectConfigDto projectConfigDto = appContext.getCurrentProject().getProjectConfig();

//                                            ProjectConfigDto parentConfig =
//                                                    projectConfigDto.findModule(parentFolder.getStorablePath());

//                                            if (parentConfig == null) {
//                                                throw new IllegalArgumentException("Parent folder not found for " + addedModule.getPath());
//                                            }

//                                            parentConfig.getModules().add(addedModule);

                                            if (isModule) {
                                                projectExplorer.reloadChildren((ModuleNode)parentFolder);
                                            }

                                            if (isProject) {
                                                projectExplorer.reloadChildren((ProjectNode)parentFolder);
                                            }
                                        }

                                        @Override
                                        protected void onFailure(Throwable exception) {
                                            showErrorDialog(exception.getMessage());
                                        }
                                    });
    }

    private String getPathToSelectedNode() {
        Selection<?> selection = projectExplorer.getSelection();

        if (selection.isMultiSelection() || selection.isEmpty()) {
            return "";
        }

        Object selectedElement = selection.getHeadElement();

        if (selectedElement instanceof HasStorablePath) {
            return ((HasStorablePath)selectedElement).getStorablePath();
        }

        return "";
    }

    private void showErrorDialog(String error) {
        view.showButtonLoader(false);
        dialogFactory.createMessageDialog("", error, null).show();
        Log.error(CreateMavenModulePresenter.class, error);
    }

    @Override
    public void projectNameChanged(String name) {
        if (NameUtils.checkProjectName(name)) {
            moduleName = name;
        } else {
            moduleName = null;
        }
        updateViewState();
    }

    private void updateViewState() {
        if (moduleName == null) {
            view.setNameError(true);
            view.setCreateButtonEnabled(false);
        } else {
            view.setNameError(false);
            if (artifactId == null) {
                view.setArtifactIdError(true);
                view.setCreateButtonEnabled(false);
            } else {
                view.setArtifactIdError(false);
                view.setCreateButtonEnabled(true);
            }
        }
        view.enableArchetypes(view.isGenerateFromArchetypeSelected());
        view.setPackagingVisibility(!view.isGenerateFromArchetypeSelected());
    }

    @Override
    public void artifactIdChanged(String artifactId) {
        if (NameUtils.checkProjectName(artifactId)) {
            this.artifactId = artifactId;
        } else {
            this.artifactId = null;
        }
        updateViewState();
    }

    @Override
    public void generateFromArchetypeChanged(boolean isGenerateFromArchetype) {
        updateViewState();
        if (!isGenerateFromArchetype) {
            view.clearArchetypes();
        } else {
            view.setArchetypes(MavenExtension.getAvailableArchetypes());
        }
    }

    private GeneratorDescription getGeneratorDescription(MavenArchetype archetype) {
        HashMap<String, String> options = new HashMap<>();
        options.put("type", ARCHETYPE_GENERATION_STRATEGY);
        options.put("archetypeGroupId", archetype.getGroupId());
        options.put("archetypeArtifactId", archetype.getArtifactId());
        options.put("archetypeVersion", archetype.getVersion());
        if (archetype.getRepository() != null) {
            options.put("archetypeRepository", archetype.getRepository());
        }
        return dtoFactory.createDto(GeneratorDescription.class).withOptions(options);
    }
}
