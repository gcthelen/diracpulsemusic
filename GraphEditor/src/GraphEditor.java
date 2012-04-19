
import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.io.*;

public class GraphEditor extends JFrame {

	private static final long serialVersionUID = 6252327634736973395L;

	public static MultiWindow parent;
	public static GraphView view;
	public static GraphController controller;
	public static GraphActionHandler actionHandler;
	public static HashMap<Long, Harmonic> harmonicIDToHarmonic;
	public static int minHarmonicLength = 1;
	public static int maxTime = 1;
	public static int minTime = 0;
	public static int maxNote = 1;
	public static int minNote = 0;
	public static double maxLogAmplitude = 1.0;
	public static double minLogAmplitude = 0.0;
	public static boolean minTimeAlwaysZero = true;
	public static int minViewTime = 0;
	public static int maxViewTime = 1;
	public static int maxViewNote = 0;
	public static int minViewNote = 1;
	public static double maxViewLogAmplitude = 0.0;
	public static double minViewLogAmplitude = 1.0;
	public static boolean clipZero = false;
	public static double zoomFactor = 2.0;
	
	public JMenuBar createMenuBar() {
        GraphActionHandler actionHandler = new GraphActionHandler(this);
        return actionHandler.createMenuBar();
    }

	public void openFileInGraphEditor(String extension) {
		startReadData();
        String fileName = FileTools.PromptForFileOpen(view, extension);
        GraphFileInput.ReadBinaryFileData(fileName);
        endReadData();
        this.setTitle(fileName);
        view.repaint();
	}
	
	static void startReadData() {
		harmonicIDToHarmonic = new HashMap<Long, Harmonic>();
		maxTime = FDData.minTime;
		minTime = FDData.maxTime;
		maxNote = FDData.getMinNote();
		minNote = FDData.getMaxNote();
		maxLogAmplitude = FDData.minLogAmplitude;
	}
	
	public static void addData(FDData data) {
		if(!harmonicIDToHarmonic.containsKey(data.getHarmonicID())) {
			harmonicIDToHarmonic.put(data.getHarmonicID(), new Harmonic(data.getHarmonicID()));
		}
		harmonicIDToHarmonic.get(data.getHarmonicID()).addData(data);
	}
	
	static void endReadData() {
		for(Harmonic harmonic: harmonicIDToHarmonic.values()) {
			if(harmonic.getStartTime() < minTime) minTime = harmonic.getStartTime();
			if(harmonic.getEndTime() > maxTime) maxTime = harmonic.getEndTime();
			if(harmonic.getAverageNote() < minNote) minNote = harmonic.getAverageNote();
			if(harmonic.getAverageNote() > maxNote) maxNote = harmonic.getAverageNote();
			if(harmonic.getMaxLogAmplitude() > maxLogAmplitude) maxLogAmplitude = harmonic.getMaxLogAmplitude();
		}
		resetView();
	}
	
	static void resetView() {
		if(minTimeAlwaysZero) minTime = 0;
		minViewTime = minTime;
		maxViewTime = maxTime;
		minViewNote = minNote;
		maxViewNote = maxNote;
		minViewLogAmplitude = 0.0;
		maxViewLogAmplitude = maxLogAmplitude;
	}

    public GraphEditor() {
        view = new GraphView();
        view.setBackground(Color.black);
        controller = new GraphController(this);
        setJMenuBar(createMenuBar());
        view.addMouseListener(controller);
        controller.setView(view);
        add(view);
        setSize(1500, 800);
        harmonicIDToHarmonic = new HashMap<Long, Harmonic>();
    }
    
	private static void createAndShowGUI() {
		// Create and set up the window.
		parent = new MultiWindow();
		parent.graphEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.graphEditorFrame.pack();
		parent.graphEditorFrame.setVisible(true);
		//parent.fdEditorFrame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//parent.fdEditorFrame2.pack();
		//parent.fdEditorFrame2.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public static void toggleClipZero() {
		clipZero = !clipZero;
		view.repaint();
	}
	
	public static int frequencyInHzToNote(double freqInHz) {
		return (int) Math.round(Math.log(freqInHz)/Math.log(2.0) * (double) FDData.noteBase);
	}
	
