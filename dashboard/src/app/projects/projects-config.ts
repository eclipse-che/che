/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {CreateProjectController} from './create-project/create-project.controller';
import {CreateProjectSvc} from './create-project/create-project.service';
import {CreateProjectGithubController} from './create-project/github/create-project-github.controller';

import {CreateProjectGit} from './create-project/git/create-project-git.directive';
import {CreateProjectGitController} from './create-project/git/create-project-git.controller';
import {CreateProjectGithub} from './create-project/github/create-project-github.directive';
import {AddSecretKeyNotificationCtrl} from './create-project/add-ssh-key-notification/add-ssh-key-notification.controller';
import {NoGithubOauthDialogController} from './create-project/github/oauth-dialog/no-github-oauth-dialog.controller';
import {CreateProjectSamplesController} from './create-project/samples/create-project-samples.controller';
import {CreateProjectSamples} from './create-project/samples/create-project-samples.directive';
import {CreateProjectWorkspacesController} from './create-project/workspaces/create-project-workspaces.controller';
import {CreateProjectWorkspaces} from './create-project/workspaces/create-project-workspaces.directive';

import {CreateProjectSamplesTagFilter} from './create-project/samples/create-project-samples-tag.filter';

import {CreateProjectZip} from './create-project/zip/create-project-zip.directive';
import {CreateProjectConfFile} from './create-project/config-file/create-project-conf-file.directive';
import {ProjectDetailsController} from './project-details/project-details.controller';
import {ProjectRepositoryConfig} from './project-details/repository/project-repository-config';
import {CheProjectItem} from './list-projects/project-item/project-item.directive';
import {ProjectItemCtrl} from './list-projects/project-item/project-item.controller';

export class ProjectsConfig {

  constructor(register: che.IRegisterService) {
    new CreateProjectSamplesTagFilter(register);


    register.controller('ProjectDetailsController', ProjectDetailsController);

    register.controller('CreateProjectGitController', CreateProjectGitController);
    register.directive('createProjectGit', CreateProjectGit);

    register.controller('CreateProjectGithubController', CreateProjectGithubController);
    register.directive('createProjectGithub', CreateProjectGithub);

    register.controller('NoGithubOauthDialogController', NoGithubOauthDialogController);

    register.controller('AddSecretKeyNotificationCtrl', AddSecretKeyNotificationCtrl);

    register.controller('CreateProjectSamplesController', CreateProjectSamplesController);
    register.directive('createProjectSamples', CreateProjectSamples);

    register.controller('CreateProjectWorkspacesController', CreateProjectWorkspacesController);
    register.directive('createProjectWorkspaces', CreateProjectWorkspaces);

    register.directive('createProjectZip', CreateProjectZip);

    register.directive('createProjectConfFile', CreateProjectConfFile);

    register.service('createProjectSvc', CreateProjectSvc);
    register.controller('CreateProjectController', CreateProjectController);

    register.directive('cheProjectItem', CheProjectItem);

    register.controller('ProjectItemCtrl', ProjectItemCtrl);


    let locationCreateProjectProvider = {
      title: 'New Project',
      templateUrl: 'app/projects/create-project/create-project.html',
      controller: 'CreateProjectController',
      controllerAs: 'createProjectCtrl'
    };

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/project/:namespace/:workspaceName/:projectName', {
          title: (params) => {return params.workspaceName + ' | ' + params.projectName},
          templateUrl: 'app/projects/project-details/project-details.html',
          controller: 'ProjectDetailsController',
          controllerAs: 'projectDetailsController'
        })
        .accessWhen('/create-project', locationCreateProjectProvider)
        .accessWhen('/create-project/:tabName', locationCreateProjectProvider);

    });

    //// config files
    new ProjectRepositoryConfig(register);

  }
}
