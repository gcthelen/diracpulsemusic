import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static double[] PCMData = null;
	static GraphEditor parent;

	static void createPCMData(GraphEditor parent) {
		//PCMData = FastSynth.synthHarmonics(new ArrayList<Harmonic>(parent.harmonicIDToHarmonic.values()));
		if(GraphEditor.harmonicIDToHarmonic == null || GraphEditor.harmonicIDToHarmonic.isEmpty()) return;
		PCMData = GraphFileOutput.SynthFDDataExternally(
				new ArrayList<Harmonic>(GraphEditor.harmonicIDToHarmonic.values()));
	}
	
	static void createPCMData(GraphEditor parent, int startTime, int endTime) {
		int startSampleOffset = (int) Math.round(startTime * timeToSample);
		int endSampleOffset = (int) Math.round(endTime * timeToSample);
		int dataLength = endSampleOffset - startSampleOffset;
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			if(harmonic.getStartSampleOffset() >= endSampleOffset) continue;
			if(harmonic.getEndSampleOffset() <= startSampleOffset) continue;
			if(harmonic.getStartSampleOffset() < startSampleOffset) {
				startSampleOffset = harmonic.getStartSampleOffset();
			}
			if(harmonic.getEndSampleOffset() > endSampleOffset) {
				endSampleOffset = harmonic.getEndSampleOffset();
			}
			if(harmonic.getStartSampleOffset() < endSampleOffset) continue;
		}
		PCMData = new double[endSampleOffset + 1];
		System.out.println("End Sample Offset " + endSampleOffset);
		for(int i= 0; i <= endSampleOffset; i++) PCMData[i] = 0.0;
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			Double[] HarmonicPCMData = harmonic.getPCMData();
			int startSample = harmonic.getStartSampleOffset();
			int endSample = startSample + HarmonicPCMData.length - 1;
			//System.out.println("SynthTools.playFileData startSample = " + startSample);
			//System.out.println("SynthTools.playFileData endSample = " + endSample);
			//System.out.println("SynthTools.playFileData HarmonicPCMData.length = " + HarmonicPCMData.length);
			for(int currentSample = startSample; currentSample < endSample - 1; currentSample++) {
				if(currentSample >= PCMData.length) break;
				PCMData[currentSample] += HarmonicPCMData[currentSample - startSample];
				if(currentSample % 441 == 0) {
					//System.out.println(currentSample + ":" + PCMData[currentSample]);
				}
			}
		}
		for(int i= 0; i <= endSampleOffset; i += 100) {
			//System.out.println(i + "!" + PCMData[i]);
		}
		//JOptionPane.showMessageDialog(parent, "Ready To Play");
	}
	
	public static void playWindow() {
		if(PCMData == null) return;
		AudioPlayer ap = new AudioPlayer(PCMData, 1.0);
		ap.start();
	}


}