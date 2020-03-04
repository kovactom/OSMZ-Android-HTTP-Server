package com.vsb.tamz.osmz_http_server;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HttpServerActivity extends Activity implements OnClickListener{

	private SocketServer2 s;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);
        
        Button btn1 = (Button)findViewById(R.id.startServerBtn);
        Button btn2 = (Button)findViewById(R.id.stopServerBtn);
         
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.http_server, menu);
        return true;
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.startServerBtn) {
			s = new SocketServer2();
			s.start();
		}
		if (v.getId() == R.id.stopServerBtn) {
			s.close();
			try {
				s.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
    
}
