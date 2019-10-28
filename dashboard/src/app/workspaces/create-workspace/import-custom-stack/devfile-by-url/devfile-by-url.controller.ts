/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {CheFactory} from '../../../../../components/api/che-factory.factory';

const ATTR_URL = 'factoryurl';

/**
 * This class is handling the controller for the import devfile by URL
 *
 * @author Oleksii Orel
 */
export class DevfileByUrlController {

  static $inject = ['cheFactory', '$q'];

  workspaceDevfileOnChange: Function;
  workspaceDevfileLocation: string;

  private cheFactory: CheFactory;
  private $q: ng.IQService;

  /**
   * Default constructor that is using resource injection
   */
  constructor(cheFactory: CheFactory, $q: ng.IQService) {
    this.cheFactory = cheFactory;
    this.$q = $q;
  }

  $onInit(): void {
  }

  onUrlChanged(url: string, form): void {
    if (this.cheFactory.hasDevfile(url)) {
      const devfile = this.cheFactory.getDevfile(url);
      const attributes = {};
      attributes[ATTR_URL] = url;
      if (angular.isFunction(this.workspaceDevfileOnChange) && devfile) {
        this.workspaceDevfileOnChange({devfile, attributes});
      }
    }
  }

  isUrlValid(url: string): ng.IPromise<void> {
    return this.cheFactory.fetchDevfile(url);
  }
}
