/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.theme;

/**
 * Theme is a collection of colors, fonts and supporting data that may be used by plugins to help provide uniform look and feel to their
 * components. <b>Note:</b><br/>
 * In this interface color means CSS color i.e : #eeeeee, rgb(1,1,1), rgba(1,1,1,1), red etc.
 *
 * @author Evgen Vidolob
 */
public interface Theme {

    /**
     * @return the theme id
     */
    String getId();

    /**
     * @return the description of the theme
     */
    String getDescription();

    /**
     * Global background color.
     *
     * @return the color
     */
    String backgroundColor();

    /**
     * return the logo color
     */
    String getLogoFill();

    /**
     * This color used in toolbar for highlight hovered items
     *
     * @return the color
     */
    String hoverBackgroundColor();

    /**
     * Background color for an item selected with the keyboard
     *
     * @return the color
     */
    String keyboardSelectionBackgroundColor();

    /**
     * Background color for selected items in CellWidgets, menu, toolbar
     *
     * @return the color
     */
    String selectionBackground();

    /**
     * Background color for selected text in input fields.
     *
     * @return the color
     */
    String inputSelectionBackground();

    /**
     * Background color for inactive selection.
     *
     * @return the color
     */
    String inactiveSelectionBackground();

    /**
     * Background color for inactive(not selected part button)
     *
     * @return the color
     */
    String inactiveTabBackground();

    /**
     * Border color for inactive(not selected part button)
     *
     * @return the color
     */
    String inactiveTabBorderColor();

    /**
     * Background color for active (selected) part button
     *
     * @return the color
     */
    String activeTabBackground();

    /**
     * Border color for active (selected) part button
     *
     * @return the color
     */
    String activeTabBorderColor();

    /**
     * Text color for active (selected) part button
     *
     * @return the color
     */
    String activeTabTextColor();

    /**
     * Text shadow for active tab.
     *
     * @return text shadow
     */
    String activeTabTextShadow();

    /**
     * Icon color in active (selected) tab.
     *
     * @return color for icon
     */
    String activeTabIconColor();

    /**
     * Text color for part button.
     *
     * @return the color
     */
    String tabTextColor();

    /**
     * Text color for hovered part button.
     *
     * @return the color
     */
    String hoveredTabTextColor();

    /**
     * Icon color for editor tab.
     *
     * @return color
     */
    String getEditorTabIconColor();

    /**
     * Background color for active (selected) editor tab.
     *
     * @return color
     */
    String activeEditorTabBackgroundColor();

    /**
     * Background color for readonly editor tab.
     *
     * @return color
     */
    String editorReadonlyTabBackgroundColor();

    /**
     * Background color for readonly active (selected) editor tab.
     *
     * @return color
     */
    String activeEditorReadonlyTabBackgroundColor();

    /**
     * Background color for focused editor tab.
     *
     * @return color
     */
    String focusedEditorTabBackgroundColor();

    /**
     * Bottom border (underline) color for focused editor tab.
     *
     * @return color
     */
    String focusedEditorTabBorderBottomColor();

    /**
     * Background color of part stack panel(where placed part button)
     *
     * @return the color
     */
    String tabsPanelBackground();

    /**
     * Border color of the tab(part button)
     *
     * @return the color
     */
    String tabBorderColor();

    /**
     * Color to underline active tab
     *
     * @return
     */
    String tabUnderlineColor();

    /**
     * Background color of the tab(part button)
     *
     * @return
     */
    String partBackground();

    /**
     * Background color of the part toolbar panel
     *
     * @return the color
     */
    String partToolbar();

    /**
     * Background color for selected (active) part toolbar
     *
     * @return the color
     */
    String partToolbarActive();

    /**
     * Shadow color(css box-shadow property) of the part toolbar
     *
     * @return the color
     */
    String partToolbarShadow();

    /**
     * First top line's color of the part toolbar separator.
     *
     * @return the color
     */
    String partToolbarSeparatorTopColor();

