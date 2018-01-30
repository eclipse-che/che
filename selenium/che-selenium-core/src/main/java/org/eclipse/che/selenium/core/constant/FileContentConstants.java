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
package org.eclipse.che.selenium.core.constant;

public final class FileContentConstants {
  public static final String PROJECT_FILE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<projectDescription>\n"
          + "        <name>%s</name>\n"
          + "        <comment></comment>\n"
          + "        <projects>\n"
          + "        </projects>\n"
          + "        <buildSpec>\n"
          + "        </buildSpec>\n"
          + "        <natures>\n"
          + "                <nature>org.eclipse.jdt.core.javanature</nature>\n"
          + "        </natures>\n"
          + "</projectDescription>";
  public static final String CLASSPATH_FILE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<classpath>\n"
          + "        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n"
          + "        <classpathentry kind=\"src\" path=\"src\"/>\n"
          + "        <classpathentry kind=\"output\" path=\"bin\"/>\n"
          + "</classpath>";
}
