/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client.json;

/**
 * @author andrew00x
 */
/*
From docker source code 'runconfig/config.go':
Volumes         map[string]struct{}

Volumes in JSON response from docker remote API:
...,
"Volumes":{
        "/tmp": {}
 },
...

It seems struct{} is reserved for future but it isn't in use for now.
*/
public class Volume {
    public String toString() {
        return "{}";
    }
}
