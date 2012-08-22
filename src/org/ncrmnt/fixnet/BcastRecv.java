package org.ncrmnt.fixnet;

import java.io.DataOutputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;



public class BcastRecv extends BroadcastReceiver {
	private static final String TAG = "fixnet";
	

	public void RunAsRoot(String[] cmds) {
		Process p;
		try {
			p = Runtime.getRuntime().exec("su");

			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			for (String tmpCmd : cmds) {
				Log.d(TAG, "execRoot: " + tmpCmd);
				os.writeBytes(tmpCmd + "\n");
			}
			os.writeBytes("exit\n");
			os.flush();
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onReceive(Context ctx, Intent i) {
		// TODO Auto-generated method stub
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean enable = mPrefs.getBoolean("enable", true);
		if ((enable) && (i.getAction().equals(Intent.ACTION_BOOT_COMPLETED))) {
			Log.d(TAG, "Applying network fixups");
			String hostname = mPrefs.getString("hostname", "glesia");
			String macaddr = mPrefs.getString("macaddr", "00:19:d1:0e:62:38");
			String iface = mPrefs.getString("iface", "eth0");
			String cmds[] = {
					"busybox ifconfig " + iface + " down",
					"setprop net.hostname " + hostname,
					"busybox ifconfig " + iface + " hw ether " + macaddr,
					"busybox ifconfig " + iface + " up" };
			RunAsRoot(cmds);
			Log.d(TAG, "Network interface fixup completed");
		}
	}
}