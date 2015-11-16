#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

/**
 * The view of {@link ${yourPrefix}Presenter}.
 *
 */
@ImplementedBy(${yourPrefix}ViewImpl.class)
public interface ${yourPrefix}View extends View<${yourPrefix}View.ActionDelegate> {
    /** Required for delegating functions in view. */
    public interface ActionDelegate {
    }
}