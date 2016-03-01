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
package org.eclipse.che.ide.part.explorer.project.synchronize;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ConfigureProjectEvent;
import org.eclipse.che.ide.api.event.project.DeleteProjectEvent;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.projectimport.wizard.ProjectImporter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.smartTree.event.BeforeExpandNodeEvent;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * The class contains business logic which allows synchronize projects in workspace with projects on file system. The synchronization
 * performs based on project problems. Project problem with code 10 means that project exist in workspace but it is absent on file
 * system, code 9 means that project exist on file system but absent in workspace and code 8 means that project exist in workspace
 * and file system but project configuration was changed.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectConfigSynchronizationListener implements BeforeExpandNodeEvent.BeforeExpandNodeHandler {

    private final DialogFactory            dialogFactory;
    private final ProjectImporter          projectImporter;
    private final CoreLocalizationConstant locale;
    private final ProjectServiceClient     projectService;
    private final NotificationManager      notificationManager;
    private final EventBus                 eventBus;
    private final ChangeLocationWidget     changeLocationWidget;
    private final CancelCallback           cancelCallback;
    private final DtoUnmarshallerFactory   factory;
    private final AppContext               appContext;
    private final String                   workspaceId;

    private ProjectConfigDto projectConfig;

    @Inject
    public ProjectConfigSynchronizationListener(ProjectImporter projectImporter,
                                                EventBus eventBus,
                                                DialogFactory dialogFactory,
                                                CoreLocalizationConstant locale,
                                                ProjectServiceClient projectService,
                                                AppContext appContext,
                                                NotificationManager notificationManager,
                                                ChangeLocationWidget changeLocationWidget,
                                                DtoUnmarshallerFactory factory) {
        this.projectImporter = projectImporter;
        this.dialogFactory = dialogFactory;
        this.locale = locale;
        this.projectService = projectService;
        this.notificationManager = notificationManager;
        this.eventBus = eventBus;
        this.changeLocationWidget = changeLocationWidget;
        this.factory = factory;
        this.appContext = appContext;

        this.workspaceId = appContext.getWorkspaceId();

        this.cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {
                deleteProject();
            }
        };

        eventBus.addHandler(BeforeExpandNodeEvent.getType(), this);
    }

    @Override
    public void onBeforeExpand(final BeforeExpandNodeEvent event) {
        if (appContext.getFactory() != null) {
            return;
        }
        Node expandedNode = event.getNode();

        if (!(expandedNode instanceof ProjectNode)) {
            return;
        }

        this.projectConfig = ((ProjectNode)expandedNode).getProjectConfig();

        if (isProjectImporting()) {
            event.setCancelled(true);

            dialogFactory.createMessageDialog(locale.projectStatusTitle(),
                                              locale.projectStatusContent(projectConfig.getName()),
                                              new ConfirmCallback() {
                                                  @Override
                                                  public void accepted() {
                                                  }
                                              }).show();
            return;
        }

        List<ProjectProblemDto> problems = projectConfig.getProblems();

        if (problems.isEmpty()) {
            return;
        }

        for (ProjectProblemDto problem : problems) {
            switch (problem.getCode()) {
                case 10:
                    event.setCancelled(true);

                    projectExistInWSButAbsentOnVFS();
                    break;
                case 9:
                    projectExistOnVFSButAbsentInWS();
                    break;
                case 8:
                    projectConfigurationChanged();
                default:
            }
        }
    }

    private boolean isProjectImporting() {
        for (String pathToProject : appContext.getImportingProjects()) {
            if (pathToProject.equals(projectConfig.getPath())) {
                return true;
            }
        }

        return false;
    }

    private void projectExistInWSButAbsentOnVFS() {
        dialogFactory.createConfirmDialog(locale.synchronizeDialogTitle(),
                                          locale.existInWorkspaceDialogContent(projectConfig.getName()),
                                          locale.buttonImport(),
                                          locale.buttonRemove(),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  String location = projectConfig.getSource().getLocation();

                                                  if (location == null) {
                                                      changeLocation();

                                                      return;
                                                  }
                                                  importProject();
                                              }
                                          },
                                          cancelCallback).show();
    }

    private void projectExistOnVFSButAbsentInWS() {
        dialogFactory.createConfirmDialog(locale.synchronizeDialogTitle(),
                                          locale.existInFileSystemDialogContent(projectConfig.getName()),
                                          locale.buttonConfigure(),
                                          locale.buttonRemove(),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  eventBus.fireEvent(new ConfigureProjectEvent(projectConfig));
                                              }
                                          },
                                          cancelCallback).show();
    }

    private void projectConfigurationChanged() {
        dialogFactory.createConfirmDialog(locale.synchronizeDialogTitle(),
                                          locale.projectConfigurationChanged(),
                                          locale.buttonConfigure(),
                                          locale.buttonKeepBlank(),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  eventBus.fireEvent(new ConfigureProjectEvent(projectConfig));
                                              }
                                          },
                                          new CancelCallback() {
                                              @Override
                                              public void cancelled() {
                                                  projectConfig.setType(Constants.BLANK_ID);

                                                  updateProject();
                                              }
                                          }).show();
    }

    private void changeLocation() {
        dialogFactory.createConfirmDialog(locale.locationDialogTitle(), changeLocationWidget, new ConfirmCallback() {
            @Override
            public void accepted() {
                SourceStorageDto source = projectConfig.getSource();
                source.setLocation(changeLocationWidget.getText());
                source.setType("github");

                importProject();
            }
        }, new CancelCallback() {
            @Override
            public void cancelled() {
            }
        }).show();
    }

    private void importProject() {
        projectImporter.checkFolderExistenceAndImport(new Wizard.CompleteCallback() {
            @Override
            public void onCompleted() {
                Log.info(getClass(), "Project " + projectConfig.getName() + " imported.");
            }

            @Override
            public void onFailure(Throwable exception) {
                Log.error(getClass(), exception);
            }
        }, projectConfig);
    }

    private void deleteProject() {
        projectService.delete(workspaceId, projectConfig.getPath(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                eventBus.fireEvent(new DeleteProjectEvent(projectConfig));

                notificationManager.notify(locale.projectRemoved(projectConfig.getName()), StatusNotification.Status.SUCCESS, true);
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(getClass(), exception);

                notificationManager.notify(locale.projectRemoveError(projectConfig.getName()), FAIL, true);
            }
        });
    }

    private void updateProject() {
        projectService.updateProject(workspaceId,
                                     projectConfig.getPath(),
                                     projectConfig,
                                     new AsyncRequestCallback<ProjectConfigDto>(factory.newUnmarshaller(ProjectConfigDto.class)) {
                                         @Override
                                         protected void onSuccess(ProjectConfigDto result) {
                                             eventBus.fireEvent(new ProjectUpdatedEvent(result.getPath(), result));
                                         }

                                         @Override
                                         protected void onFailure(Throwable exception) {
                                             Log.error(getClass(), exception);

                                             notificationManager.notify(locale.projectUpdateError(projectConfig.getName()), FAIL, true);
                                         }
                                     });
    }
}
