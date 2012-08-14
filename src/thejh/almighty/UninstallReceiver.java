package thejh.almighty;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class UninstallReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context c, Intent i) {
		if (i.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return;
		int uid = i.getIntExtra(Intent.EXTRA_UID, -1);
		// TODO what about shared-uid apps?
		// just delete the file, even if it's not there
		File f = new File(Constants.SETTINGS_DIR, "uid:"+uid);
		f.delete();
	}
}
