package be.tarsos.dsp.rubberband;

import java.io.IOException;

import com.breakfastquay.rubberband.RubberBandStretcher;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.util.FileUtils;

public class RubberBandAudioProcessor implements AudioProcessor {
	
		static{
			try {
				FileUtils.loadLibrary();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private final RubberBandStretcher rbs;

		public RubberBandAudioProcessor(int sampleRate,double initialTimeRatio, double initialPitchScale){
			int options = RubberBandStretcher.OptionProcessRealTime | RubberBandStretcher.OptionWindowShort | RubberBandStretcher.OptionPitchHighQuality;
			rbs = new RubberBandStretcher(sampleRate, 1, options, initialTimeRatio, initialPitchScale);	
		}		
		
		@Override
		public boolean process(AudioEvent audioEvent) {
			float[][] input = {audioEvent.getFloatBuffer()};
			rbs.process(input, false);
			
			int availableSamples = rbs.available();
			while(availableSamples ==0){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				availableSamples = rbs.available();
			}
			float[][] output = {new float[availableSamples]};
			rbs.retrieve(output);
			audioEvent.setFloatBuffer(output[0]);
			return true;
		}
		
		@Override
		public void processingFinished() {
			rbs.dispose();
		}

		public void setTimeRatio(double d) {
			rbs.setTimeRatio(d);
			
		}

		public void setPitchScale(double d) {
			rbs.setPitchScale(d);
		}
}
