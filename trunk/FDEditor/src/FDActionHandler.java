import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class FDActionHandler extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2754195729464045141L;
	private FDEditor parent;
	
	public FDActionHandler(FDEditor parent) {
		this.parent = parent;
	}
	
	public class OpenAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8330693329974727658L;

		public OpenAction() {
			super("Open");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			parent.openFileInFDEditor();
		}
	}	

	public class ExitAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6690671099967989664L;

		public ExitAction() {
			super("Exit");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.exit(0);
		}
	}

	public class PlayAllAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9196870261707581557L;

		public PlayAllAction() {
			super("Play All");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Play\n");
			FDEditor.playSelectedDataInCurrentWindow(parent);
		}
	}
	
	public class TestAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public TestAction() {
			super("Test");
		}

		// @0verride
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Test\n");
			MathTools mt = new MathTools();
			mt.runTests();
		}
	}
	
	public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        //Create the File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenAction());
        fileMenu.add(new ExitAction());
        //Create the Play menu
        JMenu playMenu = new JMenu("Play");
        menuBar.add(playMenu);        
        playMenu.add(new JMenuItem(new PlayAllAction()));
        JMenu testMenu = new JMenu("Test");
        testMenu.add(new TestAction());
        menuBar.add(testMenu);
        return menuBar;
	}

}
