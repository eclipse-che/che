import { Container } from "inversify";
import { Driver } from "./driver/Driver";
import { TYPES } from "./types";
import { ChromeDriver } from "./driver/ChromeDriver";

const e2eContainer = new Container();

e2eContainer.bind<Driver>(TYPES.Driver).to(ChromeDriver).inSingletonScope();


export { e2eContainer }
