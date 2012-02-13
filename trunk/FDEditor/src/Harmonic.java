import java.util.ArrayList;
import java.util.TreeMap;


public class Harmonic {

	private boolean applyTaper = false;
	private boolean overwrite = false;
	private TreeMap<Integer, FDData> timeToData = new TreeMap<Integer, FDData>();
	
	public Harmonic() {
	}
	
	// returns true if data already exists at that time
	public boolean addData(FDData data) {
		System.out.println(data);
		if(!timeToData.containsKey(data.getTime())) {
			timeToData.put(data.getTime(), data);
			return false;
		}
		// data already exists at that time
		if(overwrite) {
			timeToData.put(data.getTime(), data);
		}
		System.out.println("Harmonic.addData(): Duplicate data at time = " + data.getTime());
		return true;
	}
	
	public boolean containsData() {
		return !timeToData.isEmpty();
	}
	
	public int getStartSampleOffset() {
		if(!containsData()) return 0;
		return (int) Math.round(timeToData.firstKey() * SynthTools.timeToSample);
	}
	
	public int getEndSampleOffset() {
		return getStartSampleOffset() + getLength();
	}
	
	public int getLength() {
		if(!containsData()) return 0;
		int length = (int) Math.round(timeToData.lastKey() * SynthTools.timeToSample);
		length += getTaperLength();
		return length;
	}
	
	public Double[] getPCMData() {
		if(timeToData.size() < 2) {
			System.out.println("Harmonics.getPCMData: number of data points < 2");
			return null;
		}
		ArrayList<Double> sampleTimes = new ArrayList<Double>();
		ArrayList<Double> logAmps = new ArrayList<Double>();
		ArrayList<Double> logFreqs = new ArrayList<Double>();
		ArrayList<Double> PCMData = new ArrayList<Double>();
		double startSample = -1;
		for(int time: timeToData.keySet()) {
			if(startSample == -1) {
				startSample = time;
				sampleTimes.add(0.0);
				logAmps.add(timeToData.get(time).getLogAmplitude());
				logFreqs.add(timeToData.get(time).getNoteComplete() / FDData.noteBase);
				continue;
			}
			sampleTimes.add(time - startSample);
			logAmps.add(timeToData.get(time).getLogAmplitude());
			logFreqs.add(timeToData.get(time).getNoteComplete() / FDData.noteBase);
		}
		// NOTE logAmps.size() = logFreqs.size() = sampleTimes.size()
		// Parallel arrays are fully contained within function so they should be OK
		double endTime = sampleTimes.get(sampleTimes.size() - 1);
		double endLogFreq = logFreqs.get(sampleTimes.size() - 1);
		if(getTaperLength() > 0) {
			// Apply taper to avoid "pop" at end of harmonic
			sampleTimes.add(endTime + getTaperLength());
			logAmps.add(0.0);
			logFreqs.add(endLogFreq);
		}
		Double[] sampleTimesArray = new Double[sampleTimes.size()];
		Double[] logAmpsArray = new Double[sampleTimes.size()];
		Double[] logFreqsArray = new Double[sampleTimes.size()];
		sampleTimesArray = sampleTimes.toArray(sampleTimesArray);
		logAmpsArray = logAmps.toArray(logAmpsArray);
		logFreqsArray = logFreqs.toArray(logFreqsArray);
		LogLinear ampEnvelope = new LogLinear(sampleTimesArray, logAmpsArray);
		LogLinear freqEnvelope = new LogLinear(sampleTimesArray, logFreqsArray);
		double currentPhase = 0.0;
		for(int currentSample = 0; currentSample < ampEnvelope.getNumSamples(); currentSample++) {
			double amplitude = ampEnvelope.getSample(currentSample);
			double frequency = freqEnvelope.getSample(currentSample);
			double deltaPhase = (frequency / SynthTools.sampleRate) * SynthTools.twoPI;
			PCMData.add(amplitude * Math.sin(currentPhase));
			//System.out.println(PCMData.get(PCMData.size() - 1));
			//System.out.println(currentPhase);
			currentPhase += deltaPhase;
		}
		Double[] returnVal = new Double[PCMData.size()];
		returnVal = PCMData.toArray(returnVal);
		return returnVal;
	}
	
	public int getTaperLength() {
		FDData endData = timeToData.lastEntry().getValue();
		double endLogAmp = endData.getLogAmplitude();
		if(endLogAmp <= 0.0) return 0;
		double endLogFreq = endData.getNoteComplete() / FDData.noteBase;
		double cycleLength = SynthTools.sampleRate / Math.pow(FDData.logBase, endLogFreq);
		return (int) Math.ceil(endLogAmp * cycleLength);
	}

	// this is here so there's no warning
	public boolean getApplyTaper() {
		return applyTaper;	
	}

	public void setApplyTaper(boolean applyTaper) {
		this.applyTaper = applyTaper;	
	}
	
	public String toString() {
		StringBuffer out = new StringBuffer();
		if(timeToData.isEmpty()) return "\n\nEMPTY\n\n";
		out.append("\n\nSTART" + timeToData.firstKey() + "\n");
		for(FDData data: timeToData.values()) {
			out.append(" [" + data.getTime() + " " + data.getNote() + " " + data.getLogAmplitude() + "]\n");
		}
		out.append("END" + timeToData.lastKey() + "\n\n");
		return out.toString();
	}
	
}
