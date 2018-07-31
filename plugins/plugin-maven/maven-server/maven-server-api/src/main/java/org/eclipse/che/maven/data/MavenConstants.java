/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.maven.data;

/**
 * Contains some well known maven constants
 *
 * @author Evgen Vidolob
 */
public interface MavenConstants {
  String SNAPSHOT = "SNAPSHOT";
  String LATEST = "LATEST";
  String RELEASE = "RELEASE";
  String POM_EXTENSION = "pom";
  String PROFILE_FROM_POM = "pom";
  String POM_FILE_NAME = "pom.xml";
}
