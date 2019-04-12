import { e2eContainer } from "./inversify.config";
import { Driver } from "./driver/Driver";
import { TYPES, CLASSES } from "./types";
import { DriverHelper } from "./utils/DriverHelper";
import { By, WebElementCondition, Condition } from "selenium-webdriver";
import { describe, after } from "mocha";
import { LoginPage } from "./pageobjects/login/LoginPage";
import { Dashboard } from "./pageobjects/dashboard/Dashboard";



const driver: Driver = e2eContainer.get<Driver>(TYPES.Driver);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const loginPage: LoginPage = e2eContainer.get<LoginPage>(TYPES.LoginPage);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard)

async function doNavigation(): Promise<void> {
    await driver.get()
        .navigate()
        .to("http://che-che.192.168.99.100.nip.io/dashboard/#/create-workspace")
}

async function waitDashboardButton(): Promise<void> {
    await driverHelper.waitVisibilityBoolean(By.css("#dashboard-item"));
}


describe("E2E", async () => {

    loginPage.login()

    dashboard.waitPage(100000)

    dashboard.clickDashboardButton()

    it("wait dashboard button by until condition", async () => {
        await driverHelper.click(By.css("#workspaces-item"))
    })

    after("close browser", async () => {
        await driver.get().quit()
    })

})

