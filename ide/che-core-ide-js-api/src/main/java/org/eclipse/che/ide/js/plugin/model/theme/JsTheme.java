/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.js.plugin.model.theme;

import elemental.json.JsonObject;
import org.eclipse.che.ide.api.theme.Theme;

/** @author Yevhen Vydolob */
public class JsTheme implements Theme {

  private final JsonObject theme;

  public JsTheme(JsonObject theme) {
    this.theme = theme;
  }

  @Override
  public String backgroundColor() {
    return theme.getString("backgroundColor");
  }

  @Override
  public String getLogoFill() {
    return theme.getString("logoFill");
  }

  @Override
  public String hoverBackgroundColor() {
    return theme.getString("hoverBackgroundColor");
  }

  @Override
  public String keyboardSelectionBackgroundColor() {
    return theme.getString("keyboardSelectionBackgroundColor");
  }

  @Override
  public String selectionBackground() {
    return theme.getString("selectionBackground");
  }

  @Override
  public String inputSelectionBackground() {
    return theme.getString("inputSelectionBackground");
  }

  @Override
  public String inactiveSelectionBackground() {
    return theme.getString("inactiveSelectionBackground");
  }

  @Override
  public String inactiveTabBackground() {
    return theme.getString("inactiveTabBackground");
  }

  @Override
  public String inactiveTabBorderColor() {
    return theme.getString("inactiveTabBorderColor");
  }

  @Override
  public String activeTabBackground() {
    return theme.getString("activeTabBackground");
  }

  @Override
  public String activeTabBorderColor() {
    return theme.getString("activeTabBorderColor");
  }

  @Override
  public String activeTabTextColor() {
    return theme.getString("activeTabTextColor");
  }

  @Override
  public String activeTabTextShadow() {
    return theme.getString("activeTabTextShadow");
  }

  @Override
  public String activeTabIconColor() {
    return theme.getString("activeTabIconColor");
  }

  @Override
  public String tabTextColor() {
    return theme.getString("tabTextColor");
  }

  @Override
  public String hoveredTabTextColor() {
    return theme.getString("hoveredTabTextColor");
  }

  @Override
  public String getEditorTabIconColor() {
    return theme.getString("editorTabIconColor");
  }

  @Override
  public String activeEditorTabBackgroundColor() {
    return theme.getString("activeEditorTabBackgroundColor");
  }

  @Override
  public String editorReadonlyTabBackgroundColor() {
    return theme.getString("editorReadonlyTabBackgroundColor");
  }

  @Override
  public String activeEditorReadonlyTabBackgroundColor() {
    return theme.getString("activeEditorReadonlyTabBackgroundColor");
  }

  @Override
  public String focusedEditorTabBackgroundColor() {
    return theme.getString("focusedEditorTabBackgroundColor");
  }

  @Override
  public String focusedEditorTabBorderBottomColor() {
    return theme.getString("focusedEditorTabBorderBottomColor");
  }

  @Override
  public String tabsPanelBackground() {
    return theme.getString("tabsPanelBackground");
  }

  @Override
  public String tabBorderColor() {
    return theme.getString("tabBorderColor");
  }

  @Override
  public String tabUnderlineColor() {
    return theme.getString("tabUnderlineColor");
  }

  @Override
  public String partBackground() {
    return theme.getString("partBackground");
  }

  @Override
  public String partToolbar() {
    return theme.getString("partToolbar");
  }

  @Override
  public String partToolbarActive() {
    return theme.getString("partToolbarActive");
  }

  @Override
  public String partToolbarShadow() {
    return theme.getString("partToolbarShadow");
  }

  @Override
  public String partToolbarSeparatorTopColor() {
    return theme.getString("partToolbarSeparatorTopColor");
  }

  @Override
  public String partToolbarSeparatorBottomColor() {
    return theme.getString("partToolbarSeparatorBottomColor");
  }

  @Override
  public String getMainFontColor() {
    return theme.getString("mainFontColor");
  }

  @Override
  public String getRadioButtonBackgroundColor() {
    return theme.getString("radioButtonBackgroundColor");
  }

  @Override
  public String getDisabledMenuColor() {
    return theme.getString("disabledMenuColor");
  }

  @Override
  public String getCommandsToolbarBackgroundColor() {
    return theme.getString("commandsToolbarBackgroundColor");
  }

  @Override
  public String getCommandsToolbarProcessesLabelBackground() {
    return theme.getString("commandsToolbarProcessesLabelBackground");
  }

  @Override
  public String getCommandsToolbarProcessesLabelBorder() {
    return theme.getString("commandsToolbarProcessesLabelBorder");
  }

  @Override
  public String getCommandsToolbarMachineNameColor() {
    return theme.getString("commandsToolbarMachineNameColor");
  }

  @Override
  public String getCommandsToolbarCommandNameColor() {
    return theme.getString("commandsToolbarCommandNameColor");
  }

  @Override
  public String getCommandEditorProjectsTableHeaderColor() {
    return theme.getString("commandEditorProjectsTableHeaderColor");
  }

