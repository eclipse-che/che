/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { By, Key } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class ContextMenu {
     private static readonly SUGGESTION_WIDGET_BODY_CSS: string = 'ul.p-Menu-content';

     constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }


     async invokeContextMenuOnTheElementWithMouse(elementLocator: By) {
          Logger.debug(`ContextMenu.invokeContextMenuOnTheElementWithMouse ${elementLocator}`);

          // const webElement: WebElement = await this.driverHelper.waitVisibility(elementLocator, TimeoutConstants.TS_CONTEXT_MENU_TIMEOUT);
          // await this.driverHelper.getAction().click(webElement, Button.RIGHT).perform();
          this.waitContextMenu();
     }

     async invokeContextMenuOnActiveElementWithKeys() {
          Logger.debug('ContextMenu.invokeContextMenuOnActiveElementWithKeys');

          this.driverHelper.getDriver().switchTo().activeElement().sendKeys(Key.SHIFT + Key.F10);
          this.waitContextMenu();
     }

     async waitContextMenuAndClickOnItem(nameOfItem: string) {
          Logger.debug(`ContextMenu.waitContextMenuAndClickOnItem "${nameOfItem}"`);

          const itemLocator: string = `//div[@class='p-Menu-itemLabel' and text()='${nameOfItem}']`;
          await this.waitContextMenu();
          await this.driverHelper.waitAndClick(By.xpath(itemLocator), TimeoutConstants.TS_CONTEXT_MENU_TIMEOUT);
     }

     async waitContextMenu() {
          Logger.debug(`ContextMenu.waitContextMenu`);

          await this.driverHelper.waitVisibility(By.css(ContextMenu.SUGGESTION_WIDGET_BODY_CSS), TimeoutConstants.TS_CONTEXT_MENU_TIMEOUT);
     }

}
