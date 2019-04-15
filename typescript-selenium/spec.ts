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



suite("E2E", async () => {

    test("test", async ()=>{
        await loginPage.login()

        await dashboard.waitPage(100000)
    
    })


    suiteTeardown("close browser", async () => {
    
        setTimeout(()=>{
            driver.get().quit()
        }, 3000)


    
    })    


})



