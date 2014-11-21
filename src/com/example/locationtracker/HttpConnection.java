package com.example.locationtracker;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.NameValuePair;

public class HttpConnection {
	public static final int GET_REQUEST = 1;
	public static final int POST_REQUEST = 2;
	
	private URL url = null;
	private HttpURLConnection urlConnection = null;
	private int type = 0;
	
	private class HttpGetRequestCallable implements Callable<String> {
		private HttpURLConnection urlCon;
		private int ty;
		private String data;
		
		public HttpGetRequestCallable(HttpURLConnection u, int t, String d) {
			urlCon = u;
			ty = t;
			data = d;
		}
		
		public String call()
		{
			InputStream in = null;
			try
			{
				in = new BufferedInputStream(urlCon.getInputStream());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		    Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
			String response = scanner.useDelimiter("\\A").next();
			scanner.close();
			return response;
		}
	}
	
	private class HttpPostRequestCallable implements Callable<String> {
		private HttpURLConnection urlCon;
		private String data;
		private String token;
		
		public HttpPostRequestCallable(HttpURLConnection u, String d, String t) {
			urlCon = u;
			try {
				urlCon.setFixedLengthStreamingMode(d.getBytes().length);
				urlCon.setRequestMethod("POST");
				urlCon.setDoOutput(true);
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			token = t;
			data = d;
		}
		
		public String call()
		{
			if(token != null)
				urlCon.setRequestProperty ("Authorization", "Token "+token);
			InputStream in = null;
			PrintWriter out = null;
			try
			{
				out = new PrintWriter(urlCon.getOutputStream());
				out.print(data);
				out.close();
				try {
					in = new BufferedInputStream(urlCon.getInputStream());
				}
				catch(FileNotFoundException e)
				{
					in = new BufferedInputStream(urlCon.getErrorStream());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		    Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
			String response = scanner.useDelimiter("\\A").next();
			scanner.close();
			return response;
		}
	}
	
	public HttpConnection(String u, int t) throws IOException
	{
		url = new URL(u);
		urlConnection = (HttpURLConnection) url.openConnection();
		type = t;
	}
	
	public String GET() throws InterruptedException, ExecutionException
	{
		final ExecutorService service;
        final Future<String> task;
        service = Executors.newFixedThreadPool(1);
        task = service.submit(new HttpGetRequestCallable(urlConnection, HttpConnection.GET_REQUEST, null));
		String response = task.get();
		return response;
	}
	
	public String POST(String d, String token, Boolean wait) throws InterruptedException, ExecutionException
	{
		final ExecutorService service;
        final Future<String> task;
        service = Executors.newFixedThreadPool(1);
        task = service.submit(new HttpPostRequestCallable(urlConnection, d, token));
		String response = null;
		if(wait)
			response = task.get();
		return response;
	}
	
	public void close()
	{
		urlConnection.disconnect();
	}
}
