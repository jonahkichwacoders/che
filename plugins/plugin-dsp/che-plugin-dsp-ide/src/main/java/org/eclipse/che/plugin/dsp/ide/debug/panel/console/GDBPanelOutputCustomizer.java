package org.eclipse.che.plugin.dsp.ide.debug.panel.console;

import static com.google.gwt.regexp.shared.RegExp.compile;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.console.AbstractOutputCustomizer;
import org.eclipse.che.ide.resource.Path;

public class GDBPanelOutputCustomizer extends AbstractOutputCustomizer {

  private static final RegExp LOCATION_AT = compile("( at )(.+):(\\d+)");
  private static final RegExp LOCATION_WHITESPACE = compile("([ \t])([^ \\t]+):(\\d+)");

  public GDBPanelOutputCustomizer(AppContext appContext, EditorAgent editorAgent) {
    super(appContext, editorAgent);

    exportDebugLocationMessageAnchorClickHandlerFunction();
  }

  @Override
  public boolean canCustomize(String text) {
    // Don't do the regex check twice, once is enough and return the unmodified string if fail
    return true;
  }

  @Override
  public String customize(String text) {
    MatchResult exec = LOCATION_AT.exec(text);
    if (exec != null) {
      return customizeDebugLocationMessage(exec);
    }
    exec = LOCATION_WHITESPACE.exec(text);
    if (exec != null) {
      return customizeDebugLocationMessage(exec);
    }

    return text;
  }

  /*
   * Customizes a linker message line
   */
  private String customizeDebugLocationMessage(MatchResult exec) {
    String input = exec.getInput();
    try {
      String match = exec.getGroup(0);
      int indexOfMatch = exec.getIndex();
      int indexEndOfMatch = exec.getIndex() + match.length();
      String matchBeforeFilename = exec.getGroup(1);
      String matchWithoutLeading = match.substring(matchBeforeFilename.length());
      String fileName = exec.getGroup(2);
      String lineNumber = exec.getGroup(3);

      StringBuilder custom = new StringBuilder(input);
      custom.setLength(indexOfMatch);
      custom.append(matchBeforeFilename);
      custom.append("<a href='javascript:openDLM(\"");
      custom.append(fileName);
      custom.append("\",");
      custom.append(lineNumber);
      custom.append(");'>");
      custom.append(matchWithoutLeading);
      custom.append("</a>");
      custom.append(input.substring(indexEndOfMatch));

      return custom.toString();
    } catch (IndexOutOfBoundsException ex) {
      // ignore
    } catch (NumberFormatException ex) {
      // ignore
    }

    return input;
  }
  /**
   * A callback that is to be called for a debug location message anchor
   *
   * @param fileName
   * @param lineNumber
   */
  public void handleDebugLocationMessageAnchorClick(String fileName, final int lineNumber) {
    if (fileName == null) {
      return;
    }

    openFileInEditorAndReveal(
        appContext, editorAgent, Path.valueOf(fileName).removeFirstSegments(1), lineNumber, 0);
  }

  /** Sets up a callback for the message anchor */
  public native void exportDebugLocationMessageAnchorClickHandlerFunction() /*-{
		var that = this;
		$wnd.openDLM = $entry(function(fileName, lineNumber) {
			that.@org.eclipse.che.plugin.dsp.ide.debug.panel.console.GDBPanelOutputCustomizer::handleDebugLocationMessageAnchorClick(*)(fileName,lineNumber);
		});
  }-*/;
}