  @Override
  public String getCommandEditorProjectsTableRowColor() {
    return theme.getString("commandEditorProjectsTableRowColor");
  }

  @Override
  public String getCommandEditorProjectSwitcherBorder() {
    return theme.getString("commandEditorProjectSwitcherBorder");
  }

  @Override
  public String getDialogContentBackground() {
    return theme.getString("dialogContentBackground");
  }

  @Override
  public String getDropdownListBackground() {
    return theme.getString("dropdownListBackground");
  }

  @Override
  public String getHoveredDropdownListBackground() {
    return theme.getString("hoveredDropdownListBackground");
  }

  @Override
  public String getActiveDropdownListBackground() {
    return theme.getString("activeDropdownListBackground");
  }

  @Override
  public String getDropdownListBorder() {
    return theme.getString("dropdownListBorder");
  }

  @Override
  public String getDropdownListButtonColor() {
    return theme.getString("dropdownListButtonColor");
  }

  @Override
  public String getMenuButtonBackground() {
    return theme.getString("menuButtonBackground");
  }

  @Override
  public String getHoveredMenuButtonBackground() {
    return theme.getString("hoveredMenuButtonBackground");
  }

  @Override
  public String getActiveMenuButtonBackground() {
    return theme.getString("activeMenuButtonBackground");
  }

  @Override
  public String getMenuButtonBorder() {
    return theme.getString("menuButtonBorder");
  }

  @Override
  public String toolbarControllerBackground() {
    return theme.getString("toolbarControllerBackground");
  }

  @Override
  public String toolbarControllerLeftColor() {
    return theme.getString("toolbarControllerLeftColor");
  }

  @Override
  public String toolbarControllerTopColor() {
    return theme.getString("toolbarControllerTopColor");
  }

  @Override
  public String getButtonBackground() {
    return theme.getString("buttonBackground");
  }

  @Override
  public String getButtonBorderColor() {
    return theme.getString("buttonBorderColor");
  }

  @Override
  public String getButtonFontColor() {
    return theme.getString("buttonFontColor");
  }

  @Override
  public String getButtonHoverBackground() {
    return theme.getString("buttonHoverBackground");
  }

  @Override
  public String getButtonHoverBorderColor() {
    return theme.getString("buttonHoverBorderColor");
  }

  @Override
  public String getButtonHoverFontColor() {
    return theme.getString("buttonHoverFontColor");
  }

  @Override
  public String getButtonClickedBackground() {
    return theme.getString("buttonClickedBackground");
  }

  @Override
  public String getButtonClickedBorderColor() {
    return theme.getString("buttonClickedBorderColor");
  }

  @Override
  public String getButtonClickedFontColor() {
    return theme.getString("buttonClickedFontColor");
  }

  @Override
  public String getButtonDisabledBackground() {
    return theme.getString("buttonDisabledBackground");
  }

  @Override
  public String getButtonDisabledBorderColor() {
    return theme.getString("buttonDisabledBorderColor");
  }

  @Override
  public String getButtonDisabledFontColor() {
    return theme.getString("buttonDisabledFontColor");
  }

  @Override
  public String getPrimaryButtonBackground() {
    return theme.getString("primaryButtonBackground");
  }

  @Override
  public String getPrimaryButtonBorderColor() {
    return theme.getString("primaryButtonBorderColor");
  }

  @Override
  public String getPrimaryButtonFontColor() {
    return theme.getString("primaryButtonFontColor");
  }

  @Override
  public String getPrimaryButtonHoverBackground() {
    return theme.getString("primaryButtonHoverBackground");
  }

  @Override
  public String getPrimaryButtonHoverBorderColor() {
    return theme.getString("primaryButtonHoverBorderColor");
  }

  @Override
  public String getPrimaryButtonHoverFontColor() {
    return theme.getString("primaryButtonHoverFontColor");
  }

  @Override
  public String getPrimaryButtonClickedBackground() {
    return theme.getString("primaryButtonClickedBackground");
  }

  @Override
  public String getPrimaryButtonClickedBorderColor() {
    return theme.getString("primaryButtonClickedBorderColor");
  }

  @Override
  public String getPrimaryButtonClickedFontColor() {
    return theme.getString("primaryButtonClickedFontColor");
  }

  @Override
  public String getPrimaryButtonDisabledBackground() {
    return theme.getString("primaryButtonDisabledBackground");
  }

  @Override
  public String getPrimaryButtonDisabledBorderColor() {
    return theme.getString("primaryButtonDisabledBorderColor");
  }

  @Override
  public String getPrimaryButtonDisabledFontColor() {
    return theme.getString("primaryButtonDisabledFontColor");
  }

  @Override
  public String getSocialButtonColor() {
    return theme.getString("socialButtonColor");
  }

  @Override
  public String editorPanelBackgroundColor() {
    return theme.getString("editorPanelBackgroundColor");
  }

  @Override
  public String editorTabsBackgroundColor() {
    return theme.getString("editorTabsBackgroundColor");
  }

  @Override
  public String editorPanelBorderColor() {
    return theme.getString("editorPanelBorderColor");
  }

