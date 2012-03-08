import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class FDView extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis;
	
	public static double getXStep() {
    	return 4;
	}
	
	public static double getYStep() {
		return 4;
	}
	
	public void drawUpperTimes(Graphics g) {
		int intDigits = 3;
		int decimalStartY = intDigits * FDEditor.yStep;
		int endTime = (int) Math.round(FDEditor.leftX + getWidth() / getXStep());
		double timeStep = (double) FDEditor.xStep / (double) getXStep();
		int screenX = FDEditor.leftOffset;
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for(double dTime = FDEditor.leftX; dTime <= endTime; dTime += timeStep) {
			int time = (int) Math.round(dTime);
			if(time >= FDEditor.maxTime) return;
			int millis = time * FDData.timeStepInMillis;
			int intVal = millis / 1000;
			int decimalVal = millis - intVal * 1000;
			FDUtils.DrawIntegerVertical(g, white, black, screenX, 0, 3, intVal);
			FDUtils.DrawIntegerVertical(g, black, white, screenX, decimalStartY, 3, decimalVal);
			screenX += FDEditor.xStep;
			//System.out.println("drawUpperTimes " + intVal + " " + decimalVal);
		}
	}

	public void drawLeftFreqs(Graphics g) {
		double dFreqStep = (double) FDEditor.yStep / (double) getYStep();
		int deltaScreenY = FDEditor.yStep;
		int screenY = FDEditor.upperOffset;
		int endFreq = (int) Math.round(FDEditor.upperY + getHeight() / getYStep());
		// handle digits > 1
		if(FDEditor.yStep < getYStep()) {
			dFreqStep = 1.0;
			deltaScreenY = (int) getYStep();
		}
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for(double dFreq = FDEditor.upperY; dFreq < endFreq; dFreq += dFreqStep) {
			int freq = (int) Math.round(dFreq);
			if(freq >= FDEditor.maxNote - FDEditor.minNote) return;
			int note = FDEditor.freqToNote(freq);
			int freqInHz = (int) Math.round(Math.pow(2.0, note / FDData.noteBase));
			FDUtils.DrawIntegerHorizontal(g, white, black, 0, screenY, 5, freqInHz);
			screenY += deltaScreenY;
			//System.out.println("drawUpperTimes " + intVal + " " + decimalVal);
		}
	}
	
	public void drawFileData(Graphics g) {
		drawLeftFreqs(g);
		drawUpperTimes(g);
		int timeStep = 1;
		int startTime = FDEditor.leftX;
		int endTime = (int) Math.round(startTime + ((getWidth() - FDEditor.leftOffset) * timeStep));
		int startFreq = FDEditor.upperY;
		int endFreq = (int) Math.round(startFreq + ((getHeight() - FDEditor.upperOffset) / getYStep()));
		for(int time = 0; time < 1000; time++) {
            //if(!isXInBounds(time)) break;
            for(int freq = 0; freq < 200; freq++) {
                //if(!isYInBounds(freq)) break;
        		FDData data = FDEditor.getSelected(time, FDEditor.freqToNote(freq)); //FDUtils.getMaxDataInTimeRange(time, time + timeStep, FDEditor.freqToNote(freq));
        		if(data == null) continue;
        		float logAmplitude = (float) data.getLogAmplitude();
        		Color b = getColor(logAmplitude);
        		g.setColor(b);
        		int screenX = FDEditor.leftOffset + (time - startTime) / timeStep;
        		int screenY = (int) Math.round(FDEditor.upperOffset + (freq - startFreq) * getYStep());
        		//System.out.println(screenX + " " + screenY + " " + logAmplitude);
        		//drawAmplitude(g, screenX, screenY, logAmplitude, b);
        		g.drawRect(screenX, screenY, 2, 2);
            }
		}	
	}
	

	private Color getColor(double logAmplitude) {
		float ampRange = (float) (FDEditor.getMaxAmplitude() - FDEditor.getMinAmplitude());
		float currentVal = (float) logAmplitude;
		currentVal -= FDEditor.getMinAmplitude();
		currentVal /= ampRange;
		if(currentVal < 0.0f) currentVal = 0.0f;
		if(currentVal > 1.0f) currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if(red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		//return new Color(1.0f, 1.0f, 1.0f, 0.75f);
		return new Color(red, green, blue, 0.75f);
	}
	
	public void drawPlayTime(int offsetInMillis, int refreshInMillis) {
		drawPlaying = true;
		FDView.offsetInMillis = offsetInMillis;
	}
	
	public int getTimeAxisWidthInMillis() {
   		double millisPerPixel = (double) FDData.timeStepInMillis / (double) getXStep();
   		return (int) Math.round(getWidth() * millisPerPixel);
	}
	
	// See also FDEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		if(y > FDEditor.maxNote - FDEditor.minNote) return false;
		return true;
	}
	
	// See also FDEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		if(x > FDEditor.maxTime) return false;
		return true;
	}
		
    protected void paintComponent(Graphics g) {
    	if(drawPlaying) {
    		double millisPerPixel = (double) FDData.timeStepInMillis / (double) getXStep();
    		int startX = (int) Math.round((double) FDView.offsetInMillis / millisPerPixel + FDEditor.leftOffset);
    		g.drawImage(bi, 0, 0, null);
       		g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.75f));
    		g.fillRect(startX, 0, 1, getHeight());    		
    		drawPlaying = false;
    		return;
    	}
    	if(useImage == true) {
    		bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    		Graphics2D g2 = bi.createGraphics();
    		super.paintComponent(g);
    		drawFileData(g2);
    		g.drawImage(bi, 0, 0, null);
    		return;
    	}
		super.paintComponent(g);
		drawFileData(g);
		return;
    	
    }
	
}
