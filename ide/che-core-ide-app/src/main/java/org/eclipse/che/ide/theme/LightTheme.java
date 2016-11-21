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
package org.eclipse.che.ide.theme;

import com.google.inject.Singleton;

import org.eclipse.che.ide.api.theme.Theme;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class LightTheme implements Theme {

    public static final String LIGHT_THEME_ID = "LightTheme";

    @Override
    public String getId() {
        return LIGHT_THEME_ID;
    }

    @Override
    public String getDescription() {
        return "Light Theme";
    }

    @Override
    public String backgroundColor() {
        return "#D6D6D9";
    }

    @Override
    public String getLogoFill() {
        return "inherit";
    }

    @Override
    public String hoverBackgroundColor() {
        return "#D4E8FF";
    }

    @Override
    public String keyboardSelectionBackgroundColor() {
        return "#C3DEFF";
    }

    @Override
    public String selectionBackground() {
        return "#C3DEFF";
    }

    @Override
    public String inputSelectionBackground() {
        return "#b1d7fe";
    }

    @Override
    public String inactiveSelectionBackground() {
        return "rgba(73,143,225,0.40)";
    }

    @Override
    public String inactiveTabBackground() {
        return "#D6D6D9";
    }

    @Override
    public String inactiveTabBorderColor() {
        return "#D6D6D9";
    }

    @Override
    public String activeTabBackground() {
        return "#ffffff";
    }

    @Override
    public String activeTabBorderColor() {
        return "#8E8E8E";
    }

    @Override
    public String activeTabTextColor() {
        return "#555555";
    }

    @Override
    public String activeTabTextShadow() {
        return "none";
    }

    @Override
    public String activeTabIconColor() {
        return "#1A68AF";
    }

    @Override
    public String tabTextColor() {
        return "#555555";
    }

    @Override
    public String hoveredTabTextColor() {
        return "#333333";
    }

    @Override
    public String tabsPanelBackground() {
        return "#D6D6D9";
    }

    @Override
    public String tabBorderColor() {
        return "#8E8E8E";
    }

    @Override
    public String tabUnderlineColor() {
        return "rgb(70,102,149)";
    }

    @Override
    public String activeEditorTabBackgroundColor() {
        return "rgb(197, 197, 197)";
    }

    @Override
    public String editorReadonlyTabBackgroundColor() {
        return "#d4d4be";
    }

    @Override
    public String activeEditorReadonlyTabBackgroundColor() {
        return "#ffffe4";
    }

    @Override
    public String getEditorTabIconColor() {
        return "#555555";
    }

    @Override
    public String focusedEditorTabBackgroundColor() {
        return "#FFFFFF";
    }

    @Override
    public String focusedEditorTabBorderBottomColor() {
        return "#4EABFF";
    }

    @Override
    public String partBackground() {
        return "#ffffff";
    }

    @Override
    public String partToolbar() {
        return "#D6D6D6";
    }

    @Override
    public String partToolbarActive() {
        return "rgba(195,222,255,1)";
    }

    @Override
    public String partToolbarShadow() {
        return "#bdbdbd";
    }

    @Override
    public String partToolbarSeparatorTopColor() {
        return "#FFFFFF";
    }

    @Override
    public String partToolbarSeparatorBottomColor() {
        return "#AAAAAA";
    }

    @Override
    public String getMainFontColor() {
        return "#222222";
    }

    @Override
    public String getRadioButtonBackgroundColor() {
        return "#BDBDBD";
    }

    @Override
    public String getDisabledMenuColor() {
        return "#AAAAAA";
    }

    @Override
    public String getDialogContentBackground() {
        return "#FFFFFF";
    }

    @Override
    public String getButtonBackground() {
        return "#5A5A5A";
    }

    @Override
    public String getButtonBorderColor() {
        return "#161819";
    }

    @Override
    public String getButtonFontColor() {
        return "#ECECEC";
    }

    @Override
    public String getButtonHoverBackground() {
        return "rgba(0,0,0,0.7)";
    }

    @Override
    public String getButtonHoverBorderColor() {
        return "#E9E9E9";
    }

    @Override
    public String getButtonHoverFontColor() {
        return "#FFFFFF";
    }

    @Override
    public String getButtonClickedBackground() {
        return "rgba(0,0,0,0.8)";
    }

    @Override
    public String getButtonClickedBorderColor() {
        return "#161819";
    }

    @Override
    public String getButtonClickedFontColor() {
        return "#FFFFFF";
    }

    @Override
    public String getButtonDisabledBackground() {
        return "rgba(129, 129, 129, 0.2)";
    }

    @Override
    public String getButtonDisabledBorderColor() {
        return "#CECBCB";
    }

    @Override
    public String getButtonDisabledFontColor() {
        return "#999595";
    }

    @Override
    public String getPrimaryButtonBackground() {
        return "#4A90E2";
    }

    @Override
    public String getPrimaryButtonBorderColor() {
        return "#161819";
    }

    @Override
    public String getPrimaryButtonFontColor() {
        return "#ECECEC";
    }

    @Override
    public String getPrimaryButtonHoverBackground() {
        return "#4484D0";
    }

    @Override
    public String getPrimaryButtonHoverBorderColor() {
        return "#E9E9E9";
    }

    @Override
    public String getPrimaryButtonHoverFontColor() {
        return "#FFFFFF";
    }

    @Override
    public String getPrimaryButtonClickedBackground() {
        return "#3b73b4";
    }

    @Override
    public String getPrimaryButtonClickedBorderColor() {
        return "#14354C";
    }

    @Override
    public String getPrimaryButtonClickedFontColor() {
        return "#FFFFFF";
    }

    @Override
    public String getPrimaryButtonDisabledBackground() {
        return "rgba(128, 177, 234, 0.62)";
    }

    @Override
    public String getPrimaryButtonDisabledBorderColor() {
        return "#A8C6EA";
    }

    @Override
    public String getPrimaryButtonDisabledFontColor() {
        return "rgba(120, 120, 119, 0.6)";
    }

    @Override
    public String getSocialButtonColor() {
        return "#ffffff";
    }

    @Override
    public String editorPanelBackgroundColor() {
        return "#D6D6D9";
    }

    @Override
    public String editorPanelBorderColor() {
        return tabBorderColor();
    }

    @Override
    public String getEditorBackgroundColor() {
        return "white";
    }

    @Override
    public String getEditorCurrentLineColor() {
        return "rgba(215, 215, 215, 0.45)";
    }

    @Override
    public String getEditorDefaultFontColor() {
        return "black";
    }

    @Override
    public String getEditorSelectionColor() {
        return "#d4e2ff";
    }

    @Override
    public String getEditorInactiveSelectionColor() {
        return "#d4d4d4";
    }

    @Override
    public String getEditorCursorColor() {
        return "black";
    }

    @Override
    public String getEditorGutterColor() {
        return "#eee";
    }

    @Override
    public String getEditorKeyWord() {
        return "#708";
    }

    @Override
    public String getEditorAtom() {
        return "#219";
    }

    @Override
    public String getEditorNumber() {
        return "#164";
    }

    @Override
    public String getEditorDef() {
        return "#00f";
    }

    @Override
    public String getEditorVariable() {
        return "black";
    }

    @Override
    public String getEditorVariable2() {
        return "#05a";
    }

    @Override
    public String getEditorProperty() {
        return "black";
    }

    @Override
    public String getEditorOperator() {
        return "black";
    }

    @Override
    public String getEditorComment() {
        return "#a50";
    }

    @Override
    public String getEditorString() {
        return "#a11";
    }

    @Override
    public String getEditorMeta() {
        return "#049";
    }

    @Override
    public String getEditorError() {
        return "#f00";
    }

    @Override
    public String getEditorBuiltin() {
        return "#30a";
    }

    @Override
    public String getEditorTag() {
        return "#170";
    }

    @Override
    public String getEditorAttribute() {
        return "#00c";
    }

    @Override
    public String getEditorString2() {
        return "#f50";
    }

    @Override
    public String completionPopupBackgroundColor() {
        return "#FFFFFF";
    }

    @Override
    public String completionPopupBorderColor() {
        return "#A5A5A5";
    }

    @Override
    public String completionPopupHeaderBackgroundColor() {
        return "#F2F2F2";
    }

    @Override
    public String completionPopupHeaderTextColor() {
        return "#909090";
    }

    @Override
    public String completionPopupSelectedItemBackgroundColor() {
        return "rgba(0, 0, 0, 0.12)";
    }

    @Override
    public String completionPopupItemTextColor() {
        return "#555555";
    }

    @Override
    public String completionPopupItemSubtitleTextColor() {
        return "#909090";
    }

    @Override
    public String completionPopupItemHighlightTextColor() {
        return "#1A68AF";
    }

    @Override
    public String getWindowContentBackground() {
        return "#ECECEC";
    }

    @Override
    public String getWindowContentFontColor() {
        return "#333333";
    }

    @Override
    public String getWindowShadowColor() {
        return "rgba(0, 0, 0, 0.50)";
    }

    @Override
    public String getWindowHeaderBackground() {
        return "#F5F5F5";
    }

    @Override
    public String getWindowHeaderBorderColor() {
        return "#1A68AF";
    }

    @Override
    public String getWindowFooterBackground() {
        return "#ECECEC";
    }

    @Override
    public String getWindowFooterBorderColor() {
        return "#A4A4A4";
    }

    @Override
    public String getWindowSeparatorColor() {
        return "#A4A4A4";
    }

    @Override
    public String getWindowTitleFontColor() {
        return "#555555";
    }

    @Override
    public String getWizardStepsColor() {
        return "#DBDBDB";
    }

    @Override
    public String getWizardStepsBorderColor() {
        return "#BDBDBD";
    }

    @Override
    public String getWelcomeFontColor() {
        return "#5E5E5E";
    }

    @Override
    public String getCaptionFontColor() {
        return "#888888";
    }

    @Override
    public String consolePanelColor() {
        return tabsPanelBackground();
    }

    @Override
    public String getStatusPanelColor() {
        return tabsPanelBackground();
    }

    @Override
    public String getCellOddRowColor() {
        return "#f3f7fb";
    }

    @Override
    public String getCellOddEvenColor() {
        return "#ffffff";
    }

    @Override
    public String getCellKeyboardSelectedRowColor() {
        return "#ffc";
    }

    @Override
    public String getCellHoveredRow() {
        return "#eee";
    }

    @Override
    public String getMainMenuBkgColor() {
        return "#cacacc";
    }

    @Override
    public String getMainMenuSelectedBkgColor() {
        return "#ffffff";
    }

    @Override
    public String getMainMenuSelectedBorderColor() {
        return "#a4a4a4";
    }

    @Override
    public String getMainMenuFontColor() {
        return "#555555";
    }

    @Override
    public String getMainMenuFontHoverColor() {
        return "#333333";
    }

    @Override
    public String getMainMenuFontSelectedColor() {
        return "#1a68af";
    }

    @Override
    public String getNotableButtonTopColor() {
        return "#dbdbdb";
    }

    @Override
    public String getNotableButtonColor() {
        return "#2d6ba3";
    }

    @Override
    public String tabBorderShadow() {
        return "rgba(198, 205, 209, 0.5)";
    }

    @Override
    public String treeTextFileColor() {
        return "#7b7b7b";
    }

    @Override
    public String treeTextFolderColor() {
        return "#606060";
    }

    @Override
    public String treeTextShadow() {
        return "rgba(255, 255, 255, 0.5)";
    }

    @Override
    public String treeIconFileColor() {
        return "#606060";
    }

    @Override
    public String getToolbarActionGroupShadowColor() {
        return "#c5c5c5";
    }

    @Override
    public String getToolbarActionGroupBackgroundColor() {
        return "#ffffff";
    }

    @Override
    public String getToolbarActionGroupBorderColor() {
        return "#afafaf";
    }

    @Override
    public String getToolbarBackgroundImage() {
        return this.getMenuBackgroundImage();
    }

    @Override
    public String getToolbarBackgroundColor() {
        return " #EAEAEA";
    }

    @Override
    public String getToolbarIconColor() {
        return iconColor();
    }

    @Override
    public String getToolbarHoverIconColor() {
        return "#565656";
    }

    @Override
    public String getToolbarSelectedIconFilter() {
        return "brightness(80%)";
    }

    @Override
    public String getTooltipBackgroundColor() {
        return "#FFFFFF";
    }

    @Override
    public String getPerspectiveSwitcherBackgroundColor() {
        return "#1a68af";
    }

    @Override
    public String getSelectCommandActionIconColor() {
        return "#1a68af";
    }

    @Override
    public String getSelectCommandActionIconBackgroundColor() {
        return "#e9e9e9";
    }

    @Override
    public String getSelectCommandActionColor() {
        return "#555555";
    }

    @Override
    public String getSelectCommandActionHoverColor() {
        return "#565656";
    }

    @Override
    public String progressColor() {
        return "#000000";
    }

    @Override
    public String getSuccessEventColor() {
        return "#7dc878";
    }

    @Override
    public String getErrorEventColor() {
        return "#e25252";
    }

    @Override
    public String getDelimeterColor() {
        return "#2f2f2f";
    }

    @Override
    public String getLinkColor() {
        return "#acacac";
    }

    @Override
    public String getFactoryLinkColor() {
        return "#60abe0";
    }

    @Override
    public String processTreeBackgroundColor() {
        return "white";
    }

    @Override
    public String consolesToolbarBackground() {
        return "#D6D6D9";
    }

    @Override
    public String colsolesToolbarBorderColor() {
        return "#8e8e8e";
    }

    @Override
    public String consolesToolbarButtonColor() {
        return "#aaaaaa";
    }

    @Override
    public String consolesToolbarHoveredButtonColor() {
        return "#e0e0e0";
    }

    @Override
    public String consolesToolbarDisabledButtonColor() {
        return "#777777";
    }

    @Override
    public String consolesToolbarToggledButtonColor() {
        return "rgba(0, 0, 0, 0.6)";
    }

    @Override
    public String processTreeDevLabel() {
        return "white";
    }

    @Override
    public String outputBackgroundColor() {
        return "white";
    }

    @Override
    public String getOutputFontColor() {
        return "#5c5c5c";
    }

    @Override
    public String getOutputLinkColor() {
        return "#1266B1";
    }

    @Override
    public String getEditorInfoBackgroundColor() {
        return "#ddd";
    }

    @Override
    public String editorInfoTextColor() {
        return "#5c5c5c";
    }

    @Override
    public String getEditorInfoBorderColor() {
        return "#bdbdbd";
    }

    @Override
    public String getEditorInfoBorderShadowColor() {
        return "#f0f0f0";
    }

    @Override
    public String getEditorLineNumberColor() {
        return "#888888";
    }

    @Override
    public String editorGutterLineNumberBackgroundColor() {
        return "#F6F6F6";
    }

    @Override
    public String getEditorSeparatorColor() {
        return "#888888";
    }

    @Override
    public String getBlueIconColor() {
        return "#1a68af";
    }

    @Override
    public String getRedIconColor() {
        return "#CF405F";
    }

    @Override
    public String getSplitterSmallBorderColor() {
        return "#8E8E8E";
    }

    @Override
    public String getSplitterLargeBorderColor() {
        return "#E1E1E1";
    }

    @Override
    public String getBadgeBackgroundColor() {
        return "rgb(78, 171, 255)";
    }

    @Override
    public String getBadgeFontColor() {
        return "white";
    }

    @Override
    public String processBadgeBorderColor() {
        return "white";
    }

    @Override
    public String getPopupBkgColor() {
        return "#ececec";
    }

    @Override
    public String getPopupBorderColor() {
        return "#a4a4a4";
    }

    @Override
    public String getPopupShadowColor() {
        return "rgba(0, 0, 0, 0.30)";
    }

    @Override
    public String getPopupHoverColor() {
        return "rgba(0, 0, 0, 0.12)";
    }

    @Override
    public String getPopupHotKeyColor() {
        return "#A2A2A2";
    }

    @Override
    public String getTextFieldTitleColor() {
        return "#555555";
    }

    @Override
    public String getTextFieldColor() {
        return "#909090";
    }

    @Override
    public String getTextFieldBackgroundColor() {
        return "#ffffff";
    }

    @Override
    public String getTextFieldFocusedColor() {
        return "#727272";
    }

    @Override
    public String getTextFieldFocusedBackgroundColor() {
        return "#ffffff";
    }

    @Override
    public String getTextFieldDisabledColor() {
        return "#b9b9b9";
    }

    @Override
    public String getTextFieldDisabledBackgroundColor() {
        return "#ffffff";
    }

    @Override
    public String getTextFieldBorderColor() {
        return "#e1e1e1";
    }

    @Override
    public String getMenuBackgroundColor() {
        return "inherit";
    }

    @Override
    public String getMenuBackgroundImage() {
        return "linear-gradient( -180deg, #d3d3d4 0%, #bdbec0 100%)";
    }

    @Override
    public String getPanelBackgroundColor() {
        return "#d6d6d9";
    }

    @Override
    public String getPrimaryHighlightColor() {
        return "#1a68af";
    }

    @Override
    public String iconColor() {
        return "#7c7c7c";
    }

    @Override
    public String activeIconColor() {
        return "#3E3A3A";
    }

    @Override
    public String getSeparatorColor() {
        return "#8e8e8e";
    }

    @Override
    public String getErrorColor() {
        return "#C34d4d";
    }

    @Override
    public String getSuccessColor() {
        return "#43A700";
    }

    @Override
    public String getListBoxHoverBackgroundColor() {
        return this.getPopupHoverColor();
    }

    @Override
    public String getListBoxColor() {
        return this.getTextFieldColor();
    }

    @Override
    public String getListBoxDisabledColor() {
        return this.getTextFieldDisabledColor();
    }

    @Override
    public String getListBoxDisabledBackgroundColor() {
        return this.getTextFieldDisabledBackgroundColor();
    }

    @Override
    public String getListBoxDropdownBackgroundColor() {
        return "#ECECEC";
    }

    @Override
    public String listBoxDropdownShadowColor() {
        return "0 1px 1px 0 rgba(0, 0, 0, 0.1)";
    }

    @Override
    public String categoriesListHeaderTextColor() {
        return this.getTextFieldTitleColor();
    }

    @Override
    public String categoriesListHeaderIconColor() {
        return this.getTextFieldTitleColor();
    }

    @Override
    public String categoriesListHeaderBackgroundColor() {
        return this.getPopupBkgColor();
    }

    @Override
    public String categoriesListItemTextColor() {
        return this.getTextFieldColor();
    }

    @Override
    public String categoriesListItemBackgroundColor() {
        return this.getTextFieldFocusedBackgroundColor();
    }

    @Override
    public String scrollbarBorderColor() {
        return "rgba(59, 59, 59, 0.3)";
    }

    @Override
    public String scrollbarBackgroundColor() {
        return "rgba(27, 27, 27, 0.10)";
    }

    @Override
    public String scrollbarHoverBackgroundColor() {
        return "rgba(27, 27, 27, 0.15)";
    }

    @Override
    public String matchingSearchBlockBackgroundColor() {
        return "rgb(169, 183, 198)";
    }

    @Override
    public String matchingSearchBlockBorderColor() {
        return "#555555";
    }

    @Override
    public String currentSearchBlockBackgroundColor() {
        return "rgb(78, 171, 255)";
    }

    @Override
    public String currentSearchBlockBorderColor() {
        return "#1a68af";
    }

    @Override
    public String openedFilesDropdownButtonBackground() {
        return "#e4e4e4";
    }

    @Override
    public String openedFilesDropdownButtonBorderColor() {
        return "#8e8e8e";
    }

    @Override
    public String openedFilesDropdownButtonShadowColor() {
        return "rgba(0, 0, 0, 0.15)";
    }

    @Override
    public String openedFilesDropdownButtonIconColor() {
        return "#8E8E8E";
    }

    @Override
    public String openedFilesDropdownButtonHoverIconColor() {
        return "#5A5A5A";
    }

    @Override
    public String openedFilesDropdownButtonActiveBackground() {
        return "#FFFFFF";
    }

    @Override
    public String openedFilesDropdownButtonActiveBorderColor() {
        return "#8E8E8E";
    }

    @Override
    public String openedFilesDropdownListBackgroundColor() {
        return "#ECECEC";
    }

    @Override
    public String openedFilesDropdownListBorderColor() {
        return "#A4A4A4";
    }

    @Override
    public String openedFilesDropdownListShadowColor() {
        return "rgba(0, 0, 0, 0.30)";
    }

    @Override
    public String openedFilesDropdownListTextColor() {
        return "#555555";
    }

    @Override
    public String openedFilesDropdownListCloseButtonColor() {
        return "#8E8E8E";
    }

    @Override
    public String openedFilesDropdownListHoverBackgroundColor() {
        return "rgba(0, 0, 0, 0.12)";
    }

    @Override
    public String openedFilesDropdownListHoverTextColor() {
        return "#8E8E8E";
    }

    @Override
    public String radioButtonIconColor() {
        return this.getBlueIconColor();
    }

    @Override
    public String radioButtonBorderColor() {
        return "#E1E1E1";
    }

    @Override
    public String radioButtonBackgroundColor() {
        return this.getTextFieldBackgroundColor();
    }

    @Override
    public String radioButtonFontColor() {
        return this.getTextFieldColor();
    }

    @Override
    public String radioButtonDisabledFontColor() {
        return this.getTextFieldDisabledColor();
    }

    @Override
    public String radioButtonDisabledIconColor() {
        return "#727272";
    }

    @Override
    public String radioButtonDisabledBackgroundColor() {
        return "#F3F3F3";
    }

    @Override
    public String checkBoxIconColor() {
        return this.getBlueIconColor();
    }

    @Override
    public String checkBoxFontColor() {
        return this.getTextFieldColor();
    }

    @Override
    public String checkBoxBorderColor() {
        return this.radioButtonBorderColor();
    }

    @Override
    public String checkBoxBackgroundColor() {
        return this.getTextFieldBackgroundColor();
    }

    @Override
    public String checkBoxDisabledIconColor() {
        return this.radioButtonDisabledIconColor();
    }

    @Override
    public String checkBoxDisabledFontColor() {
        return this.getTextFieldDisabledColor();
    }

    @Override
    public String checkBoxDisabledBackgroundColor() {
        return this.radioButtonDisabledBackgroundColor();
    }

    @Override
    public String treeExpandArrowColor() {
        return "#5D5D5D";
    }

    @Override
    public String treeExpandArrowShadow() {
        return "1px 1px 0 rgba(0, 0, 0, 0.1)";
    }

    @Override
    public String projectExplorerJointContainerFill() {
        return "#5D5D5D";
    }

    @Override
    public String projectExplorerJointContainerShadow() {
        return "drop-shadow(1px 1px 0 rgba(0, 0, 0, 0.1))";
    }

    @Override
    public String projectExplorerPresentableTextShadow() {
        return "1px 1px 1px rgba(0, 0, 0, 0.1)";
    }

    @Override
    public String projectExplorerInfoTextShadow() {
        return "1px 1px 1px rgba(0, 0, 0, 0.1)";
    }

    @Override
    public String projectExplorerSelectedRowBackground() {
        return "rgba(0, 0, 0, 0.2)";
    }

    @Override
    public String projectExplorerHoverRowBackground() {
        return "rgba(0, 0, 0, 0.12)";
    }

    @Override
    public String projectExplorerSelectedRowBorder() {
        return "#555";
    }

    @Override
    public String projectExplorerHoverRowBorder() {
        return "#7b7b7b";
    }

    @Override
    public String loaderExpanderColor() {
        return "#555555";
    }

    @Override
    public String loaderIconBackgroundColor() {
        return "#e9e9e9";
    }

    @Override
    public String loaderProgressStatusColor() {
        return "#1a68af";
    }

    @Override
    public String  placeholderColor() {
        return "#727272";
    }

    @Override
    public String categoryHeaderButtonHoverColor() {
        return this.getToolbarHoverIconColor();
    }

    @Override
    public String categoryHeaderButtonColor() {
        return this.getToolbarIconColor();
    }

    @Override
    public String categoryElementButtonHoverColor() {
        return this.getBlueIconColor();
    }

    @Override
    public String categoryElementButtonColor() {
        return this.getPrimaryButtonBackground();
    }

    @Override
    public String categorySelectElementBackgroundColor() {
        return "#E7F2FF";
    }

    @Override
    public String categorySelectElementColor() {
        return this.getPrimaryButtonBackground();
    }

    @Override
    public String notificationPopupSuccessBackground() {
        return "#31b993";
    }

    @Override
    public String notificationPopupFailBackground() {
        return "#c34d4d";
    }

    @Override
    public String notificationPopupProgressBackground() {
        return "#9b9b9b";
    }

    @Override
    public String notificationPopupWarningBackground() {
    	return "#F0AD4E";
    }
    
    @Override
    public String notificationPopupPanelShadow() {
        return "0 0 7px rgba(0,0,0,0.2)";
    }

    @Override
    public String notificationPopupIconSuccessFill() {
        return "#31b993";
    }

    @Override
    public String notificationPopupIconFailFill() {
        return "#c34d4d";
    }

    @Override
    public String notificationPopupIconProgressFill() {
        return "#9b9b9b";
    }
    
    @Override
    public String notificationPopupIconWarningFill() {
    	return "#F0AD4E";
    }
    
    @Override
    public String notificationPopupIconSvgFill() {
        return "#FFFFFF";
    }

    @Override
    public String notificationPopupTextColor() {
        return "#FFFFFF";
    }

    @Override
    public String closeNotificationButtonColor() {
        return "#7b7b7b";
    }

    @Override
    public String closeNotificationHoveredButtonColor() {
        return "#333333";
    }

    @Override
    public String projectExplorerReadonlyItemBackground() {
        return "#ffffe4";
    }

    @Override
    public String projectExplorerTestItemBackground() {
        return "#effae7";
    }

    @Override
    public String editorTabPinBackgroundColor() {
        return "#6AAF32";
    }

    @Override
    public String editorTabPinDropShadow() {
        return "drop-shadow(1px 1px 1px rgba(0, 0, 0, 0.4))";
    }

    @Override
    public String loaderBackgroundColor() {
        return "#fff";
    }

    @Override
    public String loaderBorderColor() {
        return "#a5a5a5";
    }

    @Override
    public String loaderBoxShadow() {
        return "0 2px 7px rgba(0,0,0,0.4)";
    }

    @Override
    public String loaderSVGFill() {
        return "#4990e2";
    }

    @Override
    public String loaderLabelColor() {
        return "#6c5455";
    }

    @Override
    public String outputBoxShadow() {
        return "inset 0px 37px 8px -35px rgba(0,0,0,0.25)";
    }

    @Override
    public String toolButtonColor() {
        return "#7b7b7b";
    }

    @Override
    public String toolButtonHoverColor() {
        return "#333333";
    }

    @Override
    public String toolButtonBorder() {
        return "1px solid transparent";
    }

    @Override
    public String toolButtonActiveBorder() {
        return "1px solid #8E8E8E";
    }

    @Override
    public String toolButtonHoverBackgroundColor() {
        return "#BDBEC0";
    }

    @Override
    public String toolButtonActiveBackgroundColor() {
        return "#AFAFB0";
    }

    @Override
    public String toolButtonHoverBoxShadow() {
        return "none";
    }

    @Override
    public String toolButtonActiveBoxShadow() {
        return "inset 1px 1px 0 0 #6F6E6E";
    }

    @Override
    public String vcsConsoleStagedFilesColor() {
        return "green";
    }

    @Override
    public String vcsConsoleUnstagedFilesColor() {
        return "red";
    }

    @Override
    public String vcsConsoleErrorColor() {
        return "red";
    }

    @Override
    public String vcsConsoleModifiedFilesColor() {
        return "#FF7F50";
    }

    @Override
    public String vcsConsoleChangesLineNumbersColor() {
        return "#66CCFF";
    }

    @Override
    public String editorPreferenceCategoryBackgroundColor() {
        return "rgba(27, 27, 27, 0.10)";
    }

    /********************************************************************************************
     *
     * Resource monitors
     *
     ********************************************************************************************/

    @Override
    public String resourceMonitorBarBackground() {
        return "#e0e0e0";
    }

    /********************************************************************************************
     *
     * Popup Loader
     *
     ********************************************************************************************/

    @Override
    public String popupLoaderBackgroundColor() {
        return "white";
    }

    @Override
    public String popupLoaderBorderColor() {
        return "#466695";
    }

    @Override
    public String popupLoaderShadow() {
        return "0 0 7px rgba(0,0,0,0.2)";
    }

    @Override
    public String popupLoaderTitleColor() {
        return "#353535";
    }

    @Override
    public String popupLoaderTextColor() {
        return "#999999";
    }
}
