import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class GraphActionHandler extends JPanel {

	private static final long serialVersionUID = 1L;
	private GraphEditor parent;
	
	public GraphActionHandler(GraphEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInGraphEditor(".saved");
		}
	}	
	
	public class ToggleClipZeroAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ToggleClipZeroAction() {
			super("Toggle Clip Zero");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.toggleClipZero();
		}
	}	
	
	public class OctaveViewAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public OctaveViewAction() {
			super("Select Octave Range");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.promptForOctaveView(parent);
		}
	}
	
	public class ColorViewAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public ColorViewAction() {
			super("Select Color View");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			GraphEditor.promptForColorView(parent);
		}
	}	

	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        viewMenu.add(new ToggleClipZeroAction());
        JMenu octaveMenu = new JMenu("Octave");
        menuBar.add(octaveMenu);
        octaveMenu.add(new OctaveViewAction());
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);
        colorMenu.add(new ColorViewAction()); 
        return menuBar;
	}

}