#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.eclipse.che.ide.theme.DarkTheme;

public class ${yourPrefix}Theme extends DarkTheme {

    @Override
    public String getId() {
        return "${yourPrefix} Theme id";
    }

    @Override
    public String getDescription() {
        return "${yourPrefix} new Che Theme";
    }

    @Override
    public String getMainFontColor() {
        return "red";
    }

    @Override
    public String getPartBackground() {
        return "white";
    }

    @Override
    public String getTabsPanelBackground() {
        return "white";
    }
}
