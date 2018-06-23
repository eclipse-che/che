package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationListener;
import org.eclipse.che.ide.api.notification.NotificationManager;

public class RecommendNotificationListener implements NotificationListener {
  NotificationManager notificationManager;
  TextEditor textEditor;
  int from, len;

  public RecommendNotificationListener(TextEditor textEditor, int from, int len) {
    this.textEditor = textEditor;
    this.from = from;
    this.len = len;
  }

  public void onClick(Notification notification) {}

  public void onDoubleClick(Notification notification) {
    textEditor
        .getDocument()
        .setSelectedRange(LinearRange.createWithStart(from).andLength(len), true);
    textEditor.activate();
  }

  public void onClose(Notification notification) {}
}
