import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { WebElement, Button, By, Key } from 'selenium-webdriver';
import { TestConstants } from '../../TestConstants';



@injectable()
export class ContextMenu {
     private static readonly SUGGESTION_WIDGET_BODY_CSS: string = 'ul.p-Menu-content';

     constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }


     async invokeContextMenuOnTheElementWithMouse(elementLocator: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
          const webElement: WebElement = await this.driverHelper.waitVisibility(elementLocator, timeout);
          await this.driverHelper.getAction().click(webElement, Button.RIGHT).perform();
          this.waitContextMenu(timeout);
     }

     async invokeContextMenuOnActiveElementWithKeys(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
          this.driverHelper.getDriver().switchTo().activeElement().sendKeys(Key.SHIFT + Key.F10);
          this.waitContextMenu(timeout);
     }

     async waitContextMenuAndClickOnItem(nameOfItem: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
          const itemLocator: string = `//div[@class='p-Menu-itemLabel' and text()='${nameOfItem}']`;
          await this.waitContextMenu();
          await this.driverHelper.waitAndClick(By.xpath(itemLocator), timeout);
     }

     async waitContextMenu(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
          await this.driverHelper.waitVisibility(By.css(ContextMenu.SUGGESTION_WIDGET_BODY_CSS), timeout);
     }

}
