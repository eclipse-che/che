import { inject } from "inversify";
import "reflect-metadata";
import { TYPES } from "../../types";
import { Driver } from "../../driver/Driver";
import { WebElementCondition } from "selenium-webdriver";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

 export class Dashboard {
    private readonly driver: Driver;

    private static readonly DASHBOARD_BUTTON: string = "#dashboard-item";
    private static readonly WORKSPACES_BUTTON: string = "#workspaces-item";
    private static readonly STACKS_BUTTON: string = "#stacks-item";
    private static readonly FACTORIES_BUTTON: string = "#factories-item";
    private static readonly LOADER_PAGE: string = ".main-page-loader"

    constructor(
        @inject(TYPES.Driver) driver: Driver 
    ){
        this.driver = driver;
    }


 }