/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.client.module;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.maven.client.MavenArchetype;
import org.eclipse.che.ide.ui.buttonLoader.ButtonLoaderResources;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class CreateMavenModuleViewImpl extends Window implements CreateMavenModuleView {
    public static final String                            CREATE      = "Create";
    private static      CreateMavenModuleViewImplUiBinder ourUiBinder = GWT.create(CreateMavenModuleViewImplUiBinder.class);
    private final Button createButton;
    @UiField
    CheckBox                       generateFromArchetype;
    @UiField
    Label                          archetypeLabel;
    @UiField
    ListBox                        archetypeField;
    @UiField
    TextBox                        parentArtifactId;
    @UiField
    TextBox                        nameField;
    @UiField
    TextBox                        artifactId;
    @UiField
    TextBox                        groupIdField;
    @UiField
    TextBox                        versionField;
    @UiField
    Label                          packagingLabel;
    @UiField
    ListBox                        packagingField;
    @UiField(provided = true)
    CreateMavenModuleResources.Css styles;
    private List<MavenArchetype> archetypes;
    private ActionDelegate       delegate;

    @Inject
    public CreateMavenModuleViewImpl(CreateMavenModuleResources resources,
                                     ButtonLoaderResources buttonLoaderResources) {
        super(true);
        styles = resources.css();
        styles.ensureInjected();
        archetypes = new ArrayList<>();
        setTitle("Create Maven Module");
        FlowPanel rootElement = ourUiBinder.createAndBindUi(this);
        setWidget(rootElement);
        createButton = createPrimaryButton(CREATE, "mavenPageView-createButton", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.create();
            }
        });
        addButtonToFooter(createButton);
        createButton.addStyleName(buttonLoaderResources.Css().buttonLoader());
    }

    @UiHandler("nameField")
    void onNameChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        delegate.projectNameChanged(nameField.getText());
    }

    @UiHandler("artifactId")
    void onArtifactChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        delegate.artifactIdChanged(artifactId.getText());
    }

    @UiHandler({"generateFromArchetype"})
    void generateFromArchetypeHandler(ValueChangeEvent<Boolean> event) {
        delegate.generateFromArchetypeChanged(generateFromArchetype.getValue());
    }

    @Override
    protected void onClose() {
        delegate.onClose();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
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
    public void enableArchetypes(boolean enabled) {
        archetypeField.setEnabled(enabled);
    }

    @Override
    public boolean isGenerateFromArchetypeSelected() {
        return generateFromArchetype.getValue();
    }

    @Override
    public void setParentArtifactId(String artifactId) {
        parentArtifactId.setValue(artifactId);
    }

    @Override
    public void setCreateButtonEnabled(boolean enabled) {
        createButton.setEnabled(enabled);
    }

    @Override
    public void setNameError(boolean hasError) {
        if (hasError) {
            nameField.addStyleName(styles.inputError());
        } else {
            nameField.removeStyleName(styles.inputError());
        }
    }

    @Override
    public void setArtifactIdError(boolean hasError) {
        if (hasError) {
            artifactId.addStyleName(styles.inputError());
        } else {
            artifactId.removeStyleName(styles.inputError());
        }
    }

    @Override
    public void reset() {
        nameField.setValue("");
        artifactId.setValue("");
        generateFromArchetype.setValue(false);
        archetypes.clear();
        archetypeField.clear();
    }

    @Override
    public String getPackaging() {
        return packagingField.getValue(packagingField.getSelectedIndex());
    }

    @Override
    public String getGroupId() {
        return groupIdField.getText();
    }

    @Override
    public void setGroupId(String groupId) {
        groupIdField.setValue(groupId);
    }

    @Override
    public String getVersion() {
        return versionField.getText();
    }

    @Override
    public String getArtifactId() {
        return artifactId.getText();
    }

    @Override
    public String getName() {
        return nameField.getText();
    }

    @Override
    public void setVersion(String version) {
        versionField.setValue(version);
    }

    @Override
    public void setPackagingVisibility(boolean visible) {
        packagingLabel.setVisible(visible);
        packagingField.setVisible(visible);
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void showButtonLoader(boolean showLoader) {
        if (showLoader) {
            createButton.setEnabled(false);
            createButton.setHTML("<i></i>");
        } else {
            createButton.setEnabled(true);
            createButton.setText(CREATE);
        }
    }

    @Override
    public void clearArchetypes() {
        archetypes.clear();
        archetypeField.clear();
    }

    interface CreateMavenModuleViewImplUiBinder extends UiBinder<FlowPanel, CreateMavenModuleViewImpl> {
    }
}
