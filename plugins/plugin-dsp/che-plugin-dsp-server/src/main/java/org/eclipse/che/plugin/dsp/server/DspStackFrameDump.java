package org.eclipse.che.plugin.dsp.server;

import java.util.List;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;

public class DspStackFrameDump extends StackFrameDumpImpl {

  private long frameId;

  public DspStackFrameDump(
      List<Field> fields, List<Variable> variables, Location location, long frameId) {
    super(fields, variables, location);
    this.frameId = frameId;
  }

  public long getFrameId() {
    return frameId;
  }
}
