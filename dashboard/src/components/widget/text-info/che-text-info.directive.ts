/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
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
 * Defines a directive for creating text info container.
 * @author Oleksii Orel
 */
export class CheTextInfo implements ng.IDirective {

  restrict = 'E';
  replace = true;
  transclude = false;
  templateUrl = 'components/widget/text-info/che-text-info.html';

  // scope values
  scope = {
    textValue: '=cheText',
    hrefValue: '=cheHref',
    labelName: '@cheLabelName',
    copyClipboard: '=cheCopyClipboard'
  };

}
