/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.json;

/** @author andrew00x */
/*
From docker source code 'runconfig/config.go':
ExposedPorts    map[nat.Port]struct{}

ExposedPorts in JSON response from docker remote API:
...,
"ExposedPorts":{
    "22/tcp": {}
},
...

It seems struct{} is reserved for future but it isn't in use for now.
*/
public class ExposedPort {
  public String toString() {
    return "{}";
  }
}
