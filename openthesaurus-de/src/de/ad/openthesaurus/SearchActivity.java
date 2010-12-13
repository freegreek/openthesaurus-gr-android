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
import org.realjson.XML;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends Activity implements OnClickListener,
		OnItemClickListener {
	private Button btnAbout;
	private Button btnTwitter;
	private Button btnSimilar1;
	private Button btnSimilar2;
	private Button btnSimilar3;
	private Button btnSimilar4;
	private EditText etSearch;
	private ImageButton ibSubmit;
	private ImageButton ibVoiceInput;
	private ListView lvResults;
	private View vLoad;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		initUIElements();
	}

	private void initUIElements() {
		btnAbout = (Button) findViewById(R.id.btnAbout);
		btnAbout.setOnClickListener(this);

		btnTwitter = (Button) findViewById(R.id.btnTwitter);
		btnTwitter.setOnClickListener(this);

		btnSimilar1 = (Button) findViewById(R.id.btnSimilar1);
		btnSimilar1.setOnClickListener(this);

		btnSimilar2 = (Button) findViewById(R.id.btnSimilar2);
		btnSimilar2.setOnClickListener(this);

		btnSimilar3 = (Button) findViewById(R.id.btnSimilar3);
		btnSimilar3.setOnClickListener(this);

		btnSimilar4 = (Button) findViewById(R.id.btnSimilar4);
		btnSimilar4.setOnClickListener(this);

		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.setText(getIntent().getStringExtra("searchstring"));
		etSearch
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							performSearch();
							return true;
						} else if (event != null
								&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							performSearch();
							return true;
						}
						return false;
					}
				});

		lvResults = (ListView) findViewById(R.id.lvResults);
		lvResults.setOnItemClickListener(this);

		ibSubmit = (ImageButton) findViewById(R.id.ibSubmit);
		ibSubmit.setOnClickListener(this);

		ibVoiceInput = (ImageButton) findViewById(R.id.ibVoiceInput);
		ibVoiceInput.setOnClickListener(this);
		
		vLoad = findViewById(R.id.llLoad);

		if (etSearch.getText().length() > 0)
			new ThesaurusTask().execute(etSearch.getText().toString());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			if (matches != null && matches.size() > 0) {
				etSearch.setText(matches.get(0));
				new ThesaurusTask().execute(matches.get(0));
			} else {
				Toast.makeText(this,
						"Die Sprachsuche ergab leider keine Treffer.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnAbout:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.btnTwitter:
			startActivity(new Intent(this, TwitterActivity.class));
			break;
		case R.id.ibSubmit:
			performSearch();
			break;
		case R.id.ibVoiceInput:
			// Check to see if a recognition activity is present
			PackageManager pm = getPackageManager();
			List activities = pm.queryIntentActivities(new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			if (activities.size() != 0) {
				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Spracheingabe");
				startActivityForResult(intent, 0);
			} else {
				Toast
						.makeText(
								this,
								"Bitte Google-Sprachsuche installieren, um dieses Feature zu nutzen.",
								Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.btnSimilar1:
			etSearch.setText(btnSimilar1.getText());
			new ThesaurusTask().execute(etSearch.getText().toString());
			break;
		case R.id.btnSimilar2:
			etSearch.setText(btnSimilar2.getText());
			new ThesaurusTask().execute(etSearch.getText().toString());
			break;
		case R.id.btnSimilar3:
			etSearch.setText(btnSimilar3.getText());
			new ThesaurusTask().execute(etSearch.getText().toString());
			break;
		case R.id.btnSimilar4:
			etSearch.setText(btnSimilar4.getText());
			new ThesaurusTask().execute(etSearch.getText().toString());
			break;
		default:
			break;
		}
	}

	private void performSearch() {
		new ThesaurusTask().execute(etSearch.getText().toString());
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String searchStr = ((TextView) arg1).getText().toString();
		
		//trim the search string
		if(searchStr.contains(" "))
			searchStr = searchStr.substring(0, searchStr.indexOf(" "));
		
		etSearch.setText(searchStr);
		new ThesaurusTask().execute(searchStr);

	}

	private class ThesaurusTask extends AsyncTask<String, Void, JSONObject> {
		private final static String _url = "http://www.openthesaurus.de/synonyme/search?q=<query>&format=text/xml&similar=true&substring=true";

		@Override
		protected void onPreExecute() {
			vLoad.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			String url = _url.replace("<query>", URLEncoder.encode(params[0]));
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

				String xml = writer.toString();

				// need to encode german "umlaute"
				xml = xml.replace("&#xe4;", "Š");
				xml = xml.replace("&#xf6;", "š");
				xml = xml.replace("&#xfc;", "Ÿ");
				xml = xml.replace("&#xc4;", "€");
				xml = xml.replace("&#xd6;", "…");
				xml = xml.replace("&#xdc;", "†");
				xml = xml.replace("&#xdf;", "§");

				return XML.toJSONObject(xml);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			vLoad.setVisibility(View.GONE);
			
			if (result != null) {
				lvResults.setAdapter(new ResultListAdapter(result));

				btnSimilar1.setVisibility(View.GONE);
				btnSimilar2.setVisibility(View.GONE);
				btnSimilar3.setVisibility(View.GONE);
				btnSimilar4.setVisibility(View.GONE);

				try {
					JSONArray terms = result.getJSONObject("matches")
							.getJSONObject("similarterms").getJSONArray("term");

					btnSimilar1.setText(terms.getJSONObject(0)
							.getString("term"));
					btnSimilar1.setVisibility(View.VISIBLE);
					btnSimilar2.setText(terms.getJSONObject(1)
							.getString("term"));
					btnSimilar2.setVisibility(View.VISIBLE);
					btnSimilar3.setText(terms.getJSONObject(2)
							.getString("term"));
					btnSimilar3.setVisibility(View.VISIBLE);
					btnSimilar4.setText(terms.getJSONObject(3)
							.getString("term"));
					btnSimilar4.setVisibility(View.VISIBLE);
				} catch (JSONException e) {
					Log.e(getClass().getName(), e.getMessage());
				}
			}
		}
	}

	private class ResultListAdapter extends BaseAdapter {
		private List<String> mTerms;

		public ResultListAdapter(JSONObject result) {

			mTerms = new ArrayList<String>();

			// parse json object
			try {
				Object raw = result.getJSONObject("matches").get("synset");

				if (raw instanceof JSONArray) {
					JSONArray synsets = (JSONArray) raw;
					for (int i = 0; i < synsets.length(); i++) {
						JSONObject synset = synsets.getJSONObject(i);

						raw = synset.get("term");

						if (raw instanceof JSONArray) {
							JSONArray terms = (JSONArray) raw;

							for (int j = 0; j < terms.length(); j++) {
								JSONObject term = terms.getJSONObject(j);

								String output = term.getString("term");

								if (term.has("level"))
									output += " (" + term.getString("level")
											+ ")";

								mTerms.add(output);
							}
						} else if (raw instanceof JSONObject) {
							JSONObject term = (JSONObject) raw;

							String output = term.getString("term");

							if (term.has("level"))
								output += " (" + term.getString("level") + ")";

							mTerms.add(output);
						}
					}
				} else if (raw instanceof JSONObject) {
					JSONObject synset = (JSONObject) raw;

					raw = synset.get("term");

					if (raw instanceof JSONArray) {
						JSONArray terms = (JSONArray) raw;

						for (int j = 0; j < terms.length(); j++) {
							JSONObject term = terms.getJSONObject(j);

							String output = term.getString("term");

							if (term.has("level"))
								output += " (" + term.getString("level") + ")";

							mTerms.add(output);
						}
					} else if (raw instanceof JSONObject) {
						JSONObject term = (JSONObject) raw;

						String output = term.getString("term");

						if (term.has("level"))
							output += " (" + term.getString("level") + ")";

						mTerms.add(output);
					}
				}

			} catch (JSONException e) {
				Log.e(getClass().getName(), e.getMessage());
			} finally {
				if (mTerms.size() == 0) {
					mTerms.add("Keine Treffer.");
				}
			}
		}

		@Override
		public int getCount() {
			return mTerms.size();
		}

		@Override
		public Object getItem(int position) {
			return mTerms.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(SearchActivity.this);
			tv.setText(mTerms.get(position));

			return tv;
		}

	}
}