    /**
     * Second bottom line's color of the part toolbar separator.
     *
     * @return the color
     */
    String partToolbarSeparatorBottomColor();

    /**
     * Color of the main font
     *
     * @return the color
     */
    String getMainFontColor();

    /**
     * Background color of the {@link com.google.gwt.user.client.ui.RadioButton}
     *
     * @return the color
     */
    String getRadioButtonBackgroundColor();

    /**
     * Background color of the disabled menu item
     *
     * @return the color
     */
    String getDisabledMenuColor();

    /**
     * Background color of the {@link com.google.gwt.user.client.ui.DialogBox}
     *
     * @return the color
     */
    String getDialogContentBackground();

    /**
     * Background color of default button.
     *
     * @return the color
     */
    String getButtonBackground();

    /**
     * Border color of default button.
     *
     * @return the color
     */
    String getButtonBorderColor();

    /**
     * Font color of default button.
     *
     * @return the color
     */
    String getButtonFontColor();

    /**
     * Background color of default button hover state.
     *
     * @return the color
     */
    String getButtonHoverBackground();

    /**
     * Border color of default button hover state.
     *
     * @return the color
     */
    String getButtonHoverBorderColor();

    /**
     * Font color of default button hover state.
     *
     * @return the color
     */
    String getButtonHoverFontColor();

    /**
     * Background color of default button clicked state.
     *
     * @return the color
     */
    String getButtonClickedBackground();

    /**
     * Border color of default button clicked state.
     *
     * @return the color
     */
    String getButtonClickedBorderColor();

    /**
     * Font color of default button clicked state.
     *
     * @return the color
     */
    String getButtonClickedFontColor();

    /**
     * Background color of default button disabled state.
     *
     * @return the color
     */
    String getButtonDisabledBackground();

    /**
     * Border color of default button disabled state.
     *
     * @return the color
     */
    String getButtonDisabledBorderColor();

    /**
     * Font color of default button disabled state.
     *
     * @return the color
     */
    String getButtonDisabledFontColor();

    /**
     * Background color of primary button normal state.
     *
     * @return the color
     */
    String getPrimaryButtonBackground();

    /**
     * Border color of primary button normal state.
     *
     * @return the color
     */
    String getPrimaryButtonBorderColor();

    /**
     * Font color of primary button normal state.
     *
     * @return the color
     */
    String getPrimaryButtonFontColor();

    /**
     * Background color of primary button hover state.
     *
     * @return the color
     */
    String getPrimaryButtonHoverBackground();

    /**
     * Border color of primary button hover state.
     *
     * @return the color
     */
    String getPrimaryButtonHoverBorderColor();

    /**
     * Font color of primary button hover state.
     *
     * @return the color
     */
    String getPrimaryButtonHoverFontColor();

    /**
     * Background color of primary button clicked state.
     *
     * @return the color
     */
    String getPrimaryButtonClickedBackground();

    /**
     * Border color of primary button clicked state.
     *
     * @return the color
     */
    String getPrimaryButtonClickedBorderColor();

    /**
     * Font color of primary button clicked state.
     *
     * @return the color
     */
    String getPrimaryButtonClickedFontColor();

    /**
     * Background color of primary button disabled state.
     *
     * @return the color
     */
    String getPrimaryButtonDisabledBackground();

    /**
     * Border color of primary button disabled state.
     *
     * @return the color
     */
    String getPrimaryButtonDisabledBorderColor();

    /**
     * Font color of primary button disabled state.
     *
     * @return the color
     */
    String getPrimaryButtonDisabledFontColor();

    /**
     * Color of the social button for sharing factory.
     *
     * @return the color
     */
    String getSocialButtonColor();

    /**
     * Background for editor panel ( place where the editor will be placed )
     *
     * @return color for editor panel
     */
    String editorPanelBackgroundColor();

    /**
     * @return border color for editor panel
     */
    String editorPanelBorderColor();

    /**
     * Background color of the editor
     *
     * @return the color
     */
    String getEditorBackgroundColor();

