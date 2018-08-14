/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */
/**
 * Error message used to provide custom exit code
 * @author Florent Benoit
 */
export class ErrorMessage {

    private exitCode? : number = 1;

    private error : any;

    constructor(error: any, exitCode : number) {
        this.error = error;
        if (exitCode) {
            this.exitCode = exitCode;
        }
    }

    getExitCode() : number {
        return this.exitCode;
    }

    getError() : any {
        return this.error;
    }
}
