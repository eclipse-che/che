#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

public interface ${yourPrefix}Resources extends ClientBundle {

    @Source("${package}/${yourPrefix}Extension.svg")
    SVGResource ${yourPrefix}ProjectTypeIcon();
}
