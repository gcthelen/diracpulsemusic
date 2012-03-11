import java.awt.*;

public class DFTUtils {
	
	public static FDData getMaxDataInTimeRange(int startTime, int endTime, int freq) {
		FDData returnVal = DFTEditor.getSelected(startTime, freq);
		for(int time = startTime + 1; time < endTime; time++) {
			FDData currentVal =  DFTEditor.getSelected(time, freq);
			if(currentVal == null) continue;
			if(returnVal == null) {
				returnVal = currentVal;
				continue;
			}
			if(currentVal.getLogAmplitude() > returnVal.getLogAmplitude()) {
				returnVal = currentVal;
			}
		}
		return returnVal;
	}
	
	public static int getConsonantOvertonesBase31(int index) {
		int[] first3 = {31, 18, 13};
		int[] cycle4 = {10, 8, 7, 6};
		int returnVal = 0;
		for(int testIndex = 0; testIndex < 3; testIndex++) {
			if(testIndex == index) return returnVal;
			returnVal += first3[testIndex];
		}
		for(int testIndex = 0; testIndex < 12 * 4; testIndex++) {
			if(testIndex + 3 == index) return returnVal;
			returnVal += cycle4[testIndex % 4];
		}
		System.out.println("DFTUtils.getConsonantOvertonesBase31 index exceeded");
		return returnVal;
	}
	
	public static void testGetConsonantOvertonesBase31() {
		for(int index = 0; index < 13 * 4; index++) {
			int harmonic = getConsonantOvertonesBase31(index);
			if(harmonic == -1) break;
			System.out.println(Math.pow(2.0, harmonic / 31.0));
		}
	}
	
	// returns -1 if screenX is LEFT of data area
	public static int screenXToTime(int screenX) {
		if(screenX < DFTEditor.leftOffset) return -1;
		screenX -= DFTEditor.leftOffset;
		int time = (int) Math.round(screenX / DFTView.getXStep() + DFTEditor.leftX);
		return time;
	}
	
	// returns -1 if screenY is ABOVE data area
	public static int screenYToFreq(int screenY) {
		if(screenY < DFTEditor.upperOffset) return -1;
		screenY -= DFTEditor.upperOffset;
		int freq = (int) Math.round(screenY / DFTView.getYStep() + DFTEditor.upperY);
		return freq;
	}
	
	// returns -1 if screenX is LEFT of data area
	public static int timeToScreenX(int time) {
		if(time < DFTEditor.leftX) return -1;
		int screenX = (int) Math.round(DFTEditor.leftOffset + ((time - DFTEditor.leftX) * DFTView.getXStep()));
		return screenX;
	}
	
	// returns -1 if screenY is ABOVE data area
	public static int freqToScreenY(int freq) {
		if(freq < DFTEditor.upperY) return -1;
		int screenY = (int) Math.round(DFTEditor.upperOffset + ((freq - DFTEditor.upperY) * DFTView.getYStep()));
		return screenY;
	}

	public static FDData getValue(int time, int freq) {
		FDData returnVal = null;
		if (time > DFTEditor.maxTime) time = DFTEditor.maxTime;
		if (freq > DFTEditor.maxScreenFreq) freq = DFTEditor.maxScreenFreq;
		if (time < 0) time = 0;
		if (freq < 0) freq = 0;		
		try {
			int note = DFTEditor.freqToNote(freq);
			returnVal = new FDData(time, note, DFTEditor.getAmplitude(time, freq));
		} catch (Exception e) {
			System.out.println("DFTUtils.getValue: Error creating FDData");
		}
		return returnVal;
	}

