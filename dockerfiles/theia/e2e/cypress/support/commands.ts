/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

// ***********************************************
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

Cypress.Commands.add('theiaCleanup', (text: string) => {

    //clean up any dialog
    cy.get('body').then(($body) => {
      const el = $body.find('div.p-Widget.dialogOverlay > div > div.dialogControl > button');
      if (el.length) {
        el.click();
      }
    })

    cy.get('body').click({force:true});
  });


Cypress.Commands.add('theiaCommandPaletteClick', (text: string, keyActions?: string) => {
  if (!keyActions) {
    keyActions = '';
  }
  cy.get('body').type('{ctrl}{cmd}{shift}p').then(() => {
    cy.get('.monaco-inputbox>.wrapper>.input').type(text).then(() => {
          cy.get('.monaco-inputbox>.wrapper>.input').type(keyActions + '{enter}').then(() => {
        return true;
      });
    });
  });
});


// Grab all name of extensions from extensions panel
Cypress.Commands.add('theiaCommandPaletteItems', (text: string) => {
    cy.get('body').type('{ctrl}{cmd}{shift}p').then(() => {
      cy.get('.monaco-inputbox>.wrapper>.input').type(text).then(() => {
        cy.get('.quick-open-tree .monaco-tree .monaco-tree-rows').find('.monaco-tree-row').then((items) => {
            let texts = items.map((i, el) => Cypress.$(el).text())
            return cy.wrap(texts.get());
          });
      }
      );
    });
  });



  
// Grab all name of extensions from extensions panel
Cypress.Commands.add('theiaExtensionsList', () => {
  // click on Help
  cy.get('#theia-top-panel').contains('Help').click().then(() => {
    cy.wait(2000);
    // Select About menu in the list
    cy.get('.p-Menu.p-MenuBar-menu').contains('About').trigger('mousemove').then(() => {
    }).then((element) => {
      // THen click on it
      cy.get('.p-Menu.p-MenuBar-menu').contains('About').click().then(() => {
        cy.get('body > div.p-Widget.dialogOverlay > div > div.dialogContent > div > ul').find('li').then((items) => {
          let texts = items.map((i, el) => Cypress.$(el).text().split(' ')[0]);
          return cy.wrap(texts.get());
        });
      })
    });
})
});


// Grab all name of extensions from extensions panel
Cypress.Commands.add('theiaExtensionsListFromPanel', () => {
  cy.get('body').type('{shift}{cmd}X').then(() => {
    cy.get('#extensions .spinnerContainer').should('exist').then(() => {
      cy.get('#extensionListContainer', {timeout: 60000}).should('exist').then(() => {
        cy.get('#extensions').find('.extensionName').then((items) => {
          let texts = items.map((i, el) => Cypress.$(el).text())
          return cy.wrap(texts.get());
        });
      });
    });
  });
});


// see more example of adding custom commands to Cypress TS interface
// in https://github.com/cypress-io/add-cypress-custom-command-in-typescript
// add new command to the existing Cypress interface
// tslint:disable-next-line no-namespace
declare namespace Cypress {
  // tslint:disable-next-line interface-name
  interface Chainable {
    theiaCommandPaletteClick: (value: string, keyActions?: string) => Cypress.Chainable<boolean>
    theiaCommandPaletteItems: (value: string) => Cypress.Chainable<string[]>,
    theiaExtensionsList: () => Cypress.Chainable<string[]>,
    theiaCleanup : () => void
  }
}

