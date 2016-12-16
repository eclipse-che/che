/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.ide.project;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.plugin.composer.shared.Constants.PACKAGE;

/**
 * @author Kaloyan Raev
 */
public class ComposerPagePresenter extends AbstractWizardPage<MutableProjectConfig> implements ComposerPageView.ActionDelegate {
    
    private static final String ATTRIBUTE_VALUE_SEPARATOR = ",   ";

    private final ComposerPageView view;

    @Inject
    public ComposerPagePresenter(ComposerPageView view) {
        super();
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public void init(MutableProjectConfig dataObject) {
        super.init(dataObject);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        if (CREATE == wizardMode) {
            setAttribute(PACKAGE, Collections.<String>emptyList());
        }
    }

    @Override
    public boolean isCompleted() {
        return areAttributesCompleted();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        final String projectName = dataObject.getName();

        if (CREATE == wizardMode && projectName != null) {
            updateDelegate.updateControls();
        }

        updateView();
        validateCoordinates();
    }

    @Override
    public void onAttributesChanged() {
        setAttribute(PACKAGE, Arrays.asList(view.getPackage()));

        validateCoordinates();
        updateDelegate.updateControls();
    }

    private List<String> getAttribute(String attrId) {
        Map<String, List<String>> attributes = dataObject.getAttributes();
        List<String> values = attributes.get(attrId);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values;
    }

    private void setAttribute(String attrId, List<String> value) {
        Map<String, List<String>> attributes = dataObject.getAttributes();
        attributes.put(attrId, value);
    }

    private boolean areAttributesCompleted() {
        return !getAttribute(PACKAGE).isEmpty();
    }

    private void updateView() {
        ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        boolean isCreateWizard = wizardMode == CREATE;

        view.changePackageFieldState(isCreateWizard);

        Map<String, List<String>> attributes = dataObject.getAttributes();

        view.setPackage(attributes.get(PACKAGE) == null ? ""
                : convertAttributeValuesToString(getAttribute(PACKAGE)));
    }

    private String convertAttributeValuesToString(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            result.append(value).append(ATTRIBUTE_VALUE_SEPARATOR);
        }

        return result.toString().isEmpty() ? result.toString()
                : result.delete(result.lastIndexOf(ATTRIBUTE_VALUE_SEPARATOR), result.length()).toString();
    }

    private void validateCoordinates() {
        view.showPackageMissingIndicator(view.getPackage().isEmpty());
    }
}
