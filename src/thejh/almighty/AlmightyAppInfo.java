package thejh.almighty;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AlmightyAppInfo {
	private int uid;
	
	private CharSequence name;

	private String packageName;
	
	private boolean allowed;
	
	public AlmightyAppInfo(int uid, PackageManager pm) {
		this.uid = uid;
		
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
	
	public AlmightyAppInfo(int uid, PackageManager pm, boolean allowed) {
		this(uid, pm);
		this.allowed = allowed;
	}
	
	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
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
}
