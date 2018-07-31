/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
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
export class DockerRegistryList implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'app/administration/docker-registry/docker-registry-list/docker-registry-list.html';
  replace = false;

  controller = 'DockerRegistryListController';
  controllerAs = 'dockerRegistryListController';

  bindToController = true;

  scope = true;

}