	public static void promptForOctaveView(GraphEditor parent) {
		Object[] octaves = {0, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
		Integer octave =  (Integer) JOptionPane.showInputDialog(parent, "Frequency Range Select", 
										"Select Minimum Freq", JOptionPane.PLAIN_MESSAGE, null, octaves, 0);
		if(octave == null) return;
		if(octave == 0 ) {
			minViewNote = minNote;
			maxViewNote = maxNote;
			view.repaint();
			return;
		}
		minViewNote = frequencyInHzToNote(octave);
		maxViewNote = minViewNote + FDData.noteBase * 2;
		view.repaint();
	}
	
	public static void promptForColorView(GraphEditor parent) {
		Object[] colorData = {"Amplitude", "Frequency", "Harmonics"};
		String choice =  (String) JOptionPane.showInputDialog(parent, "Color Display Select", 
										"Select Color View", JOptionPane.PLAIN_MESSAGE, null, colorData, 0);
		if(choice == null) return;
		if(choice.equals("Amplitude")) GraphView.colorView = GraphView.ColorView.AMPLITUDE;
		if(choice.equals("Frequency")) GraphView.colorView = GraphView.ColorView.FREQUENCY;
		if(choice.equals("Harmonics")) GraphView.colorView = GraphView.ColorView.HARMONICS;
		view.repaint();
	}
	
	public static void promptForYView(GraphEditor parent) {
		Object[] colorData = {"Amplitude", "Frequency"};
		String choice =  (String) JOptionPane.showInputDialog(parent, "Color Display Select", 
										"Select Color View", JOptionPane.PLAIN_MESSAGE, null, colorData, 0);
		if(choice == null) return;
		if(choice.equals("Amplitude")) GraphView.yView = GraphView.YView.AMPLITUDE;
		if(choice.equals("Frequency")) GraphView.yView = GraphView.YView.FREQUENCY;
		view.repaint();
	}
	
	public static void zoomInX(int x) {
		double divisor = 2 * zoomFactor;
		minViewTime = GraphUtils.screenXToTime(x - view.getWidth() / divisor);
		maxViewTime = GraphUtils.screenXToTime(x + view.getWidth() / divisor);
		if(minViewTime < 0) {
			maxViewTime -= minViewTime;
			minViewTime = 0;
		}
		if(maxViewTime > maxTime) {
			minViewTime += (maxViewTime - maxTime);
			maxViewTime = maxTime;
		}
		view.repaint();
	}
	
	public static void zoomInAmplitude(int y) {
		double divisor = 2 * zoomFactor;
		System.out.println(y);
		minViewLogAmplitude = GraphUtils.screenYToValue(y + view.getHeight() / divisor);
		maxViewLogAmplitude = GraphUtils.screenYToValue(y - view.getHeight() / divisor);
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
		if(minViewLogAmplitude < 0) {
			maxViewLogAmplitude -= minViewLogAmplitude;
			minViewLogAmplitude = 0;
		}
		if(maxViewLogAmplitude > maxLogAmplitude) {
			minViewLogAmplitude -= (maxViewLogAmplitude - maxLogAmplitude);
			maxViewLogAmplitude = maxLogAmplitude;
		}
		System.out.println(minViewLogAmplitude + " " + maxViewLogAmplitude);
		view.repaint();
	}
	
	public static void zoomInFrequency(int y) {
		double divisor = 2 * zoomFactor;
		minViewNote = (int) Math.round(GraphUtils.screenYToValue(y + view.getHeight() / divisor));
		maxViewNote = (int) Math.round(GraphUtils.screenYToValue(y - view.getHeight() / divisor));
		if(minViewNote < 0) {
			maxViewNote -= minViewNote;
			minViewNote = 0;
		}
		if(maxViewNote > maxNote) {
			minViewNote -= (maxViewNote - maxNote);
			maxViewNote = maxNote;
		}
		view.repaint();
	}
	
	public static void playDataInCurrentWindow(GraphEditor parent) {
		new PlayDataInWindow(parent, 50, view.getTimeAxisWidthInMillis());
	}

	public static void drawPlayTime(int offsetInMillis) {
		view.drawPlayTime(offsetInMillis);
		view.repaint();
	}

}
