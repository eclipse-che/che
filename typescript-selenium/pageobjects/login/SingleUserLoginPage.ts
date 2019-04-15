import "reflect-metadata";
import { LoginPage } from "./LoginPage";
import { injectable, inject } from "inversify";
import { ThenableWebDriver } from "selenium-webdriver";
import { TYPES } from "../../types";
import { Driver } from "../../driver/Driver";

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
            .to("http://che-che.192.168.99.100.nip.io/dashboard/#/create-workspace")
    }

}