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
package org.eclipse.che.plugin.maven.shared;

/** @author Evgen Vidolob */
public interface MavenAttributes {
  String MAVEN_ID = "maven";
  String MAVEN_NAME = "Maven";

  String GENERATION_STRATEGY_OPTION = "type";

  String SIMPLE_GENERATION_STRATEGY = "simple";
  String ARCHETYPE_GENERATION_STRATEGY = "archetype";

  String ARCHETYPE_GROUP_ID_OPTION = "archetypeGroupId";
  String ARCHETYPE_ARTIFACT_ID_OPTION = "archetypeArtifactId";
  String ARCHETYPE_VERSION_OPTION = "archetypeVersion";
  String ARCHETYPE_REPOSITORY_OPTION = "archetypeRepository";

  String GROUP_ID = "maven.groupId";
  String ARTIFACT_ID = "maven.artifactId";
  String VERSION = "maven.version";
  String PACKAGING = "maven.packaging";
  String PARENT_GROUP_ID = "maven.parent.groupId";
  String PARENT_ARTIFACT_ID = "maven.parent.artifactId";
  String PARENT_VERSION = "maven.parent.version";

  String TEST_SOURCE_FOLDER = "maven.test.source.folder";
  String RESOURCE_FOLDER = "maven.resource.folder";

  String DEFAULT_SOURCE_FOLDER = "src/main/java";
  String DEFAULT_RESOURCES_FOLDER = "src/main/resources";
  String DEFAULT_TEST_SOURCE_FOLDER = "src/test/java";
  String DEFAULT_TEST_RESOURCES_FOLDER = "src/test/resources";
  String DEFAULT_VERSION = "1.0-SNAPSHOT";
  String DEFAULT_PACKAGING = "jar";
  String DEFAULT_OUTPUT_FOLDER = "target/classes";

  String DEFAULT_OUTPUT_DIRECTORY = "target/classes";
  String DEFAULT_TEST_OUTPUT_DIRECTORY = "target/test-classes";

  String POM_XML = "pom.xml";

  /** Name of WebSocket chanel */
  String MAVEN_CHANEL_NAME = "maven:workspace";

  String MAVEN_OUTPUT_UNSUBSCRIBE = "mavenOutput/unsubscribe";
  String MAVEN_OUTPUT_SUBSCRIBE = "mavenOutput/subscribe";
  String MAVEN_OUTPUT_TEXT_METHOD = "mavenOutput/text";
  String MAVEN_OUTPUT_UPDATE_METHOD = "mavenOutput/update";
  String MAVEN_OUTPUT_START_STOP_METHOD = "mavenOutput/start_stop";
  String MAVEN_OUTPUT_PERCENT_METHOD = "mavenOutput/percent";
  String MAVEN_OUTPUT_PERCENT_UNDEFINED_METHOD = "mavenOutput/percentUndefined";

  String MAVEN_ARCHETYPE_CHANEL_UNSUBSCRIBE = "mavenArchetype/unsubscribe";
  String MAVEN_ARCHETYPE_CHANEL_SUBSCRIBE = "mavenArchetype/subscribe";
  String MAVEN_ARCHETYPE_CHANEL_OUTPUT = "mavenArchetype/output";
}
