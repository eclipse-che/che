import { e2eContainer } from "./inversify.config";
import { Driver } from "./driver/Driver";
import { TYPES } from "./types";
import { DriverHelper } from "./utils/DriverHelper";
import { By } from "selenium-webdriver";


const driver: Driver = e2eContainer.get<Driver>(TYPES.Driver);
const driverHelper: DriverHelper = e2eContainer.get('DriverHelper');

async function doNavigation() {
    await driver.get()
        .navigate()
        .to("http://che-che.192.168.99.100.nip.io/dashboard/#/create-workspace")
}

async function ttt(){
    await driverHelper.waitVisibility(By.css("#dashboard-item"));
}

doNavigation()
ttt()


