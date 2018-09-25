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
package org.eclipse.che.plugin.maven.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Represents the localization constants contained in resource bundle:
 * 'MavenLocalizationConstant.properties'.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
public interface MavenLocalizationConstant extends Messages {

  /* Actions */
  @Key("action.effectivePom.title")
  String actionGetEffectivePomTitle();

  @Key("action.effectivePom.description")
  String actionGetEffectivePomDescription();

  @Key("action.reimportDependencies.title")
  String actionReimportDependenciesTitle();

  @Key("action.reimportDependencies.description")
  String actionReimportDependenciesDescription();

  /* MavenCommandPageView */
  @Key("view.mavenCommandPage.arguments.text")
  String mavenCommandPageViewArgumentsText();

  @Key("maven.page.artifactIdTooltip")
  String mavenPageArtifactIdTooltip();

  @Key("maven.page.groupIdTooltip")
  String mavenPageGroupIdTooltip();

  @Key("maven.class.decompiled")
  String mavenClassDecompiled();

  @Key("maven.class.download.sources")
  String mavenDownloadSources();

  @Key("maven.class.download.failed")
  String mavenClassDownloadFailed(String fqn);

  @Key("loader.action.name")
  String loaderActionName();

  @Key("loader.action.description")
  String loaderActionDescription();

  @Key("window.loader.title")
  String windowLoaderTitle();

  @Key("maven.page.estimate.errorMessage")
  String mavenPageEstimateErrorMessage();

  @Key("maven.page.errorDialog.title")
  String mavenPageErrorDialogTitle();

  @Key("maven.page.archetype.disabled.title")
  String mavenPageArchetypeDisabledTitle();

  @Key("maven.page.archetype.disabled.message")
  String mavenPageArchetypeDisabledMessage();

  /* Preferences page*/
  @Key("maven.preferences.title")
  String mavenPreferencesTitle();

  @Key("maven.preferences.show.artifact.id.checkbox.text")
  String mavenPreferencesShowArtifactIdCheckboxText();
}
