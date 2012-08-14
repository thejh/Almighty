package thejh.almighty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This activity asks the user whether he wants to grant root access to a
 * specific app. To prevent attacks that ask for permission exactly when
 * the user taps on what was on the screen before, there is a one-second
 * safety delay that gets reset whenever the activity loses focus.
 */
public class AskPermissionActivity extends Activity {
	public static final String EXTRA_CALLER_UID = "uid";
	public static final String EXTRA_TARGET_PID = "pid";
	
	private ProgressBarUpdater pbu = null;
	private Handler h;
	private int caller_uid = -1;
	private int target_pid = -1;
	
	static {
		if (!Constants.SETTINGS_DIR.exists()) {
			// TODO check that this sets good permissions
			Constants.SETTINGS_DIR.mkdir();
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ask_permission);
        h = new Handler();
    }
    
    private void disableButton(int id) {
    	Button button = (Button) findViewById(id);
    	button.setEnabled(false);
    	button.setTextColor(Color.DKGRAY);
    }
    
    private void enableButton(int id, int color) {
    	Button button = (Button) findViewById(id);
    	button.setEnabled(true);
    	button.setTextColor(color);
    }
    
    private void setText(int id, CharSequence text) {
    	TextView t = (TextView) findViewById(id);
    	t.setText(text);
    }
    
    @Override
    public void onStart() {
    	Intent i = getIntent();
    	caller_uid = i.getIntExtra(EXTRA_CALLER_UID, -1);
    	if (caller_uid == -1) {
    		throw new RuntimeException("missing caller_uid parameter");
    	}
    	target_pid = i.getIntExtra(EXTRA_TARGET_PID, -1);
    	if (target_pid == -1) {
    		throw new RuntimeException("missing target_pid parameter");
    	}
    	
    	disableButton(R.id.allow_button);
    	disableButton(R.id.deny_button);
    	
    	AlmightyAppInfo appInfo = new AlmightyAppInfo(caller_uid, getPackageManager());
    	setText(R.id.caller_uid, appInfo.getUid()+"");
    	setText(R.id.caller_package, appInfo.getPackageName());
    	setText(R.id.caller_name, appInfo.getName());
    }
    
    @Override
    public void onPause() {
    	if (pbu != null) {
    		synchronized (pbu) {
        		pbu.stop = true;
        		pbu = null;
			}
    	}
    }
    
    @Override
    public void onResume() {
    	if (pbu != null) throw new RuntimeException("the progressbarupdater can't exist here!");
    	ProgressBar pbar = (ProgressBar) findViewById(R.id.safety_delay_progressbar);
    	pbar.setProgress(0);
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
    		while (progress < 100) {
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
    					enableButton(R.id.allow_button, Color.GREEN);
    					enableButton(R.id.deny_button, Color.RED);
    				}
    			});
    		}
    	}
    }
    
    private void terminate_with_result(char result) {
    	File result_file = new File(Constants.SETTINGS_DIR, "uid:" + caller_uid);
    	try {
    		FileOutputStream out = new FileOutputStream(result_file);
			out.write(result);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	Process.sendSignal(target_pid, Process.SIGNAL_KILL);
    	finish();
    }
    
    public void allow_clicked(View view) {
    	terminate_with_result('A'); // A means "allow"
    }
    
    public void deny_clicked(View view) {
    	terminate_with_result('D'); // D means "deny"
    }
}