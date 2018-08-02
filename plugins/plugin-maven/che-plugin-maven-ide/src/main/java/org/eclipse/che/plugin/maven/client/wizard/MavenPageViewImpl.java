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
package org.eclipse.che.plugin.maven.client.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.ui.listbox.CustomListBox;
import org.eclipse.che.plugin.maven.client.MavenArchetype;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/** @author Evgen Vidolob */
public class MavenPageViewImpl implements MavenPageView {

  private static MavenPageViewImplUiBinder ourUiBinder =
      GWT.create(MavenPageViewImplUiBinder.class);
  private final DockLayoutPanel rootElement;
  @UiField Style style;
  @UiField TextBox versionField;
  @UiField TextBox groupId;
  @UiField TextBox artifactId;
  @UiField Button artifactIdTooltipButton;
  @UiField Button groupIdTooltipButton;
  @UiField Label packagingLabel;
  @UiField CustomListBox packagingField;
  @UiField CheckBox generateFromArchetype;
  @UiField Label archetypeLabel;
  @UiField CustomListBox archetypeField;

  private ActionDelegate delegate;
  private List<MavenArchetype> archetypes;

  @Inject
  public MavenPageViewImpl(MavenLocalizationConstant localizedConstant) {
    rootElement = ourUiBinder.createAndBindUi(this);
    archetypes = new ArrayList<>();

    artifactId.setFocus(true);

    packagingField.addItem("not specified", "");
    packagingField.addItem("JAR", "jar");
    packagingField.addItem("WAR", "war");
    packagingField.addItem("POM", "pom");
    packagingField.setSelectedIndex(0);
    generateFromArchetype.setValue(false);

    final Element artifactIdTooltip = DOM.createSpan();
    artifactIdTooltip.setInnerText(localizedConstant.mavenPageArtifactIdTooltip());

    artifactIdTooltipButton.addMouseOverHandler(
        new MouseOverHandler() {
          @Override
          public void onMouseOver(MouseOverEvent event) {
            final Element link = event.getRelativeElement();
            if (!link.isOrHasChild(artifactIdTooltip)) {
              link.appendChild(artifactIdTooltip);
            }
          }
        });
    artifactIdTooltipButton.addStyleName(style.tooltip());

    final Element groupIdTooltip = DOM.createSpan();
    groupIdTooltip.setInnerText(localizedConstant.mavenPageGroupIdTooltip());

    groupIdTooltipButton.addMouseOverHandler(
        new MouseOverHandler() {
          @Override
          public void onMouseOver(MouseOverEvent event) {
            final Element link = event.getRelativeElement();
            if (!link.isOrHasChild(groupIdTooltip)) {
              link.appendChild(groupIdTooltip);
            }
          }
        });
    groupIdTooltipButton.addStyleName(style.tooltip());
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  @Override
  public String getArtifactId() {
    return artifactId.getText();
  }

  @Override
  public void setArtifactId(String artifactId) {
    this.artifactId.setText(artifactId);
  }

  @Override
  public String getVersion() {
    return versionField.getText();
  }

  @Override
  public void setVersion(String value) {
    versionField.setText(value);
  }

  @Override
  public String getPackaging() {
    return packagingField.getValue(packagingField.getSelectedIndex());
  }

  @Override
  public void setPackaging(String packaging) {
    for (int i = 0; i < packagingField.getItemCount(); i++) {
      if (packaging.equals(packagingField.getValue(i))) {
        packagingField.setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  public MavenArchetype getArchetype() {
    final String coordinates = archetypeField.getValue(archetypeField.getSelectedIndex());
    for (MavenArchetype archetype : archetypes) {
      if (coordinates.equals(archetype.toString())) {
        return archetype;
      }
    }
    return null;
  }

  @Override
  public void setArchetypes(List<MavenArchetype> archetypes) {
    this.archetypes.clear();
    this.archetypes.addAll(archetypes);
    archetypeField.clear();
    for (MavenArchetype archetype : archetypes) {
      archetypeField.addItem(archetype.toString(), archetype.toString());
    }
  }

  @Override
  public void setPackagingVisibility(boolean visible) {
    packagingLabel.setVisible(visible);
    packagingField.setVisible(visible);
  }

  @Override
  public void setArchetypeSectionVisibility(boolean visible) {
    generateFromArchetype.setVisible(visible);
    archetypeLabel.setVisible(visible);
    archetypeField.setVisible(visible);
  }

  @Override
  public void enableArchetypes(boolean enabled) {
    archetypeField.setEnabled(enabled);
  }

  @Override
  public boolean isGenerateFromArchetypeSelected() {
    return generateFromArchetype.getValue();
  }

  @Override
  public String getGroupId() {
    return groupId.getText();
  }

  @Override
  public void setGroupId(String group) {
    groupId.setText(group);
  }

  @UiHandler({"versionField", "groupId", "artifactId"})
  void onKeyUp(KeyUpEvent event) {
    delegate.onCoordinatesChanged();
  }

  @UiHandler("packagingField")
  void onPackagingChanged(ChangeEvent event) {
    delegate.packagingChanged(getPackaging());
  }

  @UiHandler({"generateFromArchetype"})
  void generateFromArchetypeHandler(ValueChangeEvent<Boolean> event) {
    delegate.generateFromArchetypeChanged(generateFromArchetype.getValue());
  }

  @UiHandler("archetypeField")
  void onArchetypeChanged(ChangeEvent event) {
    delegate.archetypeChanged(getArchetype());
  }

  @Override
  public void showArtifactIdMissingIndicator(boolean doShow) {
    if (doShow) {
      artifactId.addStyleName(style.inputError());
    } else {
      artifactId.removeStyleName(style.inputError());
    }
  }

  @Override
  public void showGroupIdMissingIndicator(boolean doShow) {
    if (doShow) {
      groupId.addStyleName(style.inputError());
    } else {
      groupId.removeStyleName(style.inputError());
    }
  }

  @Override
  public void showVersionMissingIndicator(boolean doShow) {
    if (doShow) {
      versionField.addStyleName(style.inputError());
    } else {
      versionField.removeStyleName(style.inputError());
    }
  }

  @Override
  public void clearArchetypes() {
    archetypes.clear();
    archetypeField.clear();
  }

  interface MavenPageViewImplUiBinder extends UiBinder<DockLayoutPanel, MavenPageViewImpl> {}

  interface Style extends CssResource {
    String inputError();

    String tooltip();
  }
}
