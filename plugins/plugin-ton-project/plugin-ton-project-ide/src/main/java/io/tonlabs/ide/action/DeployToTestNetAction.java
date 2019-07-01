package io.tonlabs.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.tonlabs.ide.TonProjectResources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;

@Singleton
public class DeployToTestNetAction extends TonProjectAction {
  /**
   * Constructor.
   *
   * @param appContext the IDE application context
   */
  @Inject
  public DeployToTestNetAction(AppContext appContext) {
    super(
        appContext,
        "Deploy to TestNet",
        "Compile the smart contract and deploy it to TestNet.",
        TonProjectResources.INSTANCE.deployIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
