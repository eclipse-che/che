import 'chromedriver';
import 'reflect-metadata';
import { injectable, inject } from "inversify";
import { ThenableWebDriver, Builder } from "selenium-webdriver";
import { Driver } from './Driver';

@injectable()
export class ChromeDriver implements Driver {
    private readonly driver: ThenableWebDriver;

    constructor() {
        this.driver = new Builder()
            .forBrowser('chrome')
            .build();

        this.driver
            .manage()
            .window()
            .setSize(1920, 1080)
    }

    get(): ThenableWebDriver{
        return this.driver
    }

}
