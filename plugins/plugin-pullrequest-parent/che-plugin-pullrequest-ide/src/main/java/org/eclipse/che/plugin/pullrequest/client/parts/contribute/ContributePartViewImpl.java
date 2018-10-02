/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.parts.contribute;

import static com.google.gwt.dom.client.Style.Cursor.POINTER;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static org.vectomatic.dom.svg.ui.SVGButtonBase.SVGFaceName.UP;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.buttonLoader.ButtonLoaderResources;
import org.eclipse.che.ide.ui.listbox.CustomListBox;
import org.eclipse.che.ide.ui.status.StatusText;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.ContributeResources;
import org.eclipse.che.plugin.pullrequest.client.dialogs.paste.PasteEvent;
import org.vectomatic.dom.svg.ui.SVGPushButton;

/** Implementation of {@link ContributePartView}. */
public class ContributePartViewImpl extends BaseView<ContributePartView.ActionDelegate>
    implements ContributePartView {

  /** The status component. */
  private final StatusSteps statusSteps;

  private final StatusText statusText;

  @UiField ScrollPanel contributePanel;
  @UiField FlowPanel stubPanel;

  /** The contribute button. */
  @UiField Button contributeButton;

  /** The resources for the view. */
  @UiField(provided = true)
  ContributeResources resources;

  /** The component for the URL of factory repository. */
  @UiField Anchor repositoryUrl;

  /** The component for the name of contribute to branch. */
  @UiField Label contributeToBranch;

  /** The component for the name of the project */
  @UiField Label projectName;

  /** The input component for the contribution branch name. */
  @UiField CustomListBox contributionBranchName;

  /** Button used to refresh the contribution branch name list. */
  @UiField SVGPushButton refreshContributionBranchNameListButton;

  /** The input component for the contribution title. */
  @UiField TextBox contributionTitle;

  /** The input zone for the contribution comment. */
  @UiField TextArea contributionComment;

  /** The i18n messages. */
  @UiField(provided = true)
  ContributeMessages messages;

  /** The contribution status section. */
  @UiField FlowPanel statusSection;

  /** The status section message. */
  @UiField Label statusSectionMessage;

  /** Open on repository host button. */
  @UiField Button openPullRequestOnVcsHostButton;

  /** The start new contribution section. */
  @UiField HTMLPanel newContributionSection;

  /** The new contribution button. */
  @UiField Button newContributionButton;

  /** The contribute button text. */
  private String contributeButtonText;

  @Inject
  public ContributePartViewImpl(
      @NotNull final ContributeMessages messages,
      @NotNull final ContributeResources resources,
      @NotNull final ButtonLoaderResources buttonLoaderResources,
      @NotNull final ContributePartViewUiBinder uiBinder,
      StatusText<FlowPanel> statusText) {
    this.messages = messages;
    this.resources = resources;
    this.statusSteps = new StatusSteps();

    this.statusText = statusText;

    setContentWidget(uiBinder.createAndBindUi(this));

    statusText.init(stubPanel, input -> true);

    setTitle(messages.contributePartTitle());

    this.contributeButtonText = contributeButton.getText();
    this.contributeButton.addStyleName(buttonLoaderResources.buttonLoaderCss().buttonLoader());

    this.refreshContributionBranchNameListButton.getElement().getStyle().setWidth(23, PX);
    this.refreshContributionBranchNameListButton.getElement().getStyle().setHeight(20, PX);
    this.refreshContributionBranchNameListButton.getElement().getStyle().setCursor(POINTER);
    this.refreshContributionBranchNameListButton.showFace(UP);
    this.refreshContributionBranchNameListButton
        .getElement()
        .getStyle()
        .setProperty("fill", "#dbdbdb");

    this.refreshContributionBranchNameListButton.ensureDebugId("refreshContributionBranchButton");
    this.statusSection.setVisible(false);
    this.newContributionSection.setVisible(false);
    this.contributionTitle
        .getElement()
        .setPropertyString(
            "placeholder",
            messages.contributePartConfigureContributionSectionContributionTitlePlaceholder());
    this.contributionComment
        .getElement()
        .setPropertyString(
            "placeholder",
            messages.contributePartConfigureContributionSectionContributionCommentPlaceholder());

    this.statusSection.insert(statusSteps, 1);
  }

  @Override
  public void setRepositoryUrl(final String url) {
    repositoryUrl.setHref(url);
    repositoryUrl.setText(url);
  }

  @Override
  public void setContributeToBranch(final String branch) {
    contributeToBranch.setText(branch);
  }

  @Override
  public void setProjectName(String projectName) {
    this.projectName.setText(projectName);
  }

  @Override
  public void setContributeButtonText(final String text) {
    contributeButton.setText(text);
    contributeButtonText = contributeButton.getText();
  }

  @Override
  public String getContributionBranchName() {
    final int selectedIndex = contributionBranchName.getSelectedIndex();
    return selectedIndex == -1 ? null : contributionBranchName.getValue(selectedIndex);
  }

  @Override
  public void setContributionBranchName(final String branchName) {
    for (int i = 0; i < contributionBranchName.getItemCount(); i++) {
      if (contributionBranchName.getValue(i).equals(branchName)) {
        contributionBranchName.setSelectedIndex(i);
        return;
      }
    }

    if (contributionBranchName.getItemCount() > 1) {
      contributionBranchName.setSelectedIndex(1);
    }
  }

  @Override
  public void setContributionBranchNameList(final List<String> branchNames) {
    final String selectedBranchName = getContributionBranchName();

    contributionBranchName.clear();
    contributionBranchName.addItem(
        messages
            .contributePartConfigureContributionSectionContributionBranchNameCreateNewItemText());
    for (final String oneBranchName : branchNames) {
      contributionBranchName.addItem(oneBranchName);
    }

    setContributionBranchName(selectedBranchName);
  }

  @Override
  public String getContributionComment() {
    return contributionComment.getValue();
  }

  @Override
  public void setContributionComment(final String comment) {
    contributionComment.setText(comment);
  }

  @Override
  public void addContributionCommentChangedHandler(TextChangedHandler handler) {
    contributionComment.addKeyUpHandler(new TextChangedHandlerAdapter(handler));
  }

  @Override
  public String getContributionTitle() {
    return contributionTitle.getValue();
  }

  @Override
  public void setContributionTitle(final String title) {
    contributionTitle.setText(title);
  }

  @Override
  public void addContributionTitleChangedHandler(TextChangedHandler handler) {
    contributionTitle.addKeyUpHandler(new TextChangedHandlerAdapter(handler));
  }

  @Override
  public void addBranchChangedHandler(final TextChangedHandler changeHandler) {
    contributionBranchName.addChangeHandler(
        new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            changeHandler.onTextChanged(contributionBranchName.getSelectedItemText());
          }
        });
  }

  @Override
  public void setContributionBranchNameEnabled(final boolean enabled) {
    contributionBranchName.setEnabled(enabled);
  }

  @Override
  public void setContributionCommentEnabled(final boolean enabled) {
    contributionComment.setEnabled(enabled);
    if (!enabled) {
      contributionComment.getElement().getStyle().setBackgroundColor("#5a5c5c");
    } else {
      contributionComment.getElement().getStyle().clearBackgroundColor();
    }
  }

  @Override
  public void setContributionTitleEnabled(final boolean enabled) {
    contributionTitle.setEnabled(enabled);
  }

  @Override
  public void setContributeButtonEnabled(final boolean enabled) {
    contributeButton.setEnabled(enabled);
  }

  @Override
  public void showContributionTitleError(final boolean showError) {
    if (showError) {
      contributionTitle.addStyleName(resources.contributeCss().inputError());
    } else {
      contributionTitle.removeStyleName(resources.contributeCss().inputError());
    }
  }

  @Override
  public void showStatusSection(final String... statusSteps) {
    this.statusSteps.removeAll();
    for (final String oneStatusStep : statusSteps) {
      this.statusSteps.addStep(oneStatusStep);
    }
    statusSection.setVisible(true);
  }

  @Override
  public void setCurrentStatusStepStatus(boolean success) {
    statusSteps.setCurrentStepStatus(success);
  }

  @Override
  public String getCurrentStatusStepName() {
    return statusSteps.getCurrentStepName();
  }

  @Override
  public void showStatusSectionMessage(final String message, final boolean error) {
    if (error) {
      statusSectionMessage.addStyleName(resources.contributeCss().errorMessage());
    } else {
      statusSectionMessage.removeStyleName(resources.contributeCss().errorMessage());
    }

    statusSectionMessage.setText(message);
    statusSectionMessage.setVisible(true);
  }

  @Override
  public void hideStatusSectionMessage() {
    statusSectionMessage.setVisible(false);
  }

  @Override
  public void hideStatusSection() {
    statusSection.setVisible(false);
  }

  @Override
  public void setContributionProgressState(final boolean progress) {
    if (progress) {
      contributeButton.setHTML("<i></i>");
    } else {
      contributeButton.setText(contributeButtonText);
    }
  }

  @Override
  public void showNewContributionSection(final String vcsHostName) {
    openPullRequestOnVcsHostButton.setText(
        messages.contributePartNewContributionSectionButtonOpenPullRequestOnVcsHostText(
            vcsHostName));
    newContributionSection.setVisible(true);
  }

  @Override
  public void showStub(String content) {
    contributePanel.setVisible(false);
    stubPanel.setVisible(true);

    statusText.setText(content);
    statusText.paint();
  }

  @Override
  public void showContent() {
    contributePanel.setVisible(true);
    stubPanel.setVisible(false);
  }

  @Override
  public void hideNewContributionSection() {
    newContributionSection.setVisible(false);
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("contributionBranchName")
  protected void contributionBranchNameChange(final ChangeEvent event) {
    final int selectedIndex = contributionBranchName.getSelectedIndex();
    if (selectedIndex == 0) {
      delegate.onCreateNewBranch();
    }
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("refreshContributionBranchNameListButton")
  protected void refreshContributionBranchNameList(final ClickEvent event) {
    delegate.onRefreshContributionBranchNameList();
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("contributionComment")
  protected void contributionCommentChanged(final ValueChangeEvent<String> event) {
    delegate.updateControls();
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("contributionTitle")
  protected void contributionTitleChanged(final ValueChangeEvent<String> event) {
    delegate.updateControls();
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("openPullRequestOnVcsHostButton")
  protected void openPullRequestOnVcsHostClick(final ClickEvent event) {
    delegate.onOpenPullRequestOnVcsHost();
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("newContributionButton")
  protected void newContributionClick(final ClickEvent event) {
    delegate.onNewContribution();
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("contributeButton")
  protected void contributeClick(final ClickEvent event) {
    delegate.onContribute();
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("contributionTitle")
  protected void contributionTitleKeyUp(final KeyUpEvent event) {
    delegate.updateControls();
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("contributionTitle")
  protected void contributionTitlePaste(final PasteEvent event) {
    delegate.updateControls();
  }

  private class StatusSteps extends FlowPanel {
    private final List<StatusStep> steps;
    private int currentStep;

    private StatusSteps() {
      this.currentStep = 0;
      this.steps = new ArrayList<>();

      addStyleName(resources.contributeCss().statusSteps());
    }

    public void addStep(final String label) {
      final StatusStep statusStep = new StatusStep(steps.size() + 1, label);

      steps.add(statusStep);
      add(statusStep);
    }

    public void removeAll() {
      clear();
      currentStep = 0;
      steps.clear();
    }

    public void setCurrentStepStatus(final boolean status) {
      steps.get(currentStep).setStatus(status);
      currentStep++;
    }

    public String getCurrentStepName() {
      return steps.get(currentStep).getLabel();
    }
  }

  private class StatusStep extends FlowPanel {
    private final SimplePanel status;
    private final String label;

    private StatusStep(final int index, final String label) {
      final Label indexLabel = new Label();
      final Label titleLabel = new Label(this.label = label);
      this.status = new SimplePanel();

      add(indexLabel);
      add(titleLabel);
      add(this.status);

      // initialize panel style
      addStyleName(resources.contributeCss().stepLabelRow());

      // initialize index style
      indexLabel.addStyleName(resources.contributeCss().statusIndexStepLabel());

      // initialize label style
      titleLabel.addStyleName(resources.contributeCss().statusTitleStepLabel());

      // initialize status style
      this.status.addStyleName(resources.contributeCss().stepLabel());
    }

    public void setStatus(final boolean success) {
      status.clear();
      status.add(getStatusIcon(success));
    }

    public String getLabel() {
      return label;
    }

    private Widget getStatusIcon(final boolean success) {
      final Widget icon = new HTML(success ? FontAwesome.CHECK : FontAwesome.EXCLAMATION_TRIANGLE);

      icon.addStyleName(
          success ? resources.contributeCss().checkIcon() : resources.contributeCss().errorIcon());

      return icon;
    }
  }

  /** Adapts {@link TextChangedHandler} to the {@link KeyUpEvent}. */
  private static class TextChangedHandlerAdapter implements KeyUpHandler {
    private final TextChangedHandler handler;

    public TextChangedHandlerAdapter(TextChangedHandler handler) {
      this.handler = handler;
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
      if (event.getSource() instanceof ValueBoxBase) {
        handler.onTextChanged(((ValueBoxBase) event.getSource()).getText());
      }
    }
  }
}