  @Override
  public String getEditorBackgroundColor() {
    return theme.getString("editorBackgroundColor");
  }

  @Override
  public String getEditorCurrentLineColor() {
    return theme.getString("editorCurrentLineColor");
  }

  @Override
  public String getEditorDefaultFontColor() {
    return theme.getString("editorDefaultFontColor");
  }

  @Override
  public String getEditorSelectionColor() {
    return theme.getString("editorSelectionColor");
  }

  @Override
  public String getEditorLinkedGroupColor() {
    return theme.getString("editorLinkedGroupColor");
  }

  @Override
  public String getEditorLinkedGroupBackground() {
    return theme.getString("editorLinkedGroupBackground");
  }

  @Override
  public String getEditorInactiveSelectionColor() {
    return theme.getString("editorInactiveSelectionColor");
  }

  @Override
  public String getEditorCursorColor() {
    return theme.getString("editorCursorColor");
  }

  @Override
  public String getEditorGutterColor() {
    return theme.getString("editorGutterColor");
  }

  @Override
  public String getEditorKeyWord() {
    return theme.getString("editorKeyWord");
  }

  @Override
  public String getEditorAtom() {
    return theme.getString("editorAtom");
  }

  @Override
  public String getEditorNumber() {
    return theme.getString("editorNumber");
  }

  @Override
  public String getEditorDef() {
    return theme.getString("editorDef");
  }

  @Override
  public String getEditorVariable() {
    return theme.getString("editorVariable");
  }

  @Override
  public String getEditorVariable2() {
    return theme.getString("editorVariable2");
  }

  @Override
  public String getEditorProperty() {
    return theme.getString("editorProperty");
  }

  @Override
  public String getEditorOperator() {
    return theme.getString("editorOperator");
  }

  @Override
  public String getEditorComment() {
    return theme.getString("editorComment");
  }

  @Override
  public String getEditorString() {
    return theme.getString("editorString");
  }

  @Override
  public String getEditorMeta() {
    return theme.getString("editorMeta");
  }

  @Override
  public String getEditorError() {
    return theme.getString("editorError");
  }

  @Override
  public String getEditorBuiltin() {
    return theme.getString("editorBuiltin");
  }

  @Override
  public String getEditorTag() {
    return theme.getString("editorTag");
  }

  @Override
  public String getEditorAttribute() {
    return theme.getString("editorAttribute");
  }

  @Override
  public String getEditorString2() {
    return theme.getString("editorString2");
  }

  @Override
  public String completionPopupBackgroundColor() {
    return theme.getString("completionPopupBackgroundColor");
  }

  @Override
  public String completionPopupBorderColor() {
    return theme.getString("completionPopupBorderColor");
  }

  @Override
  public String completionPopupHeaderBackgroundColor() {
    return theme.getString("completionPopupHeaderBackgroundColor");
  }

  @Override
  public String completionPopupHeaderTextColor() {
    return theme.getString("completionPopupHeaderTextColor");
  }

  @Override
  public String completionPopupSelectedItemBackgroundColor() {
    return theme.getString("completionPopupSelectedItemBackgroundColor");
  }

  @Override
  public String completionPopupItemTextColor() {
    return theme.getString("completionPopupItemTextColor");
  }

  @Override
  public String completionPopupItemSubtitleTextColor() {
    return theme.getString("completionPopupItemSubtitleTextColor");
  }

  @Override
  public String completionPopupItemHighlightTextColor() {
    return theme.getString("completionPopupItemHighlightTextColor");
  }

  @Override
  public String getWindowContentBackground() {
    return theme.getString("windowContentBackground");
  }

  @Override
  public String getWindowContentFontColor() {
    return theme.getString("windowContentFontColor");
  }

  @Override
  public String getWindowShadowColor() {
    return theme.getString("windowShadowColor");
  }

  @Override
  public String getWindowHeaderBackground() {
    return theme.getString("windowHeaderBackground");
  }

  @Override
  public String getWindowHeaderBorderColor() {
    return theme.getString("windowHeaderBorderColor");
  }

  @Override
  public String getWindowFooterBackground() {
    return theme.getString("windowFooterBackground");
  }

  @Override
  public String getWindowFooterBorderColor() {
    return theme.getString("windowFooterBorderColor");
  }

  @Override
  public String getWindowSeparatorColor() {
    return theme.getString("windowSeparatorColor");
  }

  @Override
  public String getWindowTitleFontColor() {
    return theme.getString("windowTitleFontColor");
  }

  @Override
  public String getWizardStepsColor() {
    return theme.getString("wizardStepsColor");
  }

  @Override
  public String getWizardStepsBorderColor() {
    return theme.getString("wizardStepsBorderColor");
  }

  @Override
  public String getFactoryLinkColor() {
    return theme.getString("factoryLinkColor");
  }

  @Override
  public String getWelcomeFontColor() {
    return theme.getString("welcomeFontColor");
  }

  @Override
  public String getCaptionFontColor() {
    return theme.getString("captionFontColor");
  }

  @Override
  public String consolePanelColor() {
    return theme.getString("consolePanelColor");
  }

