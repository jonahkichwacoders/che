package org.eclipse.che.api.debug.shared.model.event;

public interface ConsoleEvent extends DebuggerEvent {
  String getConsoleText();
}
