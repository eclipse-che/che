import { Container } from "inversify";
import { ILoginPage } from "../pageobjects/dashboard/interfaces/ILoginPage";
import { TYPES } from "./types";
import { LoginPage } from "../pageobjects/dashboard/LoginPage";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

const e2eContainer = new Container();

e2eContainer.bind<ILoginPage>(TYPES.ILoginPage).to(LoginPage)


export {e2eContainer}
