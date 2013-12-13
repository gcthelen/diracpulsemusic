
package main.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import main.FDData;
import main.Module;
import main.ModuleEditor;
import main.TestSignals.Generator;
import main.TestSignals.TAPair;
import main.SynthTools;

public class StereoPan implements Module {
	
	ModuleEditor parent = null;
	Integer moduleID = null;
	int width = 150;
	int height = 150; // calculated by init
	int cornerX;
	int cornerY;
	String name = "Stereo Pan";
	
	ArrayList<Input> inputs;
	ArrayList<Input> controls;
	ArrayList<OutputLeft> outputsLeft;
	ArrayList<OutputRight> outputsRight;
	
	private class Input extends Module.Input {

		public Input(Module parent, Rectangle selectArea, Integer connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	private class OutputLeft extends Module.Output {

		private double[] calculatedSamples = null;
		
		public OutputLeft(Module parent, Rectangle selectArea, Integer connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double[] getSamples(HashSet<Integer> waitingForModuleIDs) {
			if(calculatedSamples != null) return calculatedSamples;
			calculatedSamples = getSamplesLeft(waitingForModuleIDs);
			return calculatedSamples;
		}
		
		public void clearSamples() {
			calculatedSamples = null;
		}
	}
	
	private class OutputRight extends Module.Output {
		
		private double[] calculatedSamples = null;


		public OutputRight(Module parent, Rectangle selectArea, Integer connectionID) {
			super(parent, selectArea, connectionID);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double[] getSamples(HashSet<Integer> waitingForModuleIDs) {
			if(calculatedSamples != null) return calculatedSamples;
			calculatedSamples = getSamplesRight(waitingForModuleIDs);
			return calculatedSamples;
		}
		
		public void clearSamples() {
			calculatedSamples = null;
		}
	}
	
	public StereoPan(ModuleEditor parent, int x, int y) {
		this.cornerX = x;
		this.cornerY = y;
		this.moduleID = ModuleEditor.getNextModuleID();
		this.parent = parent;
		inputs = new ArrayList<Input>();
		controls = new ArrayList<Input>();
		outputsLeft = new ArrayList<OutputLeft>();
		outputsRight = new ArrayList<OutputRight>();
		init();
		for(Input input: inputs) {
			parent.addInput(input);
		}
		for(Input input: controls) {
			parent.addInput(input);
		}
		for(Output output: outputsLeft) {
			parent.addOutput(output);
		}
		for(Output output: outputsRight) {
			parent.addOutput(output);
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Integer getModuleId() {
		return moduleID;
	}
	
	public double[] getSamplesLeft(HashSet<Integer> waitingForModuleIDs) {
		double[] inputArray = getInputSum(inputs, waitingForModuleIDs);
		double[] controlsArray = getInputSum(controls, waitingForModuleIDs);
		double[] returnVal = new double[inputArray.length];
		for(int index = 0; index < inputArray.length; index++) {
			if(index >= controlsArray.length) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] <= 0.0) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] >= 1.0) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = (1.0 - controlsArray[index]) * inputArray[index];
		}
		return returnVal;
	}
	
	public double[] getSamplesRight(HashSet<Integer> waitingForModuleIDs) {
		double[] inputArray = getInputSum(inputs, waitingForModuleIDs);
		double[] controlsArray = getInputSum(controls, waitingForModuleIDs);
		double[] returnVal = new double[inputArray.length];
		for(int index = 0; index < inputArray.length; index++) {
			if(index >= controlsArray.length) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] >= 0.0) {
				returnVal[index] = inputArray[index];
				continue;
			}
			if(controlsArray[index] <= -1.0) {
				returnVal[index] = 0.0;
				continue;
			}
			returnVal[index] = (controlsArray[index] + 1.0) * inputArray[index];
		}
		return returnVal;
	}
	
