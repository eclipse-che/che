#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

import com.google.inject.Inject;
import com.google.inject.Provider;

import static ${package}.${yourPrefix}Attributes.${yourPrefix}_PROJECT_TYPE_CATEGORY;
import static ${package}.${yourPrefix}Attributes.${yourPrefix}_PROJECT_TYPE_ID;

/**
 * Provides information for registering sample project type into project wizard.
 */
public class ${yourPrefix}Wizard implements ProjectWizardRegistrar {
    private final List<Provider<? extends WizardPage<ImportProject>>> wizardPages;

    @Inject
    public ${yourPrefix}Wizard() {
        wizardPages = new ArrayList<>();
    }

    @NotNull
    public String getProjectTypeId() {
        return ${yourPrefix}_PROJECT_TYPE_ID;
    }

    @NotNull
    public String getCategory() {
        return ${yourPrefix}_PROJECT_TYPE_CATEGORY;
    }

    @NotNull
    public List<Provider<? extends WizardPage<ImportProject>>> getWizardPages() {
        return wizardPages;
    }
}
