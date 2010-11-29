package de.ad.openthesaurus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.realjson.JSONArray;
import org.realjson.JSONException;
import org.realjson.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TwitterActivity extends Activity implements OnClickListener {
	private Button btnBack;
	private ListView lvTweets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter);

		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);

		lvTweets = (ListView) findViewById(R.id.lvTweets);

		new TwitterTask().execute("openthesaurus");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			finish();
			break;
		default:
			break;
		}
	}

	private class TwitterTask extends AsyncTask<String, Void, String> {
		private final static String _url = "http://api.twitter.com/1/statuses/user_timeline.json?screen_name=<Name>";

		private ProgressDialog mDialog;

		@Override
		protected void onPreExecute() {
			mDialog = ProgressDialog.show(TwitterActivity.this, "",
					"Lade Tweets...", true);
		}

		@Override
		protected String doInBackground(String... params) {
			String url = _url.replace("<Name>", URLEncoder.encode(params[0]));
			try {
				URLConnection conn = new URL(url).openConnection();
				conn.connect();

				Writer writer = new StringWriter();

				InputStream is = conn.getInputStream();

				char[] buffer = new char[1024];
				try {
					Reader reader = new BufferedReader(new InputStreamReader(
							is, "UTF-8"));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} finally {
					is.close();
				}

				String json = writer.toString();

				return json;
			} catch (MalformedURLException e) {
				Log.e(getClass().getName(), e.getMessage());
			} catch (IOException e) {
				Log.e(getClass().getName(), e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mDialog.dismiss();

			if (result != null) {
				lvTweets.setAdapter(new TweetListAdapter(result));
			}
		}
	}

	private class TweetListAdapter extends BaseAdapter {
		private List<String> mTweets;

		public TweetListAdapter(String result) {

			mTweets = new ArrayList<String>();

			// parse json object
			try {
				JSONArray statuses = new JSONArray(result);

				for (int i = 0; i < statuses.length(); i++) {
					JSONObject status = statuses.getJSONObject(i);

					mTweets.add(status.getString("text"));
				}

			} catch (Exception e) {
				Log.e(getClass().getName(), e.getMessage());
			} finally {
				if (mTweets.size() == 0) {
					mTweets.add("Keine Tweets.");
				}
			}
		}

		@Override
		public int getCount() {
			return mTweets.size();
		}

		@Override
		public Object getItem(int position) {
			return mTweets.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(TwitterActivity.this);
			tv.setAutoLinkMask(Linkify.WEB_URLS);
			tv.setText(mTweets.get(position));

			return tv;
		}

	}
}