  @Override
  public String getStatusPanelColor() {
    return theme.getString("statusPanelColor");
  }

  @Override
  public String getCellOddRowColor() {
    return theme.getString("cellOddRowColor");
  }

  @Override
  public String getCellOddEvenColor() {
    return theme.getString("cellOddEvenColor");
  }

  @Override
  public String getCellKeyboardSelectedRowColor() {
    return theme.getString("cellKeyboardSelectedRowColor");
  }

  @Override
  public String getCellHoveredRow() {
    return theme.getString("cellHoveredRow");
  }

  @Override
  public String getMainMenuBkgColor() {
    return theme.getString("mainMenuBkgColor");
  }

  @Override
  public String mainMenuDelimiterBackground() {
    return theme.getString("mainMenuDelimiterBackground");
  }

  @Override
  public String getMainMenuSelectedBkgColor() {
    return theme.getString("mainMenuSelectedBkgColor");
  }

  @Override
  public String getMainMenuSelectedBorderColor() {
    return theme.getString("mainMenuSelectedBorderColor");
  }

  @Override
  public String getMainMenuFontColor() {
    return theme.getString("mainMenuFontColor");
  }

  @Override
  public String getMainMenuFontHoverColor() {
    return theme.getString("mainMenuFontHoverColor");
  }

  @Override
  public String getMainMenuFontSelectedColor() {
    return theme.getString("mainMenuFontSelectedColor");
  }

  @Override
  public String getNotableButtonTopColor() {
    return theme.getString("notableButtonTopColor");
  }

  @Override
  public String getNotableButtonColor() {
    return theme.getString("notableButtonColor");
  }

  @Override
  public String tabBorderShadow() {
    return theme.getString("tabBorderShadow");
  }

  @Override
  public String treeTextFileColor() {
    return theme.getString("treeTextFileColor");
  }

  @Override
  public String treeTextFolderColor() {
    return theme.getString("treeTextFolderColor");
  }

  @Override
  public String treeTextShadow() {
    return theme.getString("treeTextShadow");
  }

  @Override
  public String treeIconFileColor() {
    return theme.getString("treeIconFileColor");
  }

  @Override
  public String getToolbarActionGroupShadowColor() {
    return theme.getString("toolbarActionGroupShadowColor");
  }

  @Override
  public String getToolbarActionGroupBackgroundColor() {
    return theme.getString("toolbarActionGroupBackgroundColor");
  }

  @Override
  public String getToolbarActionGroupBorderColor() {
    return theme.getString("toolbarActionGroupBorderColor");
  }

  @Override
  public String getToolbarBackgroundImage() {
    return theme.getString("toolbarBackgroundImage");
  }

  @Override
  public String getToolbarBackgroundColor() {
    return theme.getString("toolbarBackgroundColor");
  }

  @Override
  public String getToolbarIconColor() {
    return theme.getString("toolbarIconColor");
  }

  @Override
  public String getToolbarHoverIconColor() {
    return theme.getString("toolbarHoverIconColor");
  }

  @Override
  public String getToolbarSelectedIconFilter() {
    return theme.getString("toolbarSelectedIconFilter");
  }

  @Override
  public String getTooltipBackgroundColor() {
    return theme.getString("tooltipBackgroundColor");
  }

  @Override
  public String getPerspectiveSwitcherBackgroundColor() {
    return theme.getString("perspectiveSwitcherBackgroundColor");
  }

  @Override
  public String getSelectCommandActionIconColor() {
    return theme.getString("selectCommandActionIconColor");
  }

  @Override
  public String getSelectCommandActionIconBackgroundColor() {
    return theme.getString("selectCommandActionIconBackgroundColor");
  }

  @Override
  public String getSelectCommandActionColor() {
    return theme.getString("selectCommandActionColor");
  }

  @Override
  public String getSelectCommandActionHoverColor() {
    return theme.getString("selectCommandActionHoverColor");
  }

  @Override
  public String progressColor() {
    return theme.getString("progressColor");
  }

  @Override
  public String getSuccessEventColor() {
    return theme.getString("successEventColor");
  }

  @Override
  public String getErrorEventColor() {
    return theme.getString("errorEventColor");
  }

  @Override
  public String getLinkColor() {
    return theme.getString("linkColor");
  }

  @Override
  public String getDelimeterColor() {
    return theme.getString("delimeterColor");
  }

  @Override
  public String processTreeBackgroundColor() {
    return theme.getString("processTreeBackgroundColor");
  }

  @Override
  public String consolesToolbarBackground() {
    return theme.getString("consolesToolbarBackground");
  }

  @Override
  public String colsolesToolbarBorderColor() {
    return theme.getString("colsolesToolbarBorderColor");
  }

  @Override
  public String consolesToolbarButtonColor() {
    return theme.getString("consolesToolbarButtonColor");
  }

  @Override
  public String consolesToolbarHoveredButtonColor() {
    return theme.getString("consolesToolbarHoveredButtonColor");
  }

  @Override
  public String consolesToolbarDisabledButtonColor() {
    return theme.getString("consolesToolbarDisabledButtonColor");
  }

