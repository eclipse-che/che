import { ThenableWebDriver } from "selenium-webdriver";

export interface Driver {
    get(): ThenableWebDriver
}
