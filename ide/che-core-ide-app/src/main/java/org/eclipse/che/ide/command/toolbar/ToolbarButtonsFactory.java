package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.safehtml.shared.SafeHtml;

/** Factory for the buttons placed on Commands Toolbar. */
public interface ToolbarButtonsFactory {

    OpenCommandsPaletteButton createOpenPaletteButton(SafeHtml content);
}