  @Override
  public String consolesToolbarToggledButtonColor() {
    return theme.getString("consolesToolbarToggledButtonColor");
  }

  @Override
  public String processTreeDevLabel() {
    return theme.getString("processTreeDevLabel");
  }

  @Override
  public String outputBackgroundColor() {
    return theme.getString("outputBackgroundColor");
  }

  @Override
  public String getOutputFontColor() {
    return theme.getString("outputFontColor");
  }

  @Override
  public String getOutputLinkColor() {
    return theme.getString("outputLinkColor");
  }

  @Override
  public String getEditorInfoBackgroundColor() {
    return theme.getString("editorInfoBackgroundColor");
  }

  @Override
  public String editorInfoTextColor() {
    return theme.getString("editorInfoTextColor");
  }

  @Override
  public String getEditorInfoBorderColor() {
    return theme.getString("editorInfoBorderColor");
  }

  @Override
  public String getEditorInfoBorderShadowColor() {
    return theme.getString("editorInfoBorderShadowColor");
  }

  @Override
  public String getEditorLineNumberColor() {
    return theme.getString("editorLineNumberColor");
  }

  @Override
  public String editorGutterLineNumberBackgroundColor() {
    return theme.getString("editorGutterLineNumberBackgroundColor");
  }

  @Override
  public String getEditorSeparatorColor() {
    return theme.getString("editorSeparatorColor");
  }

  @Override
  public String getSplitterSmallBorderColor() {
    return theme.getString("splitterSmallBorderColor");
  }

  @Override
  public String getSplitterLargeBorderColor() {
    return theme.getString("splitterLargeBorderColor");
  }

  @Override
  public String getBadgeBackgroundColor() {
    return theme.getString("badgeBackgroundColor");
  }

  @Override
  public String getBadgeFontColor() {
    return theme.getString("badgeFontColor");
  }

  @Override
  public String processBadgeBorderColor() {
    return theme.getString("processBadgeBorderColor");
  }

  @Override
  public String getBlueIconColor() {
    return theme.getString("blueIconColor");
  }

  @Override
  public String getRedIconColor() {
    return theme.getString("redIconColor");
  }

  @Override
  public String getPopupBkgColor() {
    return theme.getString("popupBkgColor");
  }

  @Override
  public String getPopupBorderColor() {
    return theme.getString("popupBorderColor");
  }

  @Override
  public String getPopupShadowColor() {
    return theme.getString("popupShadowColor");
  }

  @Override
  public String getPopupHoverColor() {
    return theme.getString("popupHoverColor");
  }

  @Override
  public String getPopupHotKeyColor() {
    return theme.getString("popupHotKeyColor");
  }

  @Override
  public String getTextFieldTitleColor() {
    return theme.getString("textFieldTitleColor");
  }

  @Override
  public String getTextFieldColor() {
    return theme.getString("textFieldColor");
  }

  @Override
  public String getTextFieldBackgroundColor() {
    return theme.getString("textFieldBackgroundColor");
  }

  @Override
  public String getTextFieldFocusedColor() {
    return theme.getString("textFieldFocusedColor");
  }

  @Override
  public String getTextFieldFocusedBackgroundColor() {
    return theme.getString("textFieldFocusedBackgroundColor");
  }

  @Override
  public String getTextFieldDisabledColor() {
    return theme.getString("textFieldDisabledColor");
  }

  @Override
  public String getTextFieldDisabledBackgroundColor() {
    return theme.getString("textFieldDisabledBackgroundColor");
  }

  @Override
  public String getTextFieldBorderColor() {
    return theme.getString("textFieldBorderColor");
  }

  @Override
  public String getMenuBackgroundColor() {
    return theme.getString("menuBackgroundColor");
  }

  @Override
  public String getMenuBackgroundImage() {
    return theme.getString("menuBackgroundImage");
  }

  @Override
  public String getPanelBackgroundColor() {
    return theme.getString("panelBackgroundColor");
  }

  @Override
  public String getPrimaryHighlightColor() {
    return theme.getString("primaryHighlightColor");
  }

  @Override
  public String iconColor() {
    return theme.getString("iconColor");
  }

  @Override
  public String activeIconColor() {
    return theme.getString("activeIconColor");
  }

  @Override
  public String getSeparatorColor() {
    return theme.getString("separatorColor");
  }

  @Override
  public String getErrorColor() {
    return theme.getString("errorColor");
  }

  @Override
  public String getSuccessColor() {
    return theme.getString("successColor");
  }

  @Override
  public String getListBoxHoverBackgroundColor() {
    return theme.getString("listBoxHoverBackgroundColor");
  }

  @Override
  public String getListBoxColor() {
    return theme.getString("listBoxColor");
  }

  @Override
  public String getListBoxDisabledColor() {
    return theme.getString("listBoxDisabledColor");
  }

  @Override
  public String getListBoxDisabledBackgroundColor() {
    return theme.getString("listBoxDisabledBackgroundColor");
  }

  @Override
  public String getListBoxDropdownBackgroundColor() {
    return theme.getString("listBoxDropdownBackgroundColor");
  }

