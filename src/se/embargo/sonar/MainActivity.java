package se.embargo.sonar;

import se.embargo.sonar.widget.HistogramView;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = "MainActivity";
	
	private HistogramView _histogramView;
	private Sonar _sonar;
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.main_activity);
		_histogramView = (HistogramView)findViewById(R.id.histogram);
		
		_sonar = new Sonar();
		_sonar.addListener(new ISonarListener() {
			@Override
			public void receive(int offset, float[] output) {
				_histogramView.update(offset, output);
			}
		});
		
		_histogramView.setResolution(_sonar.getResolution());
		//_histogramView.setZoom((float)PULSEINTERVAL / PULSERANGE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		_sonar.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		_sonar.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
