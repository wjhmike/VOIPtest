import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;

/*
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
*/

public class Client {

	boolean stopCapture = false;
	ByteArrayOutputStream byteArrayOutputStream;
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	AudioInputStream audioInputStream;
	BufferedOutputStream out = null;
	BufferedInputStream in = null;
	Socket sock = null;
	SourceDataLine sourceDataLine;
	private static String IPaddress;

	private static JTextField IpAddressField;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		/*Display display = Display.getDefault();
		Shell shell = new Shell();
		shell.setSize(850, 400);
		shell.setText("Voip Phone");

		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

			    Client tx = new Client();
			    tx.jitterBuffer();
			}
		});
		btnNewButton.setBounds(37, 35, 261, 79);
		btnNewButton.setText("Open Gateway");

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}*/
		JFrame frame = new JFrame("Gold Squadron VOIP");
		frame.setSize(850,400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		JButton button = new JButton("connect");
		button.setBounds(37,35,50,10);
		JLabel label = new JLabel("IP address: ");
		JLabel connection = new JLabel("connect: "+ IPaddress);


		IpAddressField = new JTextField(20);


		JPanel panel = new JPanel();
		panel.add(button);
		panel.add(label);
		panel.add(IpAddressField);
		panel.add(connection);
		frame.getContentPane().add(panel);

		IpAddressField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == IpAddressField) {
					IPaddress = IpAddressField.getText();
				}
			}
		});


		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == button) {
					Client tx = new Client();
					tx.jitterBuffer();
				}
			}
		});


	}
	private void jitterBuffer() {
	    try {
	        sock = new Socket(IPaddress, 1024);
	        out = new BufferedOutputStream(sock.getOutputStream());
	        in = new BufferedInputStream(sock.getInputStream());

	        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
	        System.out.println("Available Hardware devices:");
	        for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
	            System.out.println(cnt+":"+mixerInfo[cnt].getName());
	        }
	        audioFormat = getAudioFormat();

	        DataLine.Info dataLineInfo = new DataLine.Info(
	                TargetDataLine.class, audioFormat);

	        Mixer mixer = AudioSystem.getMixer(mixerInfo[1]);    //Select Available Hardware Devices for the micro, for my Notebook it is number 3.

	        targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);

	        targetDataLine.open(audioFormat);
	        targetDataLine.start();

	        Thread captureThread = new CaptureThread();
	        captureThread.start();

	        DataLine.Info dataLineInfo1 = new DataLine.Info(
	                SourceDataLine.class, audioFormat);
	        sourceDataLine = (SourceDataLine) AudioSystem
	                .getLine(dataLineInfo1);
	        sourceDataLine.open(audioFormat);
	        sourceDataLine.start();

	        Thread playThread = new PlayThread();
	        playThread.start();

	    } catch (Exception e) {
	        System.out.println(e);
	        System.exit(0);
	    }
	}

	/*private AudioFormat encode(AudioFormat audio){

	}*/

	class CaptureThread extends Thread {

	    byte tempBuffer[] = new byte[10000];

	    @Override
	    public void run() {
	        byteArrayOutputStream = new ByteArrayOutputStream();
	        stopCapture = false;
	        try {
	            while (!stopCapture) {

	                int cnt = targetDataLine.read(tempBuffer, 0,
	                        tempBuffer.length);

	                out.write(tempBuffer);

	                if (cnt > 0) {

	                    byteArrayOutputStream.write(tempBuffer, 0, cnt);

	                }
	            }
	            byteArrayOutputStream.close();
	        } catch (Exception e) {
	            System.out.println(e);
	            System.exit(0);
	        }
	    }
	}
	
	private AudioFormat getAudioFormat() {
	    float sampleRate = 48000.0F;

	    int sampleSizeInBits = 16;

	    int channels = 1;

	    boolean signed = true;

	    boolean bigEndian = false;


	    return new AudioFormat(sampleRate, sampleSizeInBits, channels,
	           signed, bigEndian);
	}
	
	class PlayThread extends Thread {

	    byte tempBuffer[] = new byte[10000];

	    @Override
	    public void run() {
	        try {
	            while (in.read(tempBuffer) != -1) {
	                sourceDataLine.write(tempBuffer, 0, 10000);

	            }
	            sourceDataLine.drain();
	            sourceDataLine.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	  }
}
