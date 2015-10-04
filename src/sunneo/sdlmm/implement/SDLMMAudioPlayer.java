package sunneo.sdlmm.implement;

public class SDLMMAudioPlayer extends SDLMMAudioDevice {
	byte[] buffer;
	int remains = 0;

	public SDLMMAudioPlayer(int hz, int channel) {
		super(hz, channel);
		buffer = new byte[frameSize];
	}

	public int write(byte[] b, int off, int len) {
		int bytesToWrite = len + remains;
		int ret = 0;
		if (bytesToWrite > buffer.length) {
			bytesToWrite = buffer.length - remains;
			System.arraycopy(b, off, buffer, remains, len);
			remains = 0;
			ret = super.write(buffer, 0, buffer.length);
		} else {
			System.arraycopy(b, off, buffer, remains, len);
			remains = (bytesToWrite) % frameSize;
			bytesToWrite = bytesToWrite - remains;
			ret = super.write(buffer, 0, bytesToWrite);
		}
		flush();
		return ret;
	}
}
