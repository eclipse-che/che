import "reflect-metadata";
import { LoginPage } from "./LoginPage";
import { injectable, inject } from "inversify";
import { ThenableWebDriver } from "selenium-webdriver";
import { TYPES } from "../../types";
import { Driver } from "../../driver/Driver";
import { TestConstants } from "../../TestConstants";

@injectable()
export class SingleUserLoginPage implements LoginPage {
    private readonly driver: ThenableWebDriver;

    constructor(
        @inject(TYPES.Driver) driver: Driver
    ) {
        this.driver = driver.get();
    }


    async login() {
        await this.driver
            .navigate()
            .to(TestConstants.TS_SELENIUM_BASE_URL)
    }

}