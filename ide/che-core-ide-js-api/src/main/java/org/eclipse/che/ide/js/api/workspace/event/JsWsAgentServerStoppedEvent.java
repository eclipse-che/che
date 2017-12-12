communication

package org.eclipse.che.ide.js.api.workspace.event;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.eclipse.che.ide.api.eventbus.EventType;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;

/** @author Yevhen Vydolob */
@JsType(name = "WsAgentServerStoppedEvent")
public class JsWsAgentServerStoppedEvent {

  public static final EventType<JsWsAgentServerStoppedEvent> TYPE = () -> "ws.agent.server.stopped";
  @JsIgnore private final WsAgentServerStoppedEvent event;

  @JsIgnore
  public JsWsAgentServerStoppedEvent(WsAgentServerStoppedEvent event) {
    this.event = event;
  }

  /** Returns the related machine's name. */
  @JsMethod
  public String getMachineName() {
    return event.getMachineName();
  }
}
