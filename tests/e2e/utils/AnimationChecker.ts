/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { CLASSES } from '../inversify.types';
import { DriverHelper } from './DriverHelper';
import { Logger } from './Logger';
import { TimeoutConstants } from '../TimeoutConstants';

@injectable()
export class AnimationChecker {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitDropDownAnimationEnd(){
        Logger.debug('AnimationChecker.waitDropDownAnimationEnd')

        await this.driverHelper.wait(TimeoutConstants.TS_SELENIUM_ANIMATION_END_DELAY);
    }

}