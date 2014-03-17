
package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.BasicWaveformEditor.ControlRect;

public class SpectrumView extends JPanel {

	private static final long serialVersionUID = 9057228507254113149L;
	
	Spectrum parent;
	
	public SpectrumView(Spectrum parent) {
		this.parent = parent;
	}
	
	protected Color timeToColor(double time) {
		float currentVal = (float) time / (float) (parent.maxTime - parent.minTime);
		if (currentVal < 0.0f)
			currentVal = 0.0f;
		if (currentVal > 1.0f)
			currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if (red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		return new Color(red, green, blue);
	}
	
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if(parent.minTime == parent.maxTime || parent.minFreq == parent.maxFreq) return;
		for(double freq: parent.freqToTimeToAmplitude.keySet()) {
			for(double time: parent.freqToTimeToAmplitude.get(freq).keySet()) {
				g2.setColor(timeToColor(time));
				int x = parent.freqToX(freq);
				int y = parent.amplitudeToY(parent.freqToTimeToAmplitude.get(freq).get(time));
				g2.drawRect(x, y, 1, 1);
			}
		}
		drawGrid(g2);
	}
	
	private void drawGrid(Graphics g2) {
		g2.setColor(new Color(0.5f, 0.5f, 0.5f));
		for(double freq = Math.ceil(parent.minFreq); freq < parent.maxFreq; freq += 1.0) {
			g2.drawLine(parent.freqToX(freq), getHeight() - parent.yPadding, parent.freqToX(freq), 0);
			g2.drawString(Math.round(Math.pow(2.0, freq)) + "", parent.freqToX(freq), getHeight() - 2);
		}
		for(double amplitude = Math.ceil(parent.minAmplitude); amplitude < parent.maxAmplitude; amplitude += 1.0) {
			g2.drawLine(parent.xPadding, parent.amplitudeToY(amplitude), getWidth(),  parent.amplitudeToY(amplitude));
			g2.drawString(Math.round(Math.pow(2.0, amplitude)) + "", 0, parent.amplitudeToY(amplitude));
		}
	}

}
	