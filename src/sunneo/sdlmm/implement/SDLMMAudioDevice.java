package sunneo.sdlmm.implement;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SDLMMAudioDevice {
	SourceDataLine line = null;
	AudioFormat format;
	DataLine.Info info;
	int frameSize = -1;

	public SDLMMAudioDevice(int hz, int channels) {
		format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float) hz, 16, channels, 4, (float) hz, false);
		info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {

			format = new AudioFormat(format.getSampleRate(), 16, format.getChannels(), true, false);
			info = new DataLine.Info(SourceDataLine.class, format);

		}
		frameSize = getBufferSize();
	}

	public boolean start() {
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public int getBufferSize() {
		int framesize = format.getFrameSize();
		return 4 * 1024 * framesize; // the buffer
	}

	public int write(byte[] b, int off, int len) {
		int writeBytes = (len / frameSize) * frameSize;
		return line.write(b, off, writeBytes);
	}

	public void flush() {
		line.drain();
	}

	public void close() {
		line.close();
	}

	public void finalize() {
		try {
			super.finalize();
			close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
