package se.embargo.sonogram.widget;

import se.embargo.core.concurrent.IForBody;
import se.embargo.core.concurrent.Parallel;
import se.embargo.sonogram.dsp.ISignalFilter;
import se.embargo.sonogram.io.ISonarController;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class SonogramView extends BufferedView implements ISonarController, ISignalFilter {
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
	public void accept(ISignalFilter.Item item) {
		// Dirty access to pixel array
		int[] pixels = _pixels;
		
		int width = item.canvas.width();
		int height = item.canvas.height();
		Parallel.forRange(new DrawSonogram(pixels, width, height), item, 0, Math.min(height, pixels.length / width));
		
		postInvalidateCanvas();
	}

	@Override
	protected synchronized void draw(Canvas canvas, Rect dataWindow, Rect canvasWindow) {
		canvas.drawBitmap(_pixels, 0, canvasWindow.width(), 0f, 0f, canvasWindow.width(), canvasWindow.height(), false, null);
	}
	
	private static class DrawSonogram implements IForBody<ISignalFilter.Item> {
		private final int[] _pixels;
		private final int _width, _height;
		
		public DrawSonogram(int[] pixels, int width, int height) {
			_pixels = pixels;
			_width = width;
			_height = height;
		}
		
		@Override
		public void run(Item item, int it, int last) {
		    // Draw the sonogram
	    	final int[] pixels = _pixels;
	    	final float factor = 255f / (float)Math.log(item.maxvalue + 1);
		    //final float factor = 255f / item.maxvalue;
	    	final float[] output = item.output;
		    
	    	for (; it < last; it++) {
			    for (int i = it * _width, o = (_height - it - 1) * _width, il = i + _width; i < il; i++, o++) {
			    	// Scale the value logarithmically into the maximum height
			    	int value = (int)(factor * Math.log(Math.abs(output[i]) + 1));
			    	//int value = (int)(factor * Math.abs(output[it]));
			    	pixels[o] = 0xff000000 | (value << 16) | (value << 8) | value;
			    }
	    	}
		}
	}
}
