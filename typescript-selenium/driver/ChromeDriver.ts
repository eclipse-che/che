import 'chromedriver';
import 'reflect-metadata';
import { injectable, inject } from "inversify";
import { ThenableWebDriver, Builder } from "selenium-webdriver";
import { Driver } from './Driver';
import { Options } from 'selenium-webdriver/chrome';
import { TestConstants } from '../TestConstants';

@injectable()
export class ChromeDriver implements Driver {
    private readonly driver: ThenableWebDriver;

    constructor() {
        const isHeadless: boolean = TestConstants.TS_SELENIUM_HEADLESS;


        if (isHeadless) {
            this.driver = new Builder()
                .forBrowser('chrome')
                .setChromeOptions(new Options().addArguments('--no-sandbox').addArguments('headless'))
                .build();
        } else {
            this.driver = new Builder()
                .forBrowser('chrome')
                .setChromeOptions(new Options().addArguments('--no-sandbox'))
                .build();
        }

        this.driver
            .manage()
            .window()
            .setSize(1920, 1080)
    }

    get(): ThenableWebDriver {
        return this.driver
    }

}