	// b = background color, f = foreground color. numdigits needed for space ahead of value 
	public static void DrawIntegerHorizontal(Graphics g, Color b, Color f, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			DFTUtils.SevenSegmentSmall(g, f, screenX, 
			                           screenY, 
			                           digitVal);
			screenX += DFTEditor.xStep;
		}
	}
	
	public static void DrawIntegerVertical(Graphics g, Color b, Color f, int screenX, int screenY, int numdigits, int value) {
		for(int digitPlace = (int) Math.round(Math.pow(10, numdigits)); digitPlace >= 1; digitPlace /= 10) {
			int digitVal = (int) Math.floor(value / digitPlace);
			value -= digitVal * digitPlace;
			g.setColor(b);
			g.fillRect(screenX, screenY, 6, 8);
			DFTUtils.SevenSegmentSmall(g, f, screenX, 
			                           screenY, 
			                           digitVal);
			screenY += DFTEditor.yStep;
		}
	}
	
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		// int lowerScreenY = screenY + topYStep;				
		g.setColor(b);
		g.fillRect(screenX, screenY, (int) DFTView.getXStep(), (int) DFTView.getYStep());;
		SevenSegmentSmall(g, black, screenX, screenY, digitVal);
		// SevenSegmentSmall(g, black, b, screenX, lowerScreenY, fractionVal);
		// g.setColor(black);
		// int bottomScreenY = screenY + yStep - 1;
		// g.drawLine(screenX, bottomScreenY, screenX + xStep, bottomScreenY);
	}
	
	//This takes two vertical lines, digitVal is above fraction val 
	public static void DrawSegmentData(Graphics g, Color b, int screenX, int screenY, int digitVal, int fractionVal) {
		Color black = new Color(0.0f, 0.0f, 0.0f);
		int lowerScreenY = screenY + DFTEditor.yStep;
		g.setColor(b);
		g.fillRect(screenX, screenY, (int) DFTView.getXStep(), (int) DFTView.getYStep());;
		SevenSegmentSmall(g, black, screenX, screenY, digitVal);
		SevenSegmentSmall(g, black, screenX, lowerScreenY, fractionVal);
		// g.setColor(black);
		// int bottomScreenY = screenY + yStep - 1;
		// g.drawLine(screenX, bottomScreenY, screenX + xStep, bottomScreenY);
	}
	
		// (x, y) = (left, top)
	// Segment numbering (0 for OFF, otherwise ON):
	//  0
	// 6 1
	//  2
	// 5 3
	//  4
	// Color f = forground (segment) color
	// Color g = background (segment block) color
	// int digitVal = digit to display (0 - 9)
	public static void SevenSegmentSmall(Graphics g, Color f, int x, int y, int digitVal) {
		int[] segments;
		int[][] digits = {	{1, 1, 0, 1, 1, 1, 1},
							{0, 1, 0, 1, 0, 0, 0},
							{1, 1, 1, 0, 1, 1, 0},
							{1, 1, 1, 1, 1, 0, 0},
							{0, 1, 1, 1, 0, 0, 1},
							{1, 0, 1, 1, 1, 0, 1},
							{1, 0, 1, 1, 1, 1, 1},
							{1, 1, 0, 1, 0, 0, 0},
							{1, 1, 1, 1, 1, 1, 1},
							{1, 1, 1, 1, 1, 0, 1}
						 };
		int[] errDigit = 	{1, 0, 1, 0, 1, 1, 1};
		if((digitVal >= 0) && (digitVal < 10)) {
			segments = digits[digitVal];
		} else {
			segments = errDigit;
		}
		// draw background
		// g.setColor(b);
		// g.fillRect(x, y, 6, 8);
		g.setColor(f);
		// top segment
		if(segments[0] != 0) g.drawLine((x + 2), (y + 1), (x + 4), (y + 1));
		// middle segment
		if(segments[2] != 0) g.drawLine((x + 2), (y + 4), (x + 4), (y + 4));
		// bottom segment
		if(segments[4] != 0) g.drawLine((x + 2), (y + 7), (x + 4), (y + 7));
		// upper right segment
		if(segments[1] != 0) g.drawLine((x + 5), (y + 2), (x + 5), (y + 3));
		// lower right segment
		if(segments[3] != 0) g.drawLine((x + 5), (y + 5), (x + 5), (y + 6));
		// lower left segment
		if(segments[5] != 0) g.drawLine((x + 1), (y + 5), (x + 1), (y + 6));
		// upper left segment
		if(segments[6] != 0) g.drawLine((x + 1), (y + 2), (x + 1), (y + 3));
	}
	
}