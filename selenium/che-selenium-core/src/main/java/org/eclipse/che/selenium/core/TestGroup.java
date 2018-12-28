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
package org.eclipse.che.selenium.core;

/** @author Dmytro Nochevnov */
public interface TestGroup {
  String MULTIUSER = "multiuser";
  String SINGLEUSER = "singleuser";
  String OPENSHIFT = "openshift";
  String DOCKER = "docker";
  String GITHUB = "github";
  String OSIO = "osio";
  String K8S = "k8s";
  String UNDER_REPAIR = "under_repair";
  String FLAKY = "flaky";
}
