package de.ad.openthesaurus;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private Button btnAbout;
	private Button btnTwitter;
	private EditText etSearch;
	private ImageButton ibSubmit;
	private ImageButton ibVoiceInput;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initUIElements();
	}

	private void initUIElements() {
		btnAbout = (Button) findViewById(R.id.btnAbout);
		btnAbout.setOnClickListener(this);

		btnTwitter = (Button) findViewById(R.id.btnTwitter);
		btnTwitter.setOnClickListener(this);

		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							startSearch();
							return true;
						} else if (event != null
								&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							startSearch();
							return true;
						}
						return false;
					}
				});

		ibSubmit = (ImageButton) findViewById(R.id.ibSubmit);
		ibSubmit.setOnClickListener(this);

		ibVoiceInput = (ImageButton) findViewById(R.id.ibVoiceInput);
		ibVoiceInput.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			finish();
		} else if (requestCode == 1 && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			if (matches != null && matches.size() > 0) {
				Intent i = new Intent(this, SearchActivity.class);
				i.putExtra("searchstring", matches.get(0));
				startActivityForResult(i, 0);
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
				startActivityForResult(intent, 1);
			} else {
				Toast
						.makeText(
								this,
								"Bitte Google-Sprachsuche installieren, um dieses Feature zu nutzen.",
								Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.ibSubmit:
			startSearch();
			break;
		default:
			break;
		}
	}

	private void startSearch() {
		Intent i = new Intent(this, SearchActivity.class);
		i.putExtra("searchstring", etSearch.getText().toString());
		startActivityForResult(i, 0);
	}
}