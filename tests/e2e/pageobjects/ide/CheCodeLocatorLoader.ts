/** *******************************************************************
 * copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { LocatorLoader } from 'monaco-page-objects/out/locators/loader';
import { getLocatorsPath } from 'vscode-extension-tester-locators';
import { LocatorDiff, Locators } from 'monaco-page-objects';
import { By } from 'selenium-webdriver';
import clone from 'clone-deep';
import { MONACO_CONSTANTS } from '../../constants/MONACO_CONSTANTS';
import { injectable } from 'inversify';

/**
 * this class allows us to change or add some specific locators base on "monaco-page-object" and "vscode-extension-tester-locators".
 * Use method webLocatorDiff(). To change place locator into field "locators", to add - "extras".
 * To see full locators list check "node_modules/vscode-extension-tester-locators/out/lib".
 */

@injectable()
export class CheCodeLocatorLoader extends LocatorLoader {
	readonly webCheCodeLocators: Locators;

	constructor() {
		super(
			MONACO_CONSTANTS.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION,
			MONACO_CONSTANTS.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION,
			getLocatorsPath()
		);
		this.webCheCodeLocators = this.mergeLocators();
	}

	private webLocatorDiff(): LocatorDiff {
		return {
			locators: {
				WelcomeContent: {
					text: By.xpath('//*[@class="dialog-message-text" and contains(text(), "trust")]'),
					button: By.xpath('//div[@class="monaco-dialog-box"]//a[@class="monaco-button monaco-text-button"]')
				},
				ScmView: {
					actionConstructor: (title: string): By => By.xpath(`.//a[@title="${title}"]`)
				}
			},
			extras: {
				ExtensionsViewSection: {
					requireReloadButton: By.xpath('//a[text()="Reload Required"]')
				}
			}
		};
	}

	private merge(target: any, obj: any): object {
		for (const key in obj) {
			if (!Object.prototype.hasOwnProperty.call(obj, key)) {
				continue;
			}

			const oldVal: any = obj[key];
			const newVal: any = target[key];

			if (typeof newVal === 'object' && typeof oldVal === 'object') {
				target[key] = this.merge(newVal, oldVal);
			} else {
				target[key] = clone(oldVal);
			}
		}
		return target;
	}

	private mergeLocators(): Locators {
		const target: Locators = super.loadLocators();

		this.merge(target, this.webLocatorDiff().locators as Locators);
		this.merge(target, this.webLocatorDiff().extras as Locators);

		return target;
	}
}
