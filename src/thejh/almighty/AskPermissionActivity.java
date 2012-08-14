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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This activity asks the user whether he wants to grant root access to a
 * specific app. To prevent attacks that ask for permission exactly when
 * the user taps on what was on the screen before, there is a three-second
 * safety delay that gets reset whenever the activity loses focus.
 */
public class AskPermissionActivity extends DelayedActivity {
	public static final String EXTRA_CALLER_UID = "uid";
	public static final String EXTRA_TARGET_PID = "pid";
	
	private int caller_uid = -1;
	private int target_pid = -1;
	
	static {
		if (!Constants.SETTINGS_DIR.exists()) {
			Constants.SETTINGS_DIR.mkdir();
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ask_permission);
    }
    
    private void setText(int id, CharSequence text) {
    	TextView t = (TextView) findViewById(id);
    	t.setText(text);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	Intent i = getIntent();
    	caller_uid = i.getIntExtra(EXTRA_CALLER_UID, -1);
    	if (caller_uid == -1) {
    		throw new RuntimeException("missing caller_uid parameter");
    	}
    	target_pid = i.getIntExtra(EXTRA_TARGET_PID, -1);
    	if (target_pid == -1) {
    		throw new RuntimeException("missing target_pid parameter");
    	}
    	
    	AlmightyAppInfo appInfo = new AlmightyAppInfo(caller_uid, getPackageManager());
    	setText(R.id.caller_uid, appInfo.getUid()+"");
    	setText(R.id.caller_package, appInfo.getPackageName());
    	setText(R.id.caller_name, appInfo.getName());
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
    	Log.d("AskPermissionActivity", "sending SIGKILL to "+target_pid+" to signal that a result exists");
    	Process.sendSignal(target_pid, Process.SIGNAL_KILL);
    	finish();
    }
    
    public void allow_clicked(View view) {
    	terminate_with_result('A'); // A means "allow"
    }
    
    public void deny_clicked(View view) {
    	terminate_with_result('D'); // D means "deny"
    }

	@Override
	protected void enableSensitiveElements() {
		enableButton(R.id.allow_button, Color.GREEN);
		enableButton(R.id.deny_button, Color.RED);
	}

	@Override
	protected void disableSensitiveElements() {
    	disableButton(R.id.allow_button);
    	disableButton(R.id.deny_button);
	}
}