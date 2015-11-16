#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.ide.Constants;

import com.google.inject.Singleton;

import static ${package}.${yourPrefix}Attributes.${yourPrefix}_PROJECT_TYPE_ID;
import static ${package}.${yourPrefix}Attributes.${yourPrefix}_PROJECT_TYPE_NAME;
import static ${package}.${yourPrefix}Attributes.PROGRAMMING_LANGUAGE;

@Singleton
public class ${yourPrefix}ProjectType extends ProjectType {

    public ${yourPrefix}ProjectType() {
        super(${yourPrefix}_PROJECT_TYPE_ID, ${yourPrefix}_PROJECT_TYPE_NAME, true, false);

        addConstantDefinition(Constants.LANGUAGE, "language", PROGRAMMING_LANGUAGE);

        setDefaultBuilder("maven");
        setDefaultRunner("system:/${yourPrefix}projecttype/standalone");
    }
}
