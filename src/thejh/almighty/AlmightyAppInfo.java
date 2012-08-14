package thejh.almighty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AlmightyAppInfo {
	private int uid;
	
	private CharSequence name;

	private String packageName;
	
	private AllowedState allowed = AllowedState.UNDEFINED;
	
	public AlmightyAppInfo(int uid, PackageManager pm) {
		this.uid = uid;
		
		if (pm != null) {
			// this stuff throws in case someone uses an uid that's not
			// associated with an app
			ApplicationInfo appInfo;
			try {
				appInfo = pm.getApplicationInfo(pm.getPackagesForUid(uid)[0], 0);
			} catch (NameNotFoundException e) {
				throw new RuntimeException(e);
			}

			this.packageName = appInfo.packageName;
			this.name = appInfo.loadLabel(pm);
		}
	}
	
	public AlmightyAppInfo(int uid, PackageManager pm, AllowedState allowed) {
		this(uid, pm);
		this.allowed = allowed;
	}
	
	public AllowedState isAllowed() {
		return allowed;
	}

	public void setAllowed(AllowedState allowed) {
		this.allowed = allowed;
	}

	public int getUid() {
		return uid;
	}

	public CharSequence getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}
	
	public AlmightyAppInfo withAllowed(boolean required) {
		try {
			FileInputStream in = new FileInputStream(new File(Constants.SETTINGS_DIR, "uid:"+uid));
			boolean allowed = in.read() == 'A';
			in.close();
			this.allowed = allowed ? AllowedState.ALLOW : AllowedState.DENY;
		} catch (IOException e) {
			if (required) {
				throw new RuntimeException(e);
			}
		}
		return this;
	}
}
