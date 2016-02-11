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

import {CreateProjectCtrl} from './create-project/create-project.controller';
import {CreateProjectSvc} from './create-project/create-project.service.js';
import {ListProjectsCtrl} from './list-projects/list-projects.controller';
import {CreateProjectGithubCtrl} from './create-project/github/create-project-github.controller';
import {ListProjectsWorkspaceFilter} from './list-projects/list-projects-filter-workspace.filter';

import {CreateProjectGit} from './create-project/git/create-project-git.directive';
import {CreateProjectGitCtrl} from './create-project/git/create-project-git.controller';
import {CreateProjectGithub} from './create-project/github/create-project-github.directive';
import {CreateProjectSamplesCtrl} from './create-project/samples/create-project-samples.controller';
import {CreateProjectSamples} from './create-project/samples/create-project-samples.directive';

import {CreateProjectSamplesFilter} from './create-project/samples/create-project-samples.filter';
import {CreateProjectSamplesNameFilter} from './create-project/samples/create-project-samples-name.filter';
import {CreateProjectSamplesTagFilter} from './create-project/samples/create-project-samples-tag.filter';

import {CreateProjectPopupCtrl} from './create-project/popup/create-project-popup.controller';
import {CreateProjectPopup} from './create-project/popup/create-project-popup.directive';

import {CreateProjectZip} from './create-project/zip/create-project-zip.directive';
import {CreateProjectConfFile} from './create-project/config-file/create-project-conf-file.directive';
import {ProjectDetailsCtrl} from './project-details/project-details.controller';
import {ProjectRepositoryConfig} from './project-details/repository/project-repository-config';
import {CheProjectItem} from './list-projects/project-item/project-item.directive';
import {ProjectItemCtrl} from './list-projects/project-item/project-item.controller';

export class ProjectsConfig {

  constructor(register) {

    new ListProjectsWorkspaceFilter(register);
    new CreateProjectSamplesFilter(register);
    new CreateProjectSamplesNameFilter(register);
    new CreateProjectSamplesTagFilter(register);

    register.controller('ListProjectsCtrl', ListProjectsCtrl);

    register.controller('ProjectDetailsCtrl', ProjectDetailsCtrl);

    register.controller('CreateProjectPopupCtrl', CreateProjectPopupCtrl);
    register.directive('createProjectPopup', CreateProjectPopup);

    register.controller('CreateProjectGitCtrl', CreateProjectGitCtrl);
    register.directive('createProjectGit', CreateProjectGit);

    register.controller('CreateProjectGithubCtrl', CreateProjectGithubCtrl);
    register.directive('createProjectGithub', CreateProjectGithub);

    register.controller('CreateProjectSamplesCtrl', CreateProjectSamplesCtrl);
    register.directive('createProjectSamples', CreateProjectSamples);

    register.directive('createProjectZip', CreateProjectZip);

    register.directive('createProjectConfFile', CreateProjectConfFile);

    register.service('createProjectSvc', CreateProjectSvc);
    register.controller('CreateProjectCtrl', CreateProjectCtrl);

    register.directive('cheProjectItem', CheProjectItem);

    register.controller('ProjectItemCtrl', ProjectItemCtrl);


    let locationCreateProjectProvider = {
      templateUrl: 'app/projects/create-project/create-project.html',
      controller: 'CreateProjectCtrl',
      controllerAs: 'createProjectCtrl'
    };

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/projects', {
        templateUrl: 'app/projects/list-projects/list-projects.html',
        controller: 'ListProjectsCtrl',
        controllerAs: 'listProjectsCtrl'
      })
        .accessWhen('/project/:workspaceId/:projectName', {
          templateUrl: 'app/projects/project-details/project-details.html',
          controller: 'ProjectDetailsCtrl',
          controllerAs: 'projectDetailsCtrl'
        })
        .accessWhen('/create-project', locationCreateProjectProvider)
        .accessWhen('/create-project/:tabName', locationCreateProjectProvider);

    });

    //// config files
    new ProjectRepositoryConfig(register);

  }
}