    /**
     * Color for highlighted line in editor(where cursor placed)
     *
     * @return the color
     */
    String getEditorCurrentLineColor();

    /**
     * Main font color in the editor
     *
     * @return the color
     */
    String getEditorDefaultFontColor();

    /**
     * Editor selection background color.
     *
     * @return the color
     */
    String getEditorSelectionColor();

    /**
     * Editor inactive selection color(if focus not in browser)
     *
     * @return the color
     */
    String getEditorInactiveSelectionColor();

    /**
     * Color of the editor cursor
     *
     * @return the color
     */
    String getEditorCursorColor();

    /**
     * Background color of the gutter (left or right vertical panels in editor)
     *
     * @return the color
     */
    String getEditorGutterColor();

    /**
     * Color of key word token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorKeyWord();

    /**
     * Color of atom token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorAtom();

    /**
     * Color of number token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorNumber();

    /**
     * Color of def token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorDef();

    /**
     * Color of variable token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorVariable();

    /**
     * Color of variable2 token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorVariable2();

    /**
     * Color of property token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorProperty();

    /**
     * Color of operator token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorOperator();

    /**
     * Color of comment token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorComment();

    /**
     * Color of string token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorString();

    /**
     * Color of meta token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorMeta();

    /**
     * Color of error token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorError();

    /**
     * Color of builtin token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorBuiltin();

    /**
     * Color of tag token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorTag();

    /**
     * Color of attribute token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorAttribute();

    /**
     * Color of string2 token, produced by Codemirror parser
     *
     * @return the color
     */
    String getEditorString2();

    /**
     * Background color of completion popup.
     *
     * @return the color
     */
    String completionPopupBackgroundColor();

    /**
     * Border color of completion popup.
     *
     * @return the color
     */
    String completionPopupBorderColor();

    /**
     * Background color for completion popup header.
     *
     * @return color
     */
    String completionPopupHeaderBackgroundColor();

    /**
     * Text color for completion popup header.
     *
     * @return color
     */
    String completionPopupHeaderTextColor();

    /**
     * Background color of selected or highlighted item for completion popup.
     *
     * @return color
     */
    String completionPopupSelectedItemBackgroundColor();

    /**
     * Item text color for completion popup.
     *
     * @return color
     */
    String completionPopupItemTextColor();

    /**
     * Item subtitle text color for completion popup.
     *
     * @return color
     */
    String completionPopupItemSubtitleTextColor();

    /**
     * Item highlight text color for completion popup.
     *
     * @return color
     */
    String completionPopupItemHighlightTextColor();

    /**
     * Background color of the window widget.
     *
     * @return the color
     */
    String getWindowContentBackground();

    /**
     * Font color of the window widget.
     *
     * @return the color
     */
    String getWindowContentFontColor();

    /**
     * Shadow color of the window widget.
     *
     * @return the color
     */
    String getWindowShadowColor();

    /**
     * Background color of the window header
     *
     * @return the color
     */
    String getWindowHeaderBackground();

    /**
     * Font color of the window header.
     *
     * @return the color
     */
    String getWindowHeaderBorderColor();

    /**
     * Background color of the window footer.
     *
     * @return the color
     */
    String getWindowFooterBackground();

    /**
     * Font color of the window footer.
     *
     * @return the color
     */
    String getWindowFooterBorderColor();

    /**
     * Color of the line separating elements in Window (for footer).
     *
     * @return the color
     */
    String getWindowSeparatorColor();

    /**
     * Font color of window's title.
     *
     * @return the color
     */
    String getWindowTitleFontColor();


    /**
     * New Project wizard steps background color(used in new project wizard, left vertical panel)
     *
     * @return the color
     */
    String getWizardStepsColor();

    /**
     * Border color of the steps panel in new project wizard
     *
     * @return the color
     */
    String getWizardStepsBorderColor();

    /**
     * Color of the Factory link.
     *
     * @return the color
     */
    String getFactoryLinkColor();

