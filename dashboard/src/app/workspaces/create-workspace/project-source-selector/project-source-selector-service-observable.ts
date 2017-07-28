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

import {Observable} from '../../../../components/utils/observable';
import {ProjectSource} from './project-source.enum';
import {ActionType} from './project-source-selector-action-type.enum';

export abstract class ProjectSourceSelectorServiceObservable extends Observable {
  constructor() {
    super();
  }

  publish(action: ActionType, source: ProjectSource): void {
    super.publish(action, source);
  }
}
