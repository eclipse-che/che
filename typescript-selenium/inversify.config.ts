import { Container } from "inversify";
import { Driver } from "./driver/Driver";
import { TYPES } from "./types";
import { ChromeDriver } from "./driver/ChromeDriver";
import { DriverHelper } from "./utils/DriverHelper";
import { LoginPage } from "./pageobjects/login/LoginPage";
import { SingleUserLoginPage } from "./pageobjects/login/SingleUserLoginPage";
import { Dashboard } from "./pageobjects/dashboard/Dashboard";

const e2eContainer = new Container();

e2eContainer.bind<Driver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();
e2eContainer.bind<LoginPage>(TYPES.LoginPage).to(SingleUserLoginPage).inSingletonScope();

e2eContainer.bind<DriverHelper>('DriverHelper').to(DriverHelper).inSingletonScope();
e2eContainer.bind<Dashboard>('Dashboard').to(Dashboard).inSingletonScope();


export { e2eContainer }
