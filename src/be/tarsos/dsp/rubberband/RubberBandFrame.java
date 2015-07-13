package be.tarsos.dsp.rubberband;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;

public class RubberBandFrame  extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JSlider pitchSlider;
	private final JSlider tempoSlider;
	private RubberBandAudioProcessor rbs;
	private AudioDispatcher adp;
	
	public RubberBandFrame() throws LineUnavailableException{
		this.setLayout(new GridLayout(0,1));
		this.setTitle("RubberBand time stretcher");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel openLabel = new JLabel("Open audio file:");
		JButton openFileButton = new JButton("Open...");
		openFileButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(RubberBandFrame.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						openFile(chooser.getSelectedFile().getAbsolutePath());
					} catch (LineUnavailableException e1) {						
						e1.printStackTrace();
					}
				}
			}});
		
		final JLabel tempoLabel = new JLabel("Duration: 100.00%");
		tempoSlider = new JSlider(200,6800);
		tempoSlider.setPaintLabels(false);
		tempoSlider.setValue(1000);
		tempoSlider.setPaintTicks(true);
		tempoSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				tempoLabel.setText(String.format("Duration: %03.2f%%",tempoSlider.getValue()/10.0));
				if(rbs!=null){
					rbs.setTimeRatio(tempoSlider.getValue()/1000.0);
				}
			}
		});
		
		final JLabel pitchLabel = new JLabel("Pitch: 100.00 %");
		pitchSlider = new JSlider(200,6800);
		pitchSlider.setValue(1000);
		pitchSlider.setPaintLabels(false);
		pitchSlider.setPaintTicks(true);
		
		pitchSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				pitchLabel.setText(String.format("Pitch: %03.2f%%",pitchSlider.getValue()/10.0));
				if(rbs!=null){
					rbs.setPitchScale(pitchSlider.getValue()/1000.0);
				}
			}
		});
		
		this.add(openLabel);
		this.add(openFileButton);
		this.add(tempoLabel);
		this.add(tempoSlider);
		this.add(pitchLabel);
		this.add(pitchSlider);
	}
	
	private void openFile(String audioFile) throws LineUnavailableException{
		if(adp !=null){
			adp.stop();
		}
		adp =  AudioDispatcherFactory.fromPipe(audioFile, 44100, 4096, 0);
		TarsosDSPAudioFormat format = adp.getFormat();
		rbs = new RubberBandAudioProcessor(44100, tempoSlider.getValue()/1000.0, pitchSlider.getValue()/1000.0);
		adp.addAudioProcessor(rbs);
		adp.addAudioProcessor(new AudioPlayer(JVMAudioInputStream.toAudioFormat(format)));
		new Thread(adp).start();
	}
	
	public static void main(String[] args){
		UIManager.put("Slider.paintValue", false); 
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
				}
				JFrame frame;
				try {
					frame = new RubberBandFrame();
					frame.setSize(500,250);
					frame.setVisible(true);
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
}
