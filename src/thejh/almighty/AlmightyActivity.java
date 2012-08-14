package thejh.almighty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AlmightyActivity extends Activity {
	public static final File SETTINGS_DIR = new File("/data/data/thejh.almighty/uid_ok/");
	
	private ListView mListView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mListView = (ListView) findViewById(R.id.apps_list);
    }
    
    @Override
    public void onResume() {
    	final ArrayList<AlmightyAppInfo> apps = new ArrayList<AlmightyAppInfo>();
    	PackageManager pm = getPackageManager();
    	
    	if (SETTINGS_DIR.exists()) {
    		String[] files = SETTINGS_DIR.list();
    		for (String fileName: files) {
    			if (!fileName.startsWith("uid:")) continue;
    			int uid = Integer.parseInt(fileName.substring(4), 10);
    			try {
    				FileInputStream in = new FileInputStream(new File(SETTINGS_DIR, fileName));
    				boolean allowed = in.read() == 'A';
    				in.close();
    				apps.add(new AlmightyAppInfo(uid, pm, allowed));
    			} catch (IOException e) {
    				throw new RuntimeException(e);
    			}
    		}
    	}
    	
    	final AlmightyActivity self = this;
    	
    	ArrayAdapter<AlmightyAppInfo> adapter = new ArrayAdapter<AlmightyAppInfo>(this, R.layout.app_list_item, -1, apps) {
    		@Override
    		public View getView(int position, View convertView, ViewGroup parent) {
    			AlmightyAppInfo app = apps.get(position);
    			View v = View.inflate(self, R.layout.app_list_item, mListView);
    			
    			TextView v_uid = (TextView)v.findViewById(R.id.app_uid);
    			TextView v_package = (TextView)v.findViewById(R.id.app_package);
    			TextView v_name = (TextView)v.findViewById(R.id.app_name);
    			
    			v_uid.setText(app.getUid()+"");
    			v_package.setText(app.getPackageName()+"");
    			v_name.setText(app.getName()+"");
    			
    			v.setBackgroundColor(app.isAllowed() ? Color.GREEN : Color.RED);
    			
    			return v;
    		}
    	};
    	
    	mListView.setAdapter(adapter);
    	
    	mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				AlmightyAppInfo app = apps.get(position);
				app.setAllowed(!app.isAllowed()); // toggle
				v.setBackgroundColor(app.isAllowed() ? Color.GREEN : Color.RED);
				try {
					FileOutputStream out = new FileOutputStream(new File(SETTINGS_DIR, "uid:"+app.getUid()));
					out.write(app.isAllowed() ? 'A' : 'D');
					out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
		});
    }
}