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

export class CheColorsConfig {

  constructor(register) {
    // Register this factory
    register.app.constant('jsonColors', JSON.stringify({
  '$che-navy-color': '#353E50',
  '$che-medium-blue-color': '#4A90E2',
  '$che-orange-color': '#F37943',
  '$che-green-color': '#60AB0A',
  '$che-purple-color': '#4E5A96',
  '$che-white-color' : '#FFFFFF',
  '$che-black-color' : '#000000',
  '$che-ide-background-color' : '#292C2F ',
  '$che-logo-yellow-color' : '#FDB940',
  '$che-logo-blue-color' : '#525C86',
  '$dark-menu-color': '#21252B',
  '$night-foggy-sky-color': '#545B64',
  '$dark-menacing-sky-color': '#5A5A5A',
  '$clear-foggy-sky-color': '#AEB0B2',
  '$red-lipstick-color': '#DB4437',
  '$cat-gray-color': '#E4E4E4',
  '$mouse-gray-color': '#D3D3D3',
  '$light-gray-color': '#F1F1F1',
  '$stroke-color': '#979797',
  '$very-light-grey-color': '#CBCBCB'
}
));

  }
}
