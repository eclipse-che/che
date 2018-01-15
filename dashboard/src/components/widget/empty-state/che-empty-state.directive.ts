/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for creating empty state widget that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Orel
 */
export class CheEmptyState implements ng.IDirective {

  restrict = 'E';

  replace = true;
  transclude = false;
  templateUrl = 'components/widget/empty-state/che-empty-state.html';

  // scope values
  scope = {
    value: '@cheValue',
    prompt: '@?chePrompt',
    iconClass: '@cheIconClass'
  };

}
