import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

class SynthTools {
	
	static double sampleRate = 44100.0;
	static double twoPI = 2.0 * Math.PI;
	static double timeToSample = sampleRate * (FDData.timeStepInMillis * 1.0 / 1000.0);
	static float[] PCMDataMono = null;
	static float[] PCMDataLeft = null;
	static float[] PCMDataRight = null;
	static float[] WAVDataMono = null;
	static float[] WAVDataLeft = null;
	static float[] WAVDataRight = null;	
	static int deltaHarmonic = 1;
	static DFTEditor parent;
	public static boolean refresh = true;
	public static boolean flatHarmonics = false;
	static double maxDeltaFreq = 1;

	static void createPCMDataLinear() {
		if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) return;
		PCMDataMono = (float[]) FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicMono.values()));
		PCMDataLeft = (float[])FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicLeft.values()));
		PCMDataRight = (float[])FastSynth.synthHarmonicsLinear(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicRight.values()));
	}
	
	static void createPCMDataLinearCubicSpline() {
		if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) return;
		PCMDataMono = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicMono.values()));
		PCMDataLeft = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicLeft.values()));
		PCMDataRight = FastSynth.synthHarmonicsLinearCubicSpline(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicRight.values()));
	}
	
	static void createPCMDataLinearNoise() {
		if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) return;
		PCMDataMono = FastSynth.synthHarmonicsLinearNoise(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicMono.values()));
		PCMDataLeft = FastSynth.synthHarmonicsLinearNoise(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicLeft.values()));
		PCMDataRight = FastSynth.synthHarmonicsLinearNoise(new ArrayList<Harmonic>(DFTEditor.harmonicIDToHarmonicRight.values()));
	}
	
	static void playPCMData() {
		AudioPlayer ap = new AudioPlayer(PCMDataMono, 1.0);
		if(DFTEditor.currentChannel == DFTEditor.Channel.STEREO) {
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.LEFT_RIGHT) {
				ap = new AudioPlayer(PCMDataLeft, PCMDataRight, 1.0);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV_RIGHT) {
				ap = new AudioPlayer(WAVDataLeft, PCMDataRight, 1.0);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.LEFT_WAV) {
				ap = new AudioPlayer(PCMDataLeft, WAVDataRight, 1.0);
			}
			if(DFTEditor.currentChannelMixer == DFTEditor.ChannelMixer.WAV) {
				ap = new AudioPlayer(WAVDataLeft, WAVDataRight, 1.0);
			}
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.LEFT) {
			ap = new AudioPlayer(PCMDataLeft, 1.0);
		}
		if(DFTEditor.currentChannel == DFTEditor.Channel.RIGHT) {
			ap = new AudioPlayer(PCMDataRight, 1.0);
		}
		ap.start();
		//ap = new AudioPlayer(parent, PCMDataRight, 1.0);
		//ap.start();
	}
	
	static void createHarmonics() {
		createHarmonicsChannel(0);
		createHarmonicsChannel(1);
		createHarmonicsChannel(2);
	}

	static void createHarmonicsChannel(int channel) {
		TreeMap<Integer, TreeMap<Integer, FDData>> timeToFreqToData = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		if(channel == 0) DFTEditor.harmonicIDToHarmonicMono = new TreeMap<Long, Harmonic>();
		if(channel == 1) DFTEditor.harmonicIDToHarmonicLeft = new TreeMap<Long, Harmonic>();
		if(channel == 2) DFTEditor.harmonicIDToHarmonicRight = new TreeMap<Long, Harmonic>();
		if(channel == 0) DFTEditor.timeToFreqsAtMaximaMono = new TreeMap<Integer, TreeSet<Integer>>();
		if(channel == 1) DFTEditor.timeToFreqsAtMaximaLeft = new TreeMap<Integer, TreeSet<Integer>>();		
		if(channel == 2) DFTEditor.timeToFreqsAtMaximaRight = new TreeMap<Integer, TreeSet<Integer>>();
		float[][] amplitudes = null;
		if(channel == 0) amplitudes = DFTEditor.amplitudesMono;
		if(channel == 1) amplitudes = DFTEditor.amplitudesLeft;
		if(channel == 2) amplitudes = DFTEditor.amplitudesRight;
		int numTimes = amplitudes.length;
		int numFreqs = amplitudes[0].length;
		int time = 0;
		int freq = 0;
		for(time = 0; time < numTimes; time++) {
			timeToFreqToData.put(time, new TreeMap<Integer, FDData>());
			if(channel == 0) DFTEditor.timeToFreqsAtMaximaMono.put(time, new TreeSet<Integer>());
			if(channel == 1) DFTEditor.timeToFreqsAtMaximaLeft.put(time, new TreeSet<Integer>());
			if(channel == 2) DFTEditor.timeToFreqsAtMaximaRight.put(time, new TreeSet<Integer>());
			for(freq = 1; freq < numFreqs - 1; freq++) {
				if(amplitudes[time][freq] >= amplitudes[time][freq - 1]) {
					if(amplitudes[time][freq] >= amplitudes[time][freq + 1]) {
						if(amplitudes[time][freq] <= 0) continue;
						FDData data = null;
						try {
							data = new FDData(time, DFTEditor.freqToNote(freq), amplitudes[time][freq], 1L);
						} catch (Exception e) {
							System.out.println("SynthTools.createHarmonics: Error creating data time: " + time + " freq: " + freq);
						}
						timeToFreqToData.get(time).put(freq, data);
						if(channel == 0) DFTEditor.timeToFreqsAtMaximaMono.get(time).add(freq);
						if(channel == 1) DFTEditor.timeToFreqsAtMaximaLeft.get(time).add(freq);
						if(channel == 2) DFTEditor.timeToFreqsAtMaximaRight.get(time).add(freq);
						//System.out.println(data);
					}
				}
			}
		}
		for(time = 0; time < numTimes; time++) {
			TreeMap<Integer, FDData> outerFreqToData = timeToFreqToData.get(time);
			while(!outerFreqToData.isEmpty()) {
				long harmonicID = DFTEditor.getRandomID();
				Harmonic newHarmonic = new Harmonic(harmonicID);
				freq = outerFreqToData.firstKey();
				FDData currentData = outerFreqToData.get(freq);
				outerFreqToData.remove(freq);
				currentData.setHarmonicID(harmonicID);
				newHarmonic.addData(currentData);
				int startFreq = freq;
				for(int innerTime = time + 1; innerTime < numTimes; innerTime++) {
					TreeMap<Integer, FDData> innerFreqToData = timeToFreqToData.get(innerTime);
					if(innerFreqToData.containsKey(freq)) {
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					if(flatHarmonics) break;
					if(innerFreqToData.containsKey(freq - 1)) {
						if(innerFreqToData.containsKey(freq + 1)) {
							FDData upperData = innerFreqToData.get(freq - 1);
							FDData lowerData = innerFreqToData.get(freq + 1);
							if(upperData.getLogAmplitude() > lowerData.getLogAmplitude()) {
								freq -= 1;
								FDData innerData = innerFreqToData.get(freq);
								innerFreqToData.remove(freq);
								innerData.setHarmonicID(harmonicID);
								newHarmonic.addData(innerData);
								//System.out.println("0:" + harmonicID + " " + innerData);
								if(freq < startFreq - maxDeltaFreq) break;
							} else {
								freq += 1;
								FDData innerData = innerFreqToData.get(freq);
								innerFreqToData.remove(freq);
								innerData.setHarmonicID(harmonicID);
								newHarmonic.addData(innerData);
								if(freq > startFreq + maxDeltaFreq) break;
								//System.out.println("0:" + harmonicID + " " + innerData);
								continue;
							}
						} else {
							freq -= 1;
							FDData innerData = innerFreqToData.get(freq);
							innerFreqToData.remove(freq);
							innerData.setHarmonicID(harmonicID);
							newHarmonic.addData(innerData);
							//System.out.println("0:" + harmonicID + " " + innerData);
							if(freq < startFreq - maxDeltaFreq) break;
						}
						continue;
					}
					if(innerFreqToData.containsKey(freq + 1)) {
						freq += 1;
						FDData innerData = innerFreqToData.get(freq);
						innerFreqToData.remove(freq);
						innerData.setHarmonicID(harmonicID);
						newHarmonic.addData(innerData);
						if(freq > startFreq + maxDeltaFreq) break;
						//System.out.println("0:" + harmonicID + " " + innerData);
						continue;
					}
					break;
				}
				if(channel == 0) DFTEditor.harmonicIDToHarmonicMono.put(harmonicID, newHarmonic);
				if(channel == 1) DFTEditor.harmonicIDToHarmonicLeft.put(harmonicID, newHarmonic);
				if(channel == 2) DFTEditor.harmonicIDToHarmonicRight.put(harmonicID, newHarmonic);
			}
		}
	}
	
	static void printHarmonics(ArrayList<Harmonic> harmonics) {
		for(Harmonic harmonic: harmonics) {
			System.out.print(harmonic);
		}
	}
	
	static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e){
			System.out.println(e);
		}

	}

	static TreeMap<Integer, TreeMap<Integer, FDData>> copyTreeMap(TreeMap<Integer, TreeMap<Integer, FDData>> input) {
		TreeMap<Integer, TreeMap<Integer, FDData>> output = new TreeMap<Integer, TreeMap<Integer, FDData>>();
		for(Integer time: input.keySet()) {
			output.put(time, new TreeMap<Integer, FDData>());
			for(Integer note: input.get(time).keySet()) {
				FDData data = input.get(time).get(note);
				output.get(time).put(note, data);
			}
		}
		return output;
	}

}
