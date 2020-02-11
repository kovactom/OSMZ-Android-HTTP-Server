package com.vsb.tamz.osmz_http_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class SocketServer2 extends Thread {

    private static final String response = "HTTP/1.1 200 OK\n" +
            "Content-Type: text/html\n" +
            "\n" +
            "<html>" +
            "<body><h1>Response</h1></body>\n" +
            "</html>";

	ServerSocket serverSocket;
	public final int port = 12345;
	boolean bRunning;
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}
	
	public void run() {
        try {
        	Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning) {
            	Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept(); 
                Log.d("SERVER", "Socket Accepted");
                
                OutputStream o = s.getOutputStream();
	        	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
	        	BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                Log.d("SERVER", "accepted message:");
	        	String tmp = in.readLine();
	        	String method = "";
	        	String path = "";
	        	String protocol = "";
                Pattern headerPattern = Pattern.compile("(GET)(/.+)(HTTP/.+)");
	        	if (!tmp.isEmpty()) {
                    Matcher headerMatcher = headerPattern.matcher(tmp);
                    if (headerMatcher.find()) {
                        method = headerMatcher.group(0);
                        path = headerMatcher.group(1);
                        protocol = headerMatcher.group(2);
                    }
                    Log.d("SERVER", "Method: " + method);
                    Log.d("SERVER", "Path: " + path);
                    Log.d("SERVER", "Protocol: " + protocol);
                }
	            while(!(tmp = in.readLine()).isEmpty()) {
//                    out.write(tmp.toUpperCase());
                    Log.d("SERVER", tmp);
                }
                out.write(response);
                out.flush();
	            
                s.close();
                Log.d("SERVER", "Socket Closed");
            }
        } 
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
            	Log.d("SERVER", "Normal exit");
            else {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        }
        finally {
        	serverSocket = null;
        	bRunning = false;
        }
    }

}
