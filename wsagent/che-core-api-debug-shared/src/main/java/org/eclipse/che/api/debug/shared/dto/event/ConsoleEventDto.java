package org.eclipse.che.api.debug.shared.dto.event;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ConsoleEventDto extends DebuggerEventDto {
  TYPE getType();

  void setType(TYPE type);

  ConsoleEventDto withType(TYPE type);

  String getConsoleText();

  void setConsoleText(String text);

  ConsoleEventDto withConsoleText(String text);
}