    /**
     * Font color for welcome page text
     *
     * @return the color
     */
    String getWelcomeFontColor();

    /**
     * Font color for group captions on view (Example, Share Factory view).
     *
     * @return the color
     */
    String getCaptionFontColor();

    /**
     * Font color for console text
     *
     * @return the color
     */
    String consolePanelColor();

    /**
     * Font color for status panel
     *
     * @return the color
     */
    String getStatusPanelColor();

    /**
     * Background color for odd rows in Cell Widgets
     *
     * @return the color
     */
    String getCellOddRowColor();

    /**
     * Background color for odd rows in Cell Widgets
     *
     * @return the color
     */
    String getCellOddEvenColor();

    /**
     * Background color for keyboard selected rows in Cell Widgets
     *
     * @return the color
     */
    String getCellKeyboardSelectedRowColor();

    /**
     * Background color for hovered rows in Cell Widgets
     *
     * @return
     */
    String getCellHoveredRow();

    /**
     * Background color of menu items
     *
     * @return the color
     */
    String getMainMenuBkgColor();

    /**
     * Background color of selected menu items
     *
     * @return the color
     */
    String getMainMenuSelectedBkgColor();

    /**
     * Border color of selected menu items
     *
     * @return the color
     */
    String getMainMenuSelectedBorderColor();

    /**
     * Font color for menu item text
     *
     * @return the color
     */
    String getMainMenuFontColor();

    /**
     * Font color for menu item hover text
     *
     * @return the color
     */
    String getMainMenuFontHoverColor();

    /**
     * Font color for menu item selected text
     *
     * @return the color
     */
    String getMainMenuFontSelectedColor();

    String getNotableButtonTopColor();

    String getNotableButtonColor();

    /**
     * @return the color of border shadow
     */
    String tabBorderShadow();

    /**
     * @return the color of tree file text
     */
    String treeTextFileColor();

    /**
     * @return the color of tree folder text
     */
    String treeTextFolderColor();

    /**
     * @return the color of tree text-shadow
     */
    String treeTextShadow();

    /**
     * @return the color of tree icon file
     */
    String treeIconFileColor();

    /**
     * The color of the action group shadow.
     *
     * @return {@link String} color
     */
    String getToolbarActionGroupShadowColor();

    /**
     * The color of the action group background.
     *
     * @return {@link String} color
     */
    String getToolbarActionGroupBackgroundColor();

    /**
     * The color of the action group border.
     *
     * @return {@link String} color
     */
    String getToolbarActionGroupBorderColor();

    /**
     * The background image of the toolbar.
     *
     * @return {@link String} background image
     */
    String getToolbarBackgroundImage();

    /**
     * The color of the toolbar background.
     *
     * @return {@link String} color
     */
    String getToolbarBackgroundColor();

    /**
     * The color of the toolbar icons.
     *
     * @return {@link String} color
     */
    String getToolbarIconColor();

    /**
     * The color of the toolbar icons if hover.
     *
     * @return {@link String} hover color
     */
    String getToolbarHoverIconColor();


    /**
     * The filter of the toolbar icons if select.
     *
     * @return {@link String} select filter
     */
    String getToolbarSelectedIconFilter();

    /**
     * The color of the tooltip background.
     *
     * @return {@link String} color
     */
    String getTooltipBackgroundColor();

    /**
     * The background color of the perspective switcher if checked.
     *
     * @return {@link String} color
     */
    String getPerspectiveSwitcherBackgroundColor();

    /**
     * The icon color for the selectCommandAction.
     *
     * @return {@link String} color
     */
    String getSelectCommandActionIconColor();

    /**
     * The background color for selectCommandAction icon.
     *
     * @return {@link String} color
     */
    String getSelectCommandActionIconBackgroundColor();

    /**
     * The command action color.
     *
     * @return {@link String} color
     */
    String getSelectCommandActionColor();

    /**
     * The command action hover color.
     *
     * @return {@link String} color
     */
    String getSelectCommandActionHoverColor();

