/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
