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
 * Defines a directive for creating additional parts for list's header.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHeaderAdditionalParts implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'components/widget/list/list-header/additional-parts/che-list-header-additional-parts.html';
  transclude: boolean = true;
}

