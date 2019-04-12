import { inject, injectable } from "inversify";
import "reflect-metadata";
import { TYPES, CLASSES } from "../../types";
import { Driver } from "../../driver/Driver";
import { WebElementCondition, By } from "selenium-webdriver";
import { DriverHelper } from "../../utils/DriverHelper";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

@injectable()
export class Dashboard {
    private readonly driverHelper: DriverHelper;

    private static readonly DASHBOARD_BUTTON_CSS: string = "#dashboard-item";
    private static readonly WORKSPACES_BUTTON_CSS: string = "#workspaces-item";
    private static readonly STACKS_BUTTON_CSS: string = "#stacks-item";
    private static readonly FACTORIES_BUTTON_CSS: string = "#factories-item";
    private static readonly LOADER_PAGE_CSS: string = ".main-page-loader"

    constructor(
        @inject(CLASSES.DriverHelper) driverHelper: DriverHelper
    ) {
        this.driverHelper = driverHelper;
    }

    waitPage(timeout: number) {
        it('Wait Dashboard page', async () => {
            await this.driverHelper
                .waitAllVisibility([
                    By.css(Dashboard.DASHBOARD_BUTTON_CSS),
                    By.css(Dashboard.WORKSPACES_BUTTON_CSS),
                    By.css(Dashboard.STACKS_BUTTON_CSS),
                    By.css(Dashboard.FACTORIES_BUTTON_CSS),
                    By.css(Dashboard.LOADER_PAGE_CSS)
                ], timeout)
        })
    }

    clickDashboardButton(timeout = DriverHelper.DEFAULT_TIMEOUT) {
        it("click 'Dashboard' button", async () => {
            await this.driverHelper.click(By.css(Dashboard.DASHBOARD_BUTTON_CSS), timeout)
        })
    }


}