package thejh.almighty;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;

/**
 * An activity that prevents users from being tricked into
 * clicking stuff by disabling some GUI elements for the
 * first three seconds after becoming visible.
 */
public abstract class DelayedActivity extends Activity {
	private ProgressBarUpdater pbu = null;
	private Handler h;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		h = new Handler();
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    	if (pbu != null) {
    		synchronized (pbu) {
        		pbu.stop = true;
        		pbu = null;
			}
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (pbu != null) throw new RuntimeException("the progressbarupdater can't exist here!");
    	ProgressBar pbar = (ProgressBar) findViewById(R.id.safety_delay_progressbar);
    	pbar.setProgress(0);
    	pbar.setMax(300);
    	pbu = new ProgressBarUpdater(pbar);
    	pbu.start();
    }
    
    class ProgressBarUpdater extends Thread {
    	boolean stop = false;
    	private int progress = 0;
    	private ProgressBar bar;
    	
    	public ProgressBarUpdater(ProgressBar pbar) {
			bar = pbar;
		}

		@Override
    	public void run() {
    		while (progress < 300) {
    			try {Thread.sleep(10);} catch (InterruptedException e) {
    				continue;
    			}
    			progress++;
    			synchronized (this) {
					if (stop) return;
					h.post(new Runnable() {
						public void run() {
							bar.setProgress(progress);
						}
					});
				}
    		}
    		
    		synchronized (this) {
    			if (stop) return;
    			h.post(new Runnable() {
    				public void run() {
    					enableSensitiveElements();
    				}
    			});
    		}
    	}
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	disableSensitiveElements();
    }
    
    protected void disableButton(int id) {
    	Button button = (Button) findViewById(id);
    	button.setEnabled(false);
    	button.setTextColor(Color.DKGRAY);
    }
    
    protected void enableButton(int id, int color) {
    	Button button = (Button) findViewById(id);
    	button.setEnabled(true);
    	button.setTextColor(color);
    }

    protected abstract void enableSensitiveElements();
	protected abstract void disableSensitiveElements();
}
