import { Container } from "inversify";
import { Driver } from "./driver/Driver";
import { TYPES } from "./types";
import { ChromeDriver } from "./driver/ChromeDriver";
import { DriverHelper } from "./utils/DriverHelper";

const e2eContainer = new Container();

e2eContainer.bind<Driver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();
e2eContainer.bind<DriverHelper>('DriverHelper').to(DriverHelper)

export { e2eContainer }
