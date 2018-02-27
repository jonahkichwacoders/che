package org.eclipse.che.api.debug.shared.model.impl.event;

import org.eclipse.che.api.debug.shared.model.event.ConsoleEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;

public class ConsoleEventImpl extends DebuggerEventImpl implements ConsoleEvent {

  private String text;

  public ConsoleEventImpl(String text) {
    super(DebuggerEvent.TYPE.CONSOLE);
    this.text = text;
  }

  @Override
  public String getConsoleText() {
    return text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConsoleEventImpl)) return false;
    if (!super.equals(o)) return false;

    ConsoleEventImpl that = (ConsoleEventImpl) o;

    return !(text != null ? !text.equals(that.text) : that.text != null);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (text != null ? text.hashCode() : 0);
    return result;
  }
}
