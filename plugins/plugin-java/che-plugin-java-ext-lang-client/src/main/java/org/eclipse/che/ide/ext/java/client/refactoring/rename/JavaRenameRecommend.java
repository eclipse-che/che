package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;

public class JavaRenameRecommend {

  private RefactoringServiceClient refactoringServiceClient;
  private TextEditor textEditor;
  private NotificationManager notificationManager;

  public void setTextEditor(TextEditor textEditor) {
    this.textEditor = textEditor;
  }

  public void setNotificationManager(NotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }

  public void setRefactoringServiceClient(RefactoringServiceClient refactoringServiceClient) {
    this.refactoringServiceClient = refactoringServiceClient;
  }

  public void recommend() {
    refactoringServiceClient
        .getRecommendationPosition()
        .then(
            new Operation<String>() {
              @Override
              public void apply(String info) throws OperationException {
                refactoringServiceClient
                    .getRecommendation()
                    .then(
                        new Operation<String>() {
                          @Override
                          public void apply(String text) throws OperationException {
                            if (text.equals("$$null")) return;
                            String[] args = info.split(",");

                            int from = Integer.parseInt(args[0]);
                            int len = Integer.parseInt(args[1]);

                            RecommendNotificationListener recommendNotificationListener =
                                new RecommendNotificationListener(textEditor, from, len);

                            notificationManager.notify(
                                "Rename Recommendation",
                                text,
                                SUCCESS,
                                FLOAT_MODE,
                                recommendNotificationListener);
                          }
                        })
                    .catchError(
                        new Operation<PromiseError>() {
                          @Override
                          public void apply(PromiseError arg) throws OperationException {
                            notificationManager.notify(
                                "Fail to recommend", arg.getMessage(), FAIL, FLOAT_MODE);
                          }
                        });
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Fail to recommend", arg.getMessage(), FAIL, FLOAT_MODE);
              }
            });
  }
}
