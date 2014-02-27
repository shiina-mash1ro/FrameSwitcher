package krasa.frameswitcher;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

/**
 * @author Vojtech Krasa
 */
public class FocusUtils {
	public static void requestFocus(Project project, final boolean useRobot) {
		JFrame frame = WindowManager.getInstance().getFrame(project);

		//the only reliable way I found to bring it to the top
		boolean aot = frame.isAlwaysOnTop();
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(aot);

		int frameState = frame.getExtendedState();
		if ((frameState & Frame.ICONIFIED) == Frame.ICONIFIED) {
			// restore the frame if it is minimized
			frame.setExtendedState(frameState ^ Frame.ICONIFIED);
		}
		frame.toFront();
		frame.requestFocus();
		if (useRobot) {
			try {
				//remember the last location of mouse
				final Point oldMouseLocation = MouseInfo.getPointerInfo().getLocation();

				//simulate a mouse click on title bar of window
				Robot robot = new Robot();
				robot.mouseMove(frame.getX(), frame.getY());
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

				//move mouse to old location
				robot.mouseMove((int) oldMouseLocation.getX(), (int) oldMouseLocation.getY());
			} catch (Exception ex) {
				//just ignore exception, or you can handle it as you want
			} finally {
				frame.setAlwaysOnTop(false);
			}
		}
	}


}