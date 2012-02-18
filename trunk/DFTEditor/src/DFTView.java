import javax.swing.*;
import java.awt.*;

public class DFTView extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2964004162144513754L;
	
	public enum View {
		Digits1,
		Digits2,
		Pixels1,
		Pixels2;
	}
	
	public static void setView(View v) {
		view = v;
	}
	
	public View getView() {
		return view;
	}
	
	private static View view = View.Pixels1; 
	
	public static int getXStep() {
    	switch(view) {
    	case Digits1:
    		return DFTEditor.xStep;
    	case Digits2:
    		return DFTEditor.xStep;
    	case Pixels1:
    		return 1;
    	case Pixels2:
    		return 2;
    	}
    	return 1;
	}
	
	public static int getYStep() {
    	switch(view) {
    	case Digits1:
    		return DFTEditor.yStep;
    	case Digits2:
    		return DFTEditor.yStep * 2;
    	case Pixels1:
    		return 1;
    	case Pixels2:
    		return 2;
    	}
    	return 1;
	}	
	
	public void DrawUpperTimes(Graphics g) {
		int iTime;
		int digitPlace;
		int digitVal;
		boolean leading0;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		int screenX;
		int screenY;
		int xStep = DFTEditor.xStep + DFTEditor.xStep % getXStep();
		int timeStep = xStep / getXStep();
		int startTime = DFTEditor.leftX;
		int endTime = DFTEditor.leftX + (getWidth() - DFTEditor.leftOffset) / xStep * timeStep;
		for(int time = startTime; time < endTime; time += timeStep) {
			iTime = time * DFTEditor.timeStepInMillis;
			leading0 = true;
			int yOffset = 1;
			for(digitPlace = 1000000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = iTime / digitPlace;
				if((digitVal == 0) && leading0 && (digitPlace != 1)) {
					yOffset++;
					continue;
				}
				leading0 = false;
				iTime -= digitVal * digitPlace;
				if(digitPlace >= 1000) {
					b = new Color(1.0f, 1.0f, 1.0f);
					f = new Color(0.0f, 0.0f, 0.0f);
				} else {
					f = new Color(1.0f, 1.0f, 1.0f);
					b = new Color(0.0f, 0.0f, 0.0f);					
				}
				if(time >= DFTEditor.maxTime) {
					f = blank;
					b = blank;
				}
				screenX = DFTEditor.leftOffset + ((time - startTime) / timeStep) * xStep;
				screenY = yOffset * DFTEditor.topYStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);				
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, screenY, digitVal);
				yOffset++;
			}
		}
	}


	public void DrawLeftFreqs(Graphics g) {
		int iFreq;
		int digitPlace;
		int digitVal;
		Color f;
		Color b;
		Color blank = new Color(0.0f, 0.0f, 0.0f);
		// ADJUST FROM 2 DIGITS TO 1
		int mainYStep = DFTEditor.yStep;
		if(view == View.Digits2) mainYStep *= 2;
		int yStep = mainYStep + mainYStep % getYStep();
		int freqStep = yStep / getYStep();
		int startFreq = DFTEditor.upperY;
		int freqRange = (getHeight() - DFTEditor.upperOffset) / yStep * freqStep;
		int endFreq = startFreq + freqRange;
		for(int freq = startFreq; freq < endFreq; freq += freqStep) {
			iFreq = DFTEditor.maxRealFreq - freq;
			int xOffset = 1;
			double freqsPerOctave = (double) DFTEditor.freqsPerOctave;
			double dFreq = (double) iFreq;
			float freqInHz = (float) Math.pow(2.0, dFreq / freqsPerOctave);			
			for(digitPlace = 10000; digitPlace >= 1; digitPlace /= 10) {
				digitVal = (int) Math.floor(freqInHz / digitPlace);
				freqInHz -= digitVal * digitPlace;
				f = new Color(1.0f, 1.0f, 1.0f);
				b = new Color(0.0f, 0.0f, 0.0f);
				if(freq >= (DFTEditor.maxRealFreq - DFTEditor.minRealFreq)) {
					f = blank;
					b = blank;
				}
				int screenX = xOffset * DFTEditor.xStep;
				int screenY = DFTEditor.upperOffset + ((freq - startFreq) / freqStep) * yStep;
				g.setColor(b);
				g.fillRect(screenX, screenY, 6, 8);
				DFTUtils.SevenSegmentSmall(g, f, b, screenX, 
				                           screenY, 
				                           digitVal);
				xOffset++;
			}
		}
	}
		
	public void DrawFileData(Graphics g, boolean scaleLines) {
		// clear old data
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(DFTEditor.leftOffset, DFTEditor.upperOffset, getWidth(), getHeight());
		DrawLeftFreqs(g);
		DrawUpperTimes(g);		
		if((view != View.Digits1) && (view != View.Digits2)) {
			drawFileDataAsPixels(g);
			return;
		}
		int startTime = DFTEditor.leftX;
		int endTime = startTime + ((getWidth() - DFTEditor.leftOffset) / getXStep());
		int startFreq = DFTEditor.upperY;
		int endFreq = startFreq + ((getHeight() - DFTEditor.upperOffset) / getYStep());
		for(int time = startTime; time < endTime; time++) {
            if(!isXInBounds(time)) break;
            for(int freq = startFreq; freq < endFreq; freq++) {
                if(!isYInBounds(freq)) break;
        		int screenX = DFTEditor.leftOffset + ((time - DFTEditor.leftX) * getXStep());
        		int screenY = DFTEditor.upperOffset + ((freq - DFTEditor.upperY) * getYStep());
                float amp = DFTEditor.getAmplitude(time, freq);
                if(amp > 0.0f) {
                	Color b = getColor(time, freq);
                	drawAmplitude(g, screenX, screenY, amp, b);
                }
			}
		}
        //g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
        //g2.setStroke(new BasicStroke(4));
        //g2.drawLine(100, 400, 1500, 400);
		//DrawMinimaAmdMaximas(g);
	}
	
	public void drawAmplitude(Graphics g, int screenX, int screenY, float currentVal, Color b) {
		int digitVal;
		int fractionVal;
		if(currentVal > 10.0f) {
			digitVal = (int) Math.floor(currentVal);
			digitVal -= 10;					
		} else {
			digitVal = (int) Math.floor(currentVal);
		}
		fractionVal = (int) Math.floor((currentVal - Math.floor(currentVal)) * 10.0f);
		if(view == View.Digits1) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal);
			return;
		}
		if(view == View.Digits2) {
			DFTUtils.DrawSegmentData(g, b, screenX, screenY, digitVal, fractionVal);
			return;
		}
		// Assume we're drawing pixels
		g.setColor(b);
		int pixelStepX = getXStep();
		int pixelStepY = getYStep();
		g.fillRect(screenX, screenY, pixelStepX, pixelStepY);
	}
	
	public void drawFileDataAsPixels(Graphics g) {
		int pixelStepX = getXStep(); //(DFTEditor.xStep / getTimeIncrement());
		int pixelStepY = getYStep(); //(DFTEditor.yStep / getFreqIncrement() / 2);
		int startX = DFTEditor.leftX;
		int endX = startX + ((getWidth() - DFTEditor.leftOffset) / pixelStepX);
		int startY = DFTEditor.upperY;
		int endY = startY + ((getHeight() - DFTEditor.upperOffset) / pixelStepY);
		for(int x = startX; x < endX; x++) {
			if(!isXInBounds(x)) break;
            for(int y = startY; y < endY; y++) {
            	if(!isYInBounds(y)) break;
        		int screenX = DFTEditor.leftOffset + ((x - DFTEditor.leftX) * pixelStepX);
        		int screenY = DFTEditor.upperOffset + ((y - DFTEditor.upperY) * pixelStepY);
        		float amp = DFTEditor.getAmplitude(x, y);
        		if(amp == 0.0f) continue;
        		Color b = getColor(x, y);
                drawAmplitude(g, screenX, screenY, DFTEditor.getAmplitude(x, y), b);
            }
		}
	}
	
	private Color getColor(int time, int freq) {
		float ampRange = DFTEditor.maxAmplitude - DFTEditor.minAmplitude;
		float currentVal = DFTEditor.getAmplitude(time, freq);
		currentVal -= DFTEditor.minAmplitude;
		currentVal /= ampRange;
		if(currentVal < 0.0f) currentVal = 0.0f;
		if(currentVal > 1.0f) currentVal = 1.0f;
		float red;
		float green;
		float blue;
		if(DFTEditor.isMaxima(time, freq)) {
			blue = 1.0f - currentVal / 2.0f;
			red = currentVal / 2.0f + 0.5f;
			green = 0.5f;
		} else {
			blue = 1.0f - currentVal;
			red = currentVal;
			if(red >= 0.5f) {
				green = (1.0f - red) * 2.0f;
			} else {
				green = red * 2.0f;
			}
		}
		return new Color(red, green, blue);
	}
	
	// See also DFTEditor.getAmplitude()
	private boolean isYInBounds(int y) {
		if(y > DFTEditor.maxScreenFreq) return false;
		return true;
	}
	
	// See also DFTEditor.getAmplitude()
	private boolean isXInBounds(int x) {
		if(x > DFTEditor.maxTime) return false;
		return true;
	}
		
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        DrawFileData(g, true);
    }
	
}
