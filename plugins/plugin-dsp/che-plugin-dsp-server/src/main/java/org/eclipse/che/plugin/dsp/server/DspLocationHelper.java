/*
 * Copyright (c) 2017 Kichwa Coders Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Kichwa Coders Ltd. - initial API and implementation
 */
package org.eclipse.che.plugin.dsp.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.MethodImpl;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.lsp4j.debug.Source;

@Singleton
public class DspLocationHelper {

  @Inject private FsManager fsManager;
  @Inject private ProjectManager projectManager;
  @Inject private PathTransformer pathTransformer;

  public Optional<Location> toLocation(
      Source source, int lineNumber, String frameName, long threadId) {
    if (source == null) {
      return Optional.empty();
    }
    String pathString = source.getPath();
    if (pathString == null) {
      return Optional.empty();
    }

    Path path = Paths.get(pathString);
    String workspacePath = pathTransformer.transform(path);

    if (!fsManager.exists(workspacePath)) {
      return Optional.empty();
    }

    String resourceProjectPath =
        projectManager.getClosest(workspacePath).map(p -> p.getPath()).orElse(null);
    if (frameName == null) {
      return Optional.of(new LocationImpl(workspacePath, lineNumber, resourceProjectPath));
    } else {
      Method method = new MethodImpl(frameName, Collections.emptyList());
      return Optional.of(
          new LocationImpl(
              workspacePath, lineNumber, false, 0, resourceProjectPath, method, threadId));
    }
  }

  public Optional<Location> toLocation(Source source, int lineNumber) {
    return toLocation(source, lineNumber, null, 0);
  }

  public Source toSource(Location location) {
    String target = location.getTarget();
    Path systemPath = pathTransformer.transform(target);
    Source source = new Source();
    source.setName(target);
    source.setPath(systemPath.toString());
    return source;
  }
}