    /**
     * @return the progress icon color
     */
    String progressColor();

    /**
     * @return the color of the success event
     */
    String getSuccessEventColor();

    /**
     * @return the color of the error event
     */
    String getErrorEventColor();

    /**
     * @return the color of the links
     */
    String getLinkColor();

    /**
     * @return the color of the event items delimeter
     */
    String getDelimeterColor();

    /**
     * Background color for processes panel.
     *
     * @return color
     */
    String processTreeBackgroundColor();

    /**
     * Background color for toolbar in consoles panel.
     *
     * @return color
     */
    String consolesToolbarBackground();

    /**
     * Border color for consoles toolbar border.
     *
     * @return color
     */
    String colsolesToolbarBorderColor();

    /**
     * Button color on consoles toolbar.
     *
     * @return color
     */
    String consolesToolbarButtonColor();

    /**
     * Hovered button color on consoles toolbar.
     *
     * @return color
     */
    String consolesToolbarHoveredButtonColor();

    /**
     * Disabled button on the consoles toolbar.
     *
     * @return color
     */
    String consolesToolbarDisabledButtonColor();

    /**
     * Toggled button on the consoles toolbar.
     *
     * @return color
     */
    String consolesToolbarToggledButtonColor();

    /**
     * Text color for DEV label in process tree.
     *
     * @return color
     */
    String processTreeDevLabel();

    /**
     * Background color for console output area.
     *
     * @return color
     */
    String outputBackgroundColor();

    /**
     * @return the color of the output font.
     */
    String getOutputFontColor();

    /**
     * @return the color of output link.
     */
    String getOutputLinkColor();

    /**
     * @return the background color for editor info panel.
     */
    String getEditorInfoBackgroundColor();

    /**
     * @return text color for editor info panel
     */
    String editorInfoTextColor();

    /**
     * @return the color of border for editor info panel.
     */
    String getEditorInfoBorderColor();

    /**
     * @return the color of border shadow for editor info panel.
     */
    String getEditorInfoBorderShadowColor();

    /**
     * @return the color of the line numbers in the editor gutter.
     */
    String getEditorLineNumberColor();

    /**
     * @return background color of the line numbers gutter
     */
    String editorGutterLineNumberBackgroundColor();

    /**
     * @return the color of the separator line between the gutter and the editor.
     */
    String getEditorSeparatorColor();

    /**
     * Splitter small border color
     *
     * @return
     */
    String getSplitterSmallBorderColor();

    /**
     * Splitter large border color
     *
     * @return
     */
    String getSplitterLargeBorderColor();

    /**
     * Color of badge background.
     *
     * @return badge background color
     */
    String getBadgeBackgroundColor();

    /**
     * Color of badge font.
     *
     * @return badge font color
     */
    String getBadgeFontColor();

    /**
     * Process badge border color.
     */
    String processBadgeBorderColor();

    /**
     * @return the blue color for icon.
     */
    String getBlueIconColor();

    /**
     * @return the red color for icon.
     */
    String getRedIconColor();

    /**
     * @return the color of the popup background.
     */
    String getPopupBkgColor();

    /**
     * @return the color of the popup border.
     */
    String getPopupBorderColor();

    /**
     * @return the color of the popup shadow.
     */
    String getPopupShadowColor();

    /**
     * @return the color of the popup hover.
     */
    String getPopupHoverColor();

    /**
     * @return the font color of the popup hot key.
     */
    String getPopupHotKeyColor();

    /**
     * @return the text field title color.
     */
    String getTextFieldTitleColor();

    /**
     * @return the text field color.
     */
    String getTextFieldColor();

    /**
     * @return the text field background color.
     */
    String getTextFieldBackgroundColor();

    /**
     * @return the text field focused color.
     */
    String getTextFieldFocusedColor();

    /**
     * @return the text field focused background color.
     */
    String getTextFieldFocusedBackgroundColor();

    /**
     * @return the text field disabled color.
     */
    String getTextFieldDisabledColor();

