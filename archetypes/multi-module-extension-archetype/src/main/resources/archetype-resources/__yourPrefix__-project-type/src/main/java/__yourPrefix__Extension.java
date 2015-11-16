#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static ${package}.${yourPrefix}Attributes.${yourPrefix}_PROJECT_TYPE_CATEGORY;

@Singleton
@Extension(title = "${yourPrefix} Project Type Extension", version = "1.0.0")
public class ${yourPrefix}Extension {

    @Inject
    public ${yourPrefix}Extension(${yourPrefix}Resources resources, IconRegistry iconRegistry) {
        iconRegistry.registerIcon(new Icon(${yourPrefix}_PROJECT_TYPE_CATEGORY + ".samples.category.icon",
                resources.${yourPrefix}ProjectTypeIcon()));
    }
}
