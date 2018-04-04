/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

export class Loader {

    /**
     * Initializes the Loader.
     */
    constructor() {
        /** Show the loader */
        setTimeout(() => {
            document.getElementById('workspace-loader').style.display = "block";
            setTimeout(() => {
                document.getElementById('workspace-loader').style.opacity = "1";
            }, 1);
        }, 1);

        /** Add click handler to maximize output */
        document.getElementById('workspace-console').onclick = () => this.onclick();
    }

    /**
     * Adds a message to output console.
     * 
     * @param message message to log
     */
    log(message: string): void {
        let container = document.getElementById("workspace-console-container");
        if (container.childElementCount > 500) {
            container.removeChild(container.firstChild)
        }

        let element = document.createElement("pre");
        element.innerHTML = message;
        container.appendChild(element);
        if (element.scrollIntoView) {
            element.scrollIntoView();
        }
    }

    onclick(): void {
        if (document.getElementById('workspace-loader').hasAttribute("max")) {
            document.getElementById('workspace-loader').removeAttribute("max");
            document.getElementById('workspace-console').removeAttribute("max");
        } else {
            document.getElementById('workspace-loader').setAttribute("max", "");
            document.getElementById('workspace-console').setAttribute("max", "");
        }
    }

}