    /**
     * @return the text field disabled background color.
     */
    String getTextFieldDisabledBackgroundColor();

    /**
     * @return the text field border color.
     */
    String getTextFieldBorderColor();

    /**
     * @return the menu background color.
     */
    String getMenuBackgroundColor();

    /**
     * @return the menu background image.
     */
    String getMenuBackgroundImage();

    /**
     * @return the panel background color.
     */
    String getPanelBackgroundColor();

    /**
     * @return the primary highlights color.
     */
    String getPrimaryHighlightColor();

    /**
     * Default icon color
     *
     * @return color
     */
    String iconColor();

    /**
     * Active icon color
     *
     * @return color
     */
    String activeIconColor();

    /**
     * @return the separator color.
     */
    String getSeparatorColor();

    /**
     * @return the error state color.
     */
    String getErrorColor();

    /**
     * @return the success state color.
     */
    String getSuccessColor();

    /**
     * @return the list box background color if hover.
     */
    String getListBoxHoverBackgroundColor();

    /**
     * @return the list box color.
     */
    String getListBoxColor();

    /**
     * @return the list box disabled color.
     */
    String getListBoxDisabledColor();

    /**
     * @return the list box disabled background color.
     */
    String getListBoxDisabledBackgroundColor();

    /**
     * @return the list box dropdown background color.
     */
    String getListBoxDropdownBackgroundColor();

    /**
     * @return the list box dropdown shadow color.
     */
    String listBoxDropdownShadowColor();

    /**
     * @return the categories list header text color.
     */
    String categoriesListHeaderTextColor();

    /**
     * @return the categories list header icon color.
     */
    String categoriesListHeaderIconColor();

    /**
     * @return the categories list header background color.
     */
    String categoriesListHeaderBackgroundColor();

    /**
     * @return the categories list item color.
     */
    String categoriesListItemTextColor();

    /**
     * @return the categories list item background color.
     */
    String categoriesListItemBackgroundColor();

    /**
     * @return the scrollbar border color
     */
    String scrollbarBorderColor();

    /**
     * @return scrollbar background color
     */
    String scrollbarBackgroundColor();

    /**
     * @return scrollbar hover background color
     */
    String scrollbarHoverBackgroundColor();

    /**
     * @return matching search block background color
     */
    String matchingSearchBlockBackgroundColor();

    /**
     * @return matching search block border color
     */
    String matchingSearchBlockBorderColor();

    /**
     * @return current search block background color
     */
    String currentSearchBlockBackgroundColor();

    /**
     * @return current search block border color
     */
    String currentSearchBlockBorderColor();

    /********************************************************************************************
     *
     * Dropdown menu with a list of opened files
     *
     ********************************************************************************************/
    String openedFilesDropdownButtonBackground();

    String openedFilesDropdownButtonBorderColor();

    String openedFilesDropdownButtonShadowColor();

    String openedFilesDropdownButtonIconColor();

    String openedFilesDropdownButtonHoverIconColor();

    String openedFilesDropdownButtonActiveBackground();

    String openedFilesDropdownButtonActiveBorderColor();

    String openedFilesDropdownListBackgroundColor();

    String openedFilesDropdownListBorderColor();

    String openedFilesDropdownListShadowColor();

    String openedFilesDropdownListTextColor();

    String openedFilesDropdownListCloseButtonColor();

    String openedFilesDropdownListHoverBackgroundColor();

    String openedFilesDropdownListHoverTextColor();

    /********************************************************************************************
     *
     * RadioButton
     *
     ********************************************************************************************/
    String radioButtonIconColor();

    String radioButtonBorderColor();

    String radioButtonBackgroundColor();

    String radioButtonFontColor();

    String radioButtonDisabledFontColor();

    String radioButtonDisabledIconColor();

    String radioButtonDisabledBackgroundColor();

    /********************************************************************************************
     *
     * Checkbox
     *
     ********************************************************************************************/
    String checkBoxIconColor();

