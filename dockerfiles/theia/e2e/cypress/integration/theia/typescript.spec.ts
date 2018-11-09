/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

context('TypeScript', () => {
    before(() => {
        cy.visit('http://localhost:3000');

        // maybe it's possible to wait for an element being displayed/hidden
        cy.wait(10000);
    })

    afterEach(() => {
        cy.theiaCleanup();
    });

    // Create a typescript file and check we can have intellisense
    it('Check Invalid Syntax', () => {

        const FOLDER_NAME = 'typescripttest' + makeid();
        const FILENAME = 'HelloWorld.ts';


        // close any workspace
        cy.theiaCommandPaletteClick('Close Workspace').then(() => {
            const $el = Cypress.$('button.theia-button.main');
            if ($el.length) {
                cy.get('button.theia-button.main').should('exist').then(() => {
                    cy.get('button.theia-button.main').click({ force: true });
                });
            }

        }).then(() => {
            // wait the refresh after workspace is opened
            cy.wait(6000);

            // open /tmp
            cy.get('#theia-top-panel').should('exist').then(() => {

                cy.theiaCommandPaletteClick('Open Workspace...', '{downarrow}').then(() => {
                    cy.get('.theia-LocationList').should('exist');
                    cy.get('.theia-LocationList').select('file:///');
                    cy.wait(2000);
                    cy.get('.dialogContent .theia-TreeNodeSegment').should('exist');
                    cy.get('.dialogContent .theia-TreeNodeSegment').contains('tmp').click({ force: true })
                    cy.get('.dialogContent  .theia-TreeNodeSegment').contains('tmp').click({ force: true })
                    cy.get('button.theia-button.main').click({ force: true });
                })
            });
        }).then(() => {
            // wait the refresh after workspace is opened
            cy.wait(10000);

            cy.get('#theia-top-panel').should('exist').then(() => {
                cy.theiaCommandPaletteClick('New Folder').then(() => {
                    // enter name of the folder
                    cy.get('.dialogContent input').type(FOLDER_NAME).then(() => {
                        cy.get('button.theia-button.main').click({ force: true });
                    });
                })
            });
        }).then(() => {
            // select new folder
            cy.get('.p-Widget.p-TabBar.theia-app-left.theia-app-sides.p-BoxPanel-child.ps').contains('Files').click({ force: true }).then(() => {
                cy.get('#files').contains(FOLDER_NAME).click({ force: true });
            })
        }).then(() => {
            // create file
            cy.get('#theia-top-panel').should('exist').then(() => {
                cy.theiaCommandPaletteClick('New File').then(() => {
                    // enter name of the folder
                    cy.get('.dialogContent input').type(FILENAME).then(() => {
                        cy.get('button.theia-button.main').click({ force: true });
                    });
                })
            });

        }).then(() => {
            cy.get('.p-Widget.p-TabBar.theia-app-centers.theia-app-main.ps').contains(FILENAME).click({ force: true });
        }).then(() => {
            cy.window().then((win: any) => {
                win.monaco.editor.getModels()[0].setValue('export class HelloWorld {\n  constructor() {}\n foo(): invalid {\n }\n}\n');
            })

        }).then(() => {
            // now, search if there is an invalid syntax
            cy.get('.lines-content.monaco-editor-background').contains('invalid').should('have.class', 'mtk11');
        });
    })
});

export function makeid() {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < 5; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}
