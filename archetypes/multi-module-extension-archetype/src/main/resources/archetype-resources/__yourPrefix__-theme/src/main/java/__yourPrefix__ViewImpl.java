#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The implementation of {@link ${yourPrefix}View}.
 *
 */
public class ${yourPrefix}ViewImpl extends Composite implements ${yourPrefix}View {
    interface ${yourPrefix}ViewImplUiBinder extends UiBinder<Widget, ${yourPrefix}ViewImpl> {
    }

    @Inject
    public ${yourPrefix}ViewImpl(${yourPrefix}ViewImplUiBinder ourUiBinder) {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        // do nothing
    }
}