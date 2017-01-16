/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc directive
 * @name administration.docker-registry.docker-registry-list.directive:dockerRegistryList
 * @restrict E
 * @element
 *
 * @description
 * <docker-registry-list></docker-registry-list>` for displaying list of registries.
 *
 * @usage
 *   <docker-registry-list></docker-registry-list>
 *
 * @author Oleksii Orel
 */
export class DockerRegistryList {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/administration/docker-registry/docker-registry-list/docker-registry-list.html';
    this.replace = false;

    this.controller = 'DockerRegistryListController';
    this.controllerAs = 'dockerRegistryListController';

    this.bindToController = true;

    this.scope = true;

  }

}
