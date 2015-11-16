#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Extension(title = "Theme ${package}", version = "1.0.0")
public class ${yourPrefix}Extension {

    @Inject
    public ${yourPrefix}Extension(WorkspaceAgent workspaceAgent,
        ${yourPrefix}Presenter ${yourPrefix}Presenter) {
        workspaceAgent.openPart(${yourPrefix}Presenter, PartStackType.EDITING);
    }
}
