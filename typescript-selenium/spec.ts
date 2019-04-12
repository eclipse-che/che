import { e2eContainer } from "./inversify.config";
import { Driver } from "./driver/Driver";
import { TYPES } from "./types";
import { DriverHelper } from "./utils/DriverHelper";
import { By, WebElementCondition, Condition } from "selenium-webdriver";
import { describe } from "mocha";
import { expect } from "chai";
import { until } from "selenium-webdriver"


const driver: Driver = e2eContainer.get<Driver>(TYPES.Driver);
const driverHelper: DriverHelper = e2eContainer.get('DriverHelper');

async function doNavigation(): Promise<void> {
    await driver.get()
        .navigate()
        .to("http://che-che.192.168.99.100.nip.io/dashboard/#/create-workspace")
}

async function waitDashboardButton(): Promise<void> {
    await driverHelper.waitVisibility(By.css("#dashboard-item"));
}


describe("E2E", async () => {

    it("navigate", async () => {
        await doNavigation()
    })


    it("wait dashboard button", async () => {
        await waitDashboardButton()
    })


    it("wait dashboard button by until condition", async () => {
        await driver.get().wait(until.elementLocated(By.css("#dashboard-item")), 10000)
        expect(true).to.be.true
        await driver.get().quit()
    })


})