  @Override
  public String listBoxDropdownShadowColor() {
    return theme.getString("listBoxDropdownShadowColor");
  }

  @Override
  public String categoriesListHeaderTextColor() {
    return theme.getString("categoriesListHeaderTextColor");
  }

  @Override
  public String categoriesListHeaderIconColor() {
    return theme.getString("categoriesListHeaderIconColor");
  }

  @Override
  public String categoriesListHeaderBackgroundColor() {
    return theme.getString("categoriesListHeaderBackgroundColor");
  }

  @Override
  public String categoriesListItemTextColor() {
    return theme.getString("categoriesListItemTextColor");
  }

  @Override
  public String categoriesListItemBackgroundColor() {
    return theme.getString("categoriesListItemBackgroundColor");
  }

  @Override
  public String scrollbarBorderColor() {
    return theme.getString("scrollbarBorderColor");
  }

  @Override
  public String scrollbarBackgroundColor() {
    return theme.getString("scrollbarBackgroundColor");
  }

  @Override
  public String scrollbarHoverBackgroundColor() {
    return theme.getString("scrollbarHoverBackgroundColor");
  }

  @Override
  public String matchingSearchBlockBackgroundColor() {
    return theme.getString("matchingSearchBlockBackgroundColor");
  }

  @Override
  public String matchingSearchBlockBorderColor() {
    return theme.getString("matchingSearchBlockBorderColor");
  }

  @Override
  public String currentSearchBlockBackgroundColor() {
    return theme.getString("currentSearchBlockBackgroundColor");
  }

  @Override
  public String currentSearchBlockBorderColor() {
    return theme.getString("currentSearchBlockBorderColor");
  }

  @Override
  public String openedFilesDropdownButtonBackground() {
    return theme.getString("openedFilesDropdownButtonBackground");
  }

  @Override
  public String openedFilesDropdownButtonBorderColor() {
    return theme.getString("openedFilesDropdownButtonBorderColor");
  }

  @Override
  public String openedFilesDropdownButtonShadowColor() {
    return theme.getString("openedFilesDropdownButtonShadowColor");
  }

  @Override
  public String openedFilesDropdownButtonIconColor() {
    return theme.getString("openedFilesDropdownButtonIconColor");
  }

  @Override
  public String openedFilesDropdownButtonHoverIconColor() {
    return theme.getString("openedFilesDropdownButtonHoverIconColor");
  }

  @Override
  public String openedFilesDropdownButtonActiveBackground() {
    return theme.getString("openedFilesDropdownButtonActiveBackground");
  }

  @Override
  public String openedFilesDropdownButtonActiveBorderColor() {
    return theme.getString("openedFilesDropdownButtonActiveBorderColor");
  }

  @Override
  public String openedFilesDropdownListBackgroundColor() {
    return theme.getString("openedFilesDropdownListBackgroundColor");
  }

  @Override
  public String openedFilesDropdownListBorderColor() {
    return theme.getString("openedFilesDropdownListBorderColor");
  }

  @Override
  public String openedFilesDropdownListShadowColor() {
    return theme.getString("openedFilesDropdownListShadowColor");
  }

  @Override
  public String openedFilesDropdownListTextColor() {
    return theme.getString("openedFilesDropdownListTextColor");
  }

  @Override
  public String openedFilesDropdownListCloseButtonColor() {
    return theme.getString("openedFilesDropdownListCloseButtonColor");
  }

  @Override
  public String openedFilesDropdownListHoverBackgroundColor() {
    return theme.getString("openedFilesDropdownListHoverBackgroundColor");
  }

  @Override
  public String openedFilesDropdownListHoverTextColor() {
    return theme.getString("openedFilesDropdownListHoverTextColor");
  }

  @Override
  public String radioButtonIconColor() {
    return theme.getString("radioButtonIconColor");
  }

  @Override
  public String radioButtonBorderColor() {
    return theme.getString("radioButtonBorderColor");
  }

  @Override
  public String radioButtonBackgroundColor() {
    return theme.getString("radioButtonBackgroundColor");
  }

  @Override
  public String radioButtonFontColor() {
    return theme.getString("radioButtonFontColor");
  }

  @Override
  public String radioButtonDisabledFontColor() {
    return theme.getString("radioButtonDisabledFontColor");
  }

  @Override
  public String radioButtonDisabledIconColor() {
    return theme.getString("radioButtonDisabledIconColor");
  }

  @Override
  public String radioButtonDisabledBackgroundColor() {
    return theme.getString("radioButtonDisabledBackgroundColor");
  }

  @Override
  public String checkBoxIconColor() {
    return theme.getString("checkBoxIconColor");
  }

  @Override
  public String checkBoxIndeterminateIconColor() {
    return theme.getString("checkBoxIndeterminateIconColor");
  }

  @Override
  public String checkBoxFontColor() {
    return theme.getString("checkBoxFontColor");
  }

  @Override
  public String checkBoxBorderColor() {
    return theme.getString("checkBoxBorderColor");
  }

  @Override
  public String checkBoxBackgroundColor() {
    return theme.getString("checkBoxBackgroundColor");
  }

