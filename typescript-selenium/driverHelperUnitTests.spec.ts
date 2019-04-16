/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from "./inversify.config";
import { Driver } from "./driver/Driver";
import { TYPES, CLASSES } from "./types";
import { DriverHelper } from "./utils/DriverHelper";
import { By, WebElementCondition, Condition } from "selenium-webdriver";
import { describe, after } from "mocha";
import { LoginPage } from "./pageobjects/login/LoginPage";
import { Dashboard } from "./pageobjects/dashboard/Dashboard";
import { expect, assert } from 'chai'
import { Workspaces } from "./pageobjects/dashboard/Workspaces";



const driver: Driver = e2eContainer.get<Driver>(TYPES.Driver);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const loginPage: LoginPage = e2eContainer.get<LoginPage>(TYPES.LoginPage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard)
const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces)


suite("Test of 'DriverHelper' methods", async () => {
    test("login", async () => {
        await loginPage.login()
    })

    test("waitAllVisibility", async () => {
        await driverHelper.waitAllVisibility([By.css("#dashboard-item"), By.css("#workspaces-item"), By.css("#stacks-item")], 20000)
    })

    test("isVisible", async () => {
        const isVisible = await driverHelper.isVisible(By.css("#dashboard-item"))
        expect(isVisible).to.be.true
    })

    test("isVisible", async () => {
        const isVisible = await driverHelper.isVisible(By.css("#dashboard-item aaaa"))
        expect(isVisible).to.be.false
    })

    test("waitVisibilityBoolean", async () => {
        const isVisible = await driverHelper.waitVisibilityBoolean(By.css("#dashboard-item"))
        expect(isVisible).to.be.true
    })

    test("waitVisibilityBoolean", async () => {
        const isVisible = await driverHelper.waitVisibilityBoolean(By.css("#dashboard-item aaaa"))
        expect(isVisible).to.be.false
    })

    test("waitDisappearanceBoolean", async () => {
        const isDisappeared = await driverHelper.waitDisappearanceBoolean(By.css("#dashboard-item"))
        expect(isDisappeared).to.be.false
    })

    test("waitDisappearanceBoolean", async () => {
        const isDisappeared = await driverHelper.waitDisappearanceBoolean(By.css("#dashboard-item aaaa"))
        expect(isDisappeared).to.be.true
    })

    test("waitDisappearance", async () => {
        await driverHelper.waitDisappearance(By.css("#dashboard-item aaaa"))
    })


    test("click dashboard button", async () => {
        await driverHelper.waitAndClick(By.css("#dashboard-item"))
    })

    test("waitAllDisappearance", async () => {
        await driverHelper.waitAllDisappearance([By.css("#dashboard-item aaa"), By.css("#workspaces-item aaa"), By.css("#stacks-item aaa")], 5, 1000)
    })

    ////

    test("getElementAttribute", async () => {
        const attributValue: string = await driverHelper.getElementAttribute(By.css("#dashboard-item"), 'id')
        expect(attributValue).to.be.equal('dashboard-item')
    })

    test("waitAttributeValue", async ()=>{
        await driverHelper.waitAttributeValue(By.css("#dashboard-item"), 'id', 'dashboard-item')
    })




})

suiteTeardown("close browser", async () => {
    driver.get().quit()
})