	public double[] getInputSum(ArrayList<Input> inputs, HashSet<Integer> waitingForModuleIDs) {
		if(waitingForModuleIDs == null) waitingForModuleIDs = new HashSet<Integer>();
		if(waitingForModuleIDs.contains(moduleID)) {
			JOptionPane.showMessageDialog((JFrame) parent, "Infinite Loop");
			return new double[0];
		}
		int numSamples = 0;
		double[] returnVal = new double[0];
		ArrayList<double[]> inputsArray = new ArrayList<double[]>();
		for(Input input: inputs) {
			if(input.getConnection() == null) continue;
			waitingForModuleIDs.add(moduleID);
			Output output = (Output) parent.connectorIDToConnector.get(input.getConnection());
			inputsArray.add(output.getSamples(waitingForModuleIDs));
		}
		if(inputsArray.isEmpty()) return returnVal;
		for(double[] inputsLeftIn: inputsArray) {
			if(inputsLeftIn.length > numSamples) numSamples = inputsLeftIn.length;
		}
		returnVal = new double[numSamples];
		for(int index = 0; index < returnVal.length; index++) returnVal[index] = 0.0;
		for(double[] inputsIn: inputsArray) {
			for(int index = 0; index < inputsIn.length; index++) {
				returnVal[index] += inputsIn[index];
			}
		}
		return returnVal;
	}
	
	public void mousePressed(int x, int y) {
		int index = 0;
		for(Output output: outputsLeft) {
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(output.getConnectorID());
				System.out.println(name + " " + "outputsLeft: " + index);
			}
			index++;
		}
		index = 0;
		for(Output output: outputsRight) {
			if(output.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(output.getConnectorID());
				System.out.println(name + " outputsRight: " + index);
			}
			index++;
		}
		index = 0;
		for(Input inputVal: inputs) {
			if(inputVal.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(inputVal.getConnectorID());
				System.out.println(name + " inputs: " + index);
			}
			index++;
		}
		index = 0;
		for(Input control: controls) {
			if(control.getSelectArea().contains(x, y)) {
				parent.handleConnectorSelect(control.getConnectorID());
				System.out.println(name + " controls: " + index);
			}
			index++;
		}
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
		if(g2 != null) g2.drawString(name, currentX, currentY);
		if(g2 != null) g2.setColor(Color.BLACK);
		currentY += yStep;
		if(g2 != null) g2.drawString("LEFT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputsLeft.add(new OutputLeft(this, currentRect, ModuleEditor.getNextConnectorID()));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.RED);		
		if(g2 != null) g2.drawString("RIGHT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) outputsRight.add(new OutputRight(this, currentRect, ModuleEditor.getNextConnectorID()));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.GREEN);		
		if(g2 != null) g2.drawString("CNTRL: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) controls.add(new Input(this, currentRect, ModuleEditor.getNextConnectorID()));
		}
		currentY += yStep;
		if(g2 != null) g2.setColor(Color.YELLOW);		
		if(g2 != null) g2.drawString("INPUT: ", currentX, currentY);
		for(int xOffset = currentX + yStep * 3; xOffset < currentX + width + fontSize - fontSize * 2; xOffset += fontSize * 2) {
			Rectangle currentRect = new Rectangle(xOffset, currentY - fontSize, fontSize, fontSize);
			if(g2 != null) g2.fillRect(currentRect.x, currentRect.y, currentRect.width, currentRect.height);
			if(g2 == null) inputs.add(new Input(this, currentRect, ModuleEditor.getNextConnectorID()));
		}
		//if(g2 == null) height = currentY + 6 - y;
	}

	@Override
	public void loadModuleInfo(BufferedReader in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ModuleType getModuleType() {
		return Module.ModuleType.STEREOPAN;
	}
	
	@Override
	public int getX() {
		return cornerX;
	}

	@Override
	public int getY() {
		return cornerY;
	}

	@Override
	public boolean pointIsInside(int x, int y) {
		Rectangle moduleBounds = new Rectangle(this.cornerX, this.cornerY, width, height);
		return moduleBounds.contains(x, y);
	}
}
