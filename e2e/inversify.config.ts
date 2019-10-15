/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import * as path from 'path';
import { Container } from 'inversify';

let pathh = path.resolve('.');
let containerInitializer = require(`${pathh}/dist/driver/ContainerInitializer.js`);
let e2eContainer : Container = containerInitializer.getContainer();

export { e2eContainer };
