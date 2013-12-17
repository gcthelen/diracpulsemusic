package main.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import main.Filter;
import main.Module;
import main.ModuleEditor;
import main.SynthTools;
import main.Module.ModuleType;

public class FIRFilter implements Module {
	
	public enum FilterType {
		LOWPASS,
		HIGHPASS,
		BANDPASS;
	}
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	double amplitude = 1.0;
	double freqInHz = 440.0;
	double bins = 2.0;
	double minFreqInHzNoControl = 20.0;
	final double minBins = 1.0;
	final double maxBins = 100.0;
	double alpha = 5.0;
	double duration = ModuleEditor.maxDuration;
	int cornerX;
	int cornerY;
	int width = 150; // should be >= value calculated by init
	int height = 150; // calculated by init
	FilterType type = FilterType.LOWPASS;
	
	Rectangle typeControl = null;
	Rectangle freqControl = null;
	Rectangle ampControl = null;
	Rectangle binsControl = null;	
	ArrayList<Integer> outputs;
	ArrayList<Integer> inputs;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	private class Output extends Module.Output {

		private double[] calculatedSamples = null;
		
		public Output(Module parent, Rectangle selectArea) {
			super(parent, selectArea);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double[] getSamples(HashSet<Integer> waitingForModuleID, double[] control) {
			if(calculatedSamples != null) return calculatedSamples;
			calculatedSamples = masterGetSamples(waitingForModuleID, control);
			return calculatedSamples;
		}
		
		public void clearSamples() {
			calculatedSamples = null;
		}
		
	}
	
	public FIRFilter(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.parent = parent;
		outputs = new ArrayList<Integer>();
		inputs = new ArrayList<Integer>();
		init();
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setModuleId(Integer id) {
		this.moduleID = id;
	}
	
	public Integer getModuleId() {
		return moduleID;
	}
	
	public double[] getSamplesLeft(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}
	
	public double[] getSamplesRight(HashSet<Integer> waitingForModuleIDs) {
		return null;
	}

	public double[] masterGetSamples(HashSet<Integer> waitingForModuleIDs, double[] controlIn) {
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			JOptionPane.showMessageDialog(parent.getParentFrame(), "Infinite Loop");
			return new double[0];
		}
		TreeMap<Double, double[]> freqRatioToFilter = new TreeMap<Double, double[]>();
		double[] innerControl = null;
		if(controlIn == null) {
			innerControl = new double[(int) Math.round(duration * SynthTools.sampleRate)];
			for(int index = 0; index < innerControl.length; index++) {
				innerControl[index] = 1.0;
			}
		} else {
			if(freqInHz < minFreqInHzNoControl) {
				innerControl = new double[(int) Math.round(duration * SynthTools.sampleRate)];
				for(int index = 0; index < innerControl.length; index++) {
					if(controlIn[index] < 0.0) {
						innerControl[index] = controlIn[index];
					} else {
						innerControl[index] = 1.0;
					}
				}
			} else {
				innerControl = controlIn;
			}
		}
		double[] inputSamples = new double[innerControl.length];
		for(int index = 0; index < inputSamples.length; index++) inputSamples[index] = 0.0;
		ArrayList<double[]> inputArray = new ArrayList<double[]>();
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Module.Output output = (Module.Output) parent.connectorIDToConnector.get(input.getConnection());
			inputArray.add(output.getSamples(waitingForModuleIDs, controlIn));
			waitingForModuleIDs.remove(moduleID);
		}
		for(double[] samplesIn: inputArray) {
			for(int index = 0; index < inputSamples.length; index++) {
				if(index >= samplesIn.length) break;
				inputSamples[index] += samplesIn[index]; 
			}
		}
		freqRatioToFilter.put(1.0, null);
		for(int index = 0; index < innerControl.length; index++) {
			double freqRatio = innerControl[index];
			if(!freqRatioToFilter.containsKey(freqRatio) && freqRatio > 0.0) {
				freqRatioToFilter.put(freqRatio, null);
			}
		}
		for(double freqRatio: freqRatioToFilter.keySet()) {
			double filterFreq = freqRatio * freqInHz;
			int filterLength = (int) Math.round((SynthTools.sampleRate / filterFreq) * bins);
			filterLength += filterLength % 2;
			if(type == FilterType.BANDPASS) freqRatioToFilter.put(freqRatio, Filter.getBPFilter(filterFreq, filterLength, alpha));
			if(type == FilterType.HIGHPASS) freqRatioToFilter.put(freqRatio, Filter.getHPFilter(filterFreq, filterLength, alpha));
			if(type == FilterType.LOWPASS) freqRatioToFilter.put(freqRatio, Filter.getLPFilter(filterFreq, filterLength, alpha));
		}
		double[] returnVal = new double[innerControl.length];
		for(int index = 0; index < innerControl.length; index++) {
			returnVal[index] = 0.0;
			if(innerControl[index] <= 0.0) continue;
			double[] filter = freqRatioToFilter.get(innerControl[index]);
			for(int filterIndex = 0; filterIndex < filter.length; filterIndex++) {
				int innerIndex = index + filterIndex - filter.length / 2;
				if(innerIndex < 0) continue;
				if(innerIndex == returnVal.length) break;
				returnVal[index] += inputSamples[innerIndex] * filter[filterIndex];
			}
			returnVal[index] *= amplitude;
		}
		return returnVal;
	}
	
