package com.ray.weblauncher;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		startService(new Intent(this, WLService.class));
	}

}
