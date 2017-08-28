/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.rest;

import javax.inject.Inject;
import org.eclipse.che.JavadocUrlProvider;

/** Provide URL for Javadoc service for JDT classes */
public class UrlContextProvider {

  @Inject private static JavadocUrlProvider provider;

  public static String get(String projectPath) {
    return provider.getJavadocUrl(projectPath);
  }
}