	public void mousePressed(int x, int y) {
		if(typeControl.contains(x, y)) {
			FilterType inputType = (FilterType) JOptionPane.showInputDialog(null, "Choose a type", "Type Select", JOptionPane.INFORMATION_MESSAGE, null, FilterType.values(),  FilterType.LOWPASS);
			if(inputType == null) return;
			type = inputType;
			parent.refreshView();
			return;
		}
		if(freqControl.contains(x, y)) {
			Double inputFreqInHz = getInput("Input Frequency In Hz", ModuleEditor.minFrequency, ModuleEditor.maxFrequency);
			if(inputFreqInHz == null) return;
			freqInHz = inputFreqInHz;
			parent.refreshView();
			return;
		}
		if(ampControl.contains(x, y)) {
			Double inputAmplitude = getInput("Input Amplitude In dB", ModuleEditor.minAmplitudeIn_dB, ModuleEditor.maxAmplitudeIn_dB);
			if(inputAmplitude == null) return;
			amplitude = Math.pow(10.0, inputAmplitude / 20.0);
			parent.refreshView();
			return;
		}
		if(binsControl.contains(x, y)) {
			Double binsInput = getInput("Input Length In Bins", minBins, maxBins);
			if(binsInput == null) return;
			bins = binsInput;
			parent.refreshView();
			return;
		}
		int index = 0;
		for(Integer outputID: outputs) {
			Output output = (Output) parent.connectorIDToConnector.get(outputID);
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(outputID);
				System.out.println(type + " " + "output: " + index);
			}
			index++;
		}
		index = 0;
		for(Integer inputID: inputs) {
			Input input = (Input) parent.connectorIDToConnector.get(inputID);
			if(input.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputID);
				System.out.println(type + " inputADD: " + index);
			}
			index++;
		}
	}
	
	public Double getInput(String prompt, double minBound, double maxBound) {
		Double returnVal = null;
		String inputValue = JOptionPane.showInputDialog(prompt);
		if(inputValue == null) return null;
		try {
			returnVal = new Double(inputValue);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(parent.getParentFrame(), "Could not parse string");
			return null;
		}
		if(returnVal < minBound || returnVal > maxBound) {
			JOptionPane.showMessageDialog(parent.getParentFrame(), "Input must be between: " + minBound + " and " + maxBound);
			return null;
		}
		return returnVal;
	}
	
	public void init() {
		draw(null);
	}
	
	public void draw(Graphics g) {
		int currentX = cornerX;
		int currentY = cornerY;
		Graphics2D g2 = null;
		if(g != null) g2 = (Graphics2D) g;
		if(g2 != null) g2.setColor(Color.GRAY);
		if(g2 != null) g2.setStroke(new BasicStroke(2));
		if(g2 != null) g2.drawRect(cornerX, cornerY, width, height);
		if(g2 != null) g2.setColor(Color.DARK_GRAY);
		if(g2 != null) g2.fillRect(cornerX, cornerY, width, height);
		int fontSize = 12;
		int yStep = fontSize + 6;
		int xStep = yStep;
		if(g2 != null) g2.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		if(g2 != null) g2.setFont(font);
		currentX = cornerX + 4;
		currentY = cornerY + yStep;
		if(g2 != null) g2.drawString("FIR Filter", currentX, currentY);
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.GREEN);
		if(g2 != null) g2.drawString(type.toString(), currentX, currentY);
		if(g2 == null) typeControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Frequency: " + freqInHz, currentX, currentY);
		if(g2 == null) freqControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Amp: " + Math.round(amplitude * 100000.0) / 100000.0 + " (" + Math.round(Math.log(amplitude)/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY);
		if(g2 == null) ampControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("Bins: " + bins, currentX, currentY);
		if(g2 == null) binsControl = new Rectangle(currentX, currentY - fontSize, width, fontSize);
		currentY += yStep;
		if(g2 != null) g2.drawString("IN: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputs.add(parent.addConnector(new Input(this, currentRect)));
		}
		if(g2 != null) g2.setColor(Color.BLUE);
		currentY += yStep;
		if(g2 != null) g2.drawString("OUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputs.add(parent.addConnector(new Output(this, currentRect)));
		}
		//if(g2 == null) height = currentY + 6 - y;
		//if(g2 == null) width = height;
		//System.out.println(width);
	}

	public void loadModuleInfo(BufferedReader in) {
		try { 
			String currentLine = in.readLine();
			this.type = FilterType.valueOf(currentLine);
			currentLine = in.readLine();
			this.freqInHz = new Double(currentLine);
			currentLine = in.readLine();
			this.amplitude = new Double(currentLine);
			currentLine = in.readLine();
			this.bins = new Double(currentLine);
		} catch (Exception e) {
			System.out.println("BasicWaveform.loadModuleInfo: Error reading from file");
		}
		
	}

	public void saveModuleInfo(BufferedWriter out) {
		try { 
			out.write(this.type.toString());
			out.newLine();
			out.write(new Double(freqInHz).toString());
			out.newLine();		
			out.write(new Double(amplitude).toString());
			out.newLine();	
			out.write(new Double(bins).toString());
			out.newLine();	
		} catch (Exception e) {
			System.out.println("FIRFilter.loadModuleInfo: Error reading from file");
		}
		
	}

	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return Module.ModuleType.FIRFILTER;
	}

	public int getX() {
		return cornerX;
	}

	public int getY() {
		return cornerY;
	}
	
	public boolean pointIsInside(int x, int y) {
		Rectangle moduleBounds = new Rectangle(this.cornerX, this.cornerY, width, height);
		return moduleBounds.contains(x, y);
	}
	
	
}
