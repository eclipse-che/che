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

/**
 * Defines a directive for Loader
 * @author Oleksii Kurinnyi
 */
export class CheLoader implements ng.IDirective {

  restrict = 'E';
  replace = true;
  template = '<div ng-transclude class="che-loader"></div>';

  transclude = true;

}
