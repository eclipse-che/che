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

/** @author Ann Shumilova */
public enum TestStacksConstants {
  ANDROID("android-default", "Android"),
  JAVA("java-default", "Java"),
  JAVA_MYSQL("java-mysql", "Java-MySQL"),
  BLANK("blank-default", "Blank"),
  BITNAMI_CODEIGNITER("bitnami-codeigniter", "Bitnami Codeigniter"),
  BITNAMI_SYMFONY("bitnami-symfony", "Bitnami Symfony"),
  BITNAMI_PLAY_FOR_JAVA("bitnami-java-play", "Bitnami Play for Java"),
  BITNAMI_RAILS("bitnami-rails", "Bitnami Rails"),
  BITNAMI_EXPRESS("bitnami-express", "Bitnami Express"),
  BITNAMI_LARAVEL("bitnami-laravel", "Bitnami Laravel"),
  BITNAMI_SWIFT("bitnami-swift", "Bitnami Swift"),
  CPP("cpp-default", "C++"),
  DOTNET("dotnet-default", ".NET"),
  ECLIPSE_CHE("che-in-che", "Eclipse Che"),
  NODE("node-default", "Node"),
  PHP("php-default", "PHP"),
  PYTHON("python-default", "Python"),
  RAILS("rails-default", "Rails");

  private final String id;
  private final String name;

  TestStacksConstants(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }
}