    String checkBoxFontColor();

    String checkBoxBorderColor();

    String checkBoxBackgroundColor();

    String checkBoxDisabledIconColor();

    String checkBoxDisabledFontColor();

    String checkBoxDisabledBackgroundColor();


    /********************************************************************************************
     *
     * Tree
     *
     ********************************************************************************************/
    String treeExpandArrowColor();

    String treeExpandArrowShadow();

    /********************************************************************************************
     *
     * Project Explorer
     *
     ********************************************************************************************/
    String projectExplorerJointContainerFill();

    String projectExplorerJointContainerShadow();

    String projectExplorerPresentableTextShadow();

    String projectExplorerInfoTextShadow();

    String projectExplorerSelectedRowBackground();

    String projectExplorerSelectedRowBorder();

    String projectExplorerHoverRowBackground();

    String projectExplorerHoverRowBorder();

    /********************************************************************************************
     *
     * Loader
     *
     ********************************************************************************************/
    /**
     * @return color of the loader expander.
     */
    String loaderExpanderColor();

    /**
     * The background color for loader icon.
     *
     * @return {@link String} color
     */
    String loaderIconBackgroundColor();

    /**
     * The progress status color for loader.
     *
     * @return {@link String} color
     */
    String loaderProgressStatusColor();

    /**
     * The placeholder color for input fields.
     *
     * @return {@link String} color
     */
    String placeholderColor();

    /********************************************************************************************
     *
     * Category
     *
     ********************************************************************************************/
    String categoryHeaderButtonHoverColor();

    String categoryHeaderButtonColor();

    String categoryElementButtonHoverColor();

    String categoryElementButtonColor();

    String categorySelectElementBackgroundColor();

    String categorySelectElementColor();

    /********************************************************************************************
     *
     * Notification
     *
     ********************************************************************************************/
    String notificationPopupSuccessBackground();

    String notificationPopupFailBackground();

    String notificationPopupProgressBackground();

    String notificationPopupWarningBackground();
    
    String notificationPopupPanelShadow();

    String notificationPopupIconSuccessFill();

    String notificationPopupIconFailFill();

    String notificationPopupIconProgressFill();

    String notificationPopupIconWarningFill();
    
    String notificationPopupIconSvgFill();

    String notificationPopupTextColor();

    String closeNotificationButtonColor();

    String closeNotificationHoveredButtonColor();

    String projectExplorerReadonlyItemBackground();

    String projectExplorerTestItemBackground();

    String editorTabPinBackgroundColor();

    String editorTabPinDropShadow();

    String loaderBackgroundColor();

    String loaderBorderColor();

    String loaderBoxShadow();

    String loaderSVGFill();

    String loaderLabelColor();

    String outputBoxShadow();

    /********************************************************************************************
     *
     * Tool button (part header button)
     *
     ********************************************************************************************/
    String toolButtonColor();

    String toolButtonHoverColor();

    String toolButtonBorder();

    String toolButtonActiveBorder();

    String toolButtonHoverBackgroundColor();

    String toolButtonActiveBackgroundColor();

    String toolButtonHoverBoxShadow();

    String toolButtonActiveBoxShadow();

    /********************************************************************************************
     *
     * VCS output console
     *
     ********************************************************************************************/
    String vcsConsoleStagedFilesColor();

    String vcsConsoleUnstagedFilesColor();

    String vcsConsoleErrorColor();

    String vcsConsoleModifiedFilesColor();

    String vcsConsoleChangesLineNumbersColor();

    String editorPreferenceCategoryBackgroundColor();

    /********************************************************************************************
     *
     * Resource monitors
     *
     ********************************************************************************************/
    String resourceMonitorBarBackground();

    /********************************************************************************************
     *
     * Popup Loader
     *
     ********************************************************************************************/
    String popupLoaderBackgroundColor();

    String popupLoaderBorderColor();

    String popupLoaderShadow();

    String popupLoaderTitleColor();
    String popupLoaderTextColor();

}