  @Override
  public String checkBoxDisabledIconColor() {
    return theme.getString("checkBoxDisabledIconColor");
  }

  @Override
  public String checkBoxDisabledFontColor() {
    return theme.getString("checkBoxDisabledFontColor");
  }

  @Override
  public String checkBoxDisabledBackgroundColor() {
    return theme.getString("checkBoxDisabledBackgroundColor");
  }

  @Override
  public String treeExpandArrowColor() {
    return theme.getString("treeExpandArrowColor");
  }

  @Override
  public String treeExpandArrowShadow() {
    return theme.getString("treeExpandArrowShadow");
  }

  @Override
  public String projectExplorerJointContainerFill() {
    return theme.getString("projectExplorerJointContainerFill");
  }

  @Override
  public String projectExplorerJointContainerShadow() {
    return theme.getString("projectExplorerJointContainerShadow");
  }

  @Override
  public String projectExplorerPresentableTextShadow() {
    return theme.getString("projectExplorerPresentableTextShadow");
  }

  @Override
  public String projectExplorerInfoTextShadow() {
    return theme.getString("projectExplorerInfoTextShadow");
  }

  @Override
  public String projectExplorerSelectedRowBackground() {
    return theme.getString("projectExplorerSelectedRowBackground");
  }

  @Override
  public String projectExplorerSelectedRowBorder() {
    return theme.getString("projectExplorerSelectedRowBorder");
  }

  @Override
  public String projectExplorerHoverRowBackground() {
    return theme.getString("projectExplorerHoverRowBackground");
  }

  @Override
  public String projectExplorerHoverRowBorder() {
    return theme.getString("projectExplorerHoverRowBorder");
  }

  @Override
  public String projectExplorerVcsHead() {
    return theme.getString("projectExplorerVcsHead");
  }

  @Override
  public String loaderExpanderColor() {
    return theme.getString("loaderExpanderColor");
  }

  @Override
  public String loaderIconBackgroundColor() {
    return theme.getString("loaderIconBackgroundColor");
  }

  @Override
  public String loaderProgressStatusColor() {
    return theme.getString("loaderProgressStatusColor");
  }

  @Override
  public String placeholderColor() {
    return theme.getString("placeholderColor");
  }

  @Override
  public String categoryHeaderButtonHoverColor() {
    return theme.getString("categoryHeaderButtonHoverColor");
  }

  @Override
  public String categoryHeaderButtonColor() {
    return theme.getString("categoryHeaderButtonColor");
  }

  @Override
  public String categoryElementButtonHoverColor() {
    return theme.getString("categoryElementButtonHoverColor");
  }

  @Override
  public String categoryElementButtonColor() {
    return theme.getString("categoryElementButtonColor");
  }

  @Override
  public String categorySelectElementBackgroundColor() {
    return theme.getString("categorySelectElementBackgroundColor");
  }

  @Override
  public String categorySelectElementColor() {
    return theme.getString("categorySelectElementColor");
  }

  @Override
  public String notificationPopupSuccessBackground() {
    return theme.getString("notificationPopupSuccessBackground");
  }

  @Override
  public String notificationPopupFailBackground() {
    return theme.getString("notificationPopupFailBackground");
  }

  @Override
  public String notificationPopupProgressBackground() {
    return theme.getString("notificationPopupProgressBackground");
  }

  @Override
  public String notificationPopupWarningBackground() {
    return theme.getString("notificationPopupWarningBackground");
  }

  @Override
  public String notificationPopupPanelShadow() {
    return theme.getString("notificationPopupPanelShadow");
  }

  @Override
  public String notificationPopupIconSuccessFill() {
    return theme.getString("notificationPopupIconSuccessFill");
  }

  @Override
  public String notificationPopupIconFailFill() {
    return theme.getString("notificationPopupIconFailFill");
  }

  @Override
  public String notificationPopupIconProgressFill() {
    return theme.getString("notificationPopupIconProgressFill");
  }

  @Override
  public String notificationPopupIconWarningFill() {
    return theme.getString("notificationPopupIconWarningFill");
  }

  @Override
  public String notificationPopupIconSvgFill() {
    return theme.getString("notificationPopupIconSvgFill");
  }

  @Override
  public String notificationPopupTextColor() {
    return theme.getString("notificationPopupTextColor");
  }

  @Override
  public String closeNotificationButtonColor() {
    return theme.getString("closeNotificationButtonColor");
  }

  @Override
  public String closeNotificationHoveredButtonColor() {
    return theme.getString("closeNotificationHoveredButtonColor");
  }

  @Override
  public String projectExplorerReadonlyItemBackground() {
    return theme.getString("projectExplorerReadonlyItemBackground");
  }

  @Override
  public String projectExplorerTestItemBackground() {
    return theme.getString("projectExplorerTestItemBackground");
  }

  @Override
  public String editorTabPinBackgroundColor() {
    return theme.getString("editorTabPinBackgroundColor");
  }

  @Override
  public String editorTabPinDropShadow() {
    return theme.getString("editorTabPinDropShadow");
  }

