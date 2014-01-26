package se.embargo.sonar.widget;

import se.embargo.core.concurrent.IForBody;
import se.embargo.core.concurrent.Parallel;
import se.embargo.sonar.ISonarController;
import se.embargo.sonar.dsp.ISignalFilter;
import se.embargo.sonar.dsp.ISignalFilter.Item;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class SonogramView extends BufferedView implements ISonarController {
	private final Paint _outline;
	private volatile int[] _pixels;

	public SonogramView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		_outline = new Paint();
		_outline.setColor(Color.WHITE);
	}

	public SonogramView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SonogramView(Context context) {
		this(context, null);
	}
	
	@Override
	protected synchronized void setCanvas(Rect canvas) {
		super.setCanvas(canvas);
		_pixels = new int[canvas.width() * canvas.height()];
	}
	
	@Override
	public synchronized void setSonarResolution(Rect resolution) {
		setResolution(resolution);
	}

	/**
	 * @note	Must not lock or the audio reader thread will be blocked
	 */
	@Override
	public Rect getSonarWindow() {
		return getWindow();
	}
	
	/**
	 * @note	Must not lock or the audio reader thread will be blocked
	 */
	@Override
	public Rect getSonarCanvas() {
		return getCanvas();
	}
	
	@Override
	public void receive(ISignalFilter.Item item) {
		// Dirty access to pixel array
		int[] pixels = _pixels;		
		Parallel.forRange(new DrawSonogram(pixels), item, 0, Math.min(pixels.length, item.output.length));
		
		postInvalidateCanvas();
	}

	@Override
	protected synchronized void draw(Canvas canvas, Rect dataWindow, Rect canvasWindow) {
		canvas.drawBitmap(_pixels, 0, canvasWindow.width(), 0f, 0f, canvasWindow.width(), canvasWindow.height(), false, null);
	}
	
	private static class DrawSonogram implements IForBody<ISignalFilter.Item> {
		private final int[] _pixels;
		
		public DrawSonogram(int[] pixels) {
			_pixels = pixels;
		}
		
		@Override
		public void run(Item item, int it, int last) {
		    // Draw the sonogram
	    	final int[] pixels = _pixels;
	    	final float factor = 255f / (float)Math.log(item.maxvalue + 1);
		    //final float factor = 255f / max;
	    	final float[] output = item.output;
		    
		    for (; it < last; it++) {
		    	// Scale the value logarithmically into the maximum height
		    	int value = (int)(factor * Math.log(Math.abs(output[it]) + 1));
		    	//int value = factor * Math.abs(values[i]);
		    	pixels[it] = 0xff000000 | (value << 16) | (value << 8) | value;
		    }
		}
	}
}