  @Override
  public String loaderBackgroundColor() {
    return theme.getString("loaderBackgroundColor");
  }

  @Override
  public String loaderBorderColor() {
    return theme.getString("loaderBorderColor");
  }

  @Override
  public String loaderBoxShadow() {
    return theme.getString("loaderBoxShadow");
  }

  @Override
  public String loaderSVGFill() {
    return theme.getString("loaderSVGFill");
  }

  @Override
  public String loaderLabelColor() {
    return theme.getString("loaderLabelColor");
  }

  @Override
  public String outputBoxShadow() {
    return theme.getString("outputBoxShadow");
  }

  @Override
  public String toolButtonColor() {
    return theme.getString("toolButtonColor");
  }

  @Override
  public String toolButtonHoverColor() {
    return theme.getString("toolButtonHoverColor");
  }

  @Override
  public String toolButtonActiveBorder() {
    return theme.getString("toolButtonActiveBorder");
  }

  @Override
  public String toolButtonHoverBackgroundColor() {
    return theme.getString("toolButtonHoverBackgroundColor");
  }

  @Override
  public String toolButtonActiveBackgroundColor() {
    return theme.getString("toolButtonActiveBackgroundColor");
  }

  @Override
  public String toolButtonActiveColor() {
    return theme.getString("toolButtonActiveColor");
  }

  @Override
  public String toolButtonHoverBoxShadow() {
    return theme.getString("toolButtonHoverBoxShadow");
  }

  @Override
  public String toolButtonActiveBoxShadow() {
    return theme.getString("toolButtonActiveBoxShadow");
  }

  @Override
  public String vcsConsoleStagedFilesColor() {
    return theme.getString("vcsConsoleStagedFilesColor");
  }

  @Override
  public String vcsConsoleUnstagedFilesColor() {
    return theme.getString("vcsConsoleUnstagedFilesColor");
  }

  @Override
  public String vcsConsoleErrorColor() {
    return theme.getString("vcsConsoleErrorColor");
  }

  @Override
  public String vcsConsoleModifiedFilesColor() {
    return theme.getString("vcsConsoleModifiedFilesColor");
  }

  @Override
  public String vcsConsoleChangesLineNumbersColor() {
    return theme.getString("vcsConsoleChangesLineNumbersColor");
  }

  @Override
  public String vcsStatusAddedColor() {
    return theme.getString("vcsStatusAddedColor");
  }

  @Override
  public String vcsStatusModifiedColor() {
    return theme.getString("vcsStatusModifiedColor");
  }

  @Override
  public String vcsStatusUntrackedColor() {
    return theme.getString("vcsStatusUntrackedColor");
  }

  @Override
  public String vcsChangeMarkerInsertionColor() {
    return theme.getString("vcsChangeMarkerInsertionColor");
  }

  @Override
  public String vcsChangeMarkerModificationColor() {
    return theme.getString("vcsChangeMarkerModificationColor");
  }

  @Override
  public String vcsChangeMarkerDeletionColor() {
    return theme.getString("vcsChangeMarkerDeletionColor");
  }

  @Override
  public String editorPreferenceCategoryBackgroundColor() {
    return theme.getString("editorPreferenceCategoryBackgroundColor");
  }

  @Override
  public String gitPanelRepositoryChangesLabelColor() {
    return theme.getString("gitPanelRepositoryChangesLabelColor");
  }

  @Override
  public String gitPanelRepositoryChangesLabelBackgroundColor() {
    return theme.getString("gitPanelRepositoryChangesLabelBackgroundColor");
  }

  @Override
  public String resourceMonitorBarBackground() {
    return theme.getString("resourceMonitorBarBackground");
  }

  @Override
  public String popupLoaderBackgroundColor() {
    return theme.getString("popupLoaderBackgroundColor");
  }

  @Override
  public String popupLoaderBorderColor() {
    return theme.getString("popupLoaderBorderColor");
  }

  @Override
  public String popupLoaderShadow() {
    return theme.getString("popupLoaderShadow");
  }

  @Override
  public String popupLoaderTitleColor() {
    return theme.getString("popupLoaderTitleColor");
  }

  @Override
  public String popupLoaderTextColor() {
    return theme.getString("popupLoaderTextColor");
  }

  @Override
  public String cellTableHrefColor() {
    return theme.getString("cellTableHrefColor");
  }

  @Override
  public String cellTableHeaderColor() {
    return theme.getString("cellTableHeaderColor");
  }

  @Override
  public String cellTableHeaderBackground() {
    return theme.getString("cellTableHeaderBackground");
  }

  @Override
  public String cellTableOddRowBackground() {
    return theme.getString("cellTableOddRowBackground");
  }

  @Override
  public String cellTableEvenRowBackground() {
    return theme.getString("cellTableEvenRowBackground");
  }

  @Override
  public String cellTableCellColor() {
    return theme.getString("cellTableCellColor");
  }

  @Override
  public String findResultsBackground() {
    return theme.getString("findResultsBackground");
  }

  @Override
  public String findResultsTextColor() {
    return theme.getString("findResultsTextColor");
  }
}
