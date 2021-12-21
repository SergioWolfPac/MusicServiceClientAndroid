package course.examples.Services.KeyClient;
/*******************************************
 * Sergio Ruelas                           *
 * sruela2@uic.edu                         *
 *                                         *
 * August 2021                              *
 * University of Illinois at Chicago       *
 * CS 478                                  *
 * Project 5                               *
 *                                         *
 *  Issues arised as far as gradle went
 *  as well as critical errors in manifest
 *  leading to outright corruption and
 *  having to start from a scratch file again
 *
 *  further issues arose when attempting to
 *  implement recyclerview dependencies
 *  were clashing                          *
 *******************************************/

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
//import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import course.examples.Services.KeyCommon.KeyGenerator;
public class KeyServiceUser extends Activity {
	RecyclerView nameView;
	RecyclerView.LayoutManager layoutManager;
	ArrayList<String> songList;
	ArrayList<String> artistList;

	protected static final String TAG = "KeyServiceUser";
	protected static final int PERMISSION_REQUEST = 0;
	private KeyGenerator mKeyGeneratorService;
	private boolean mIsBound = false;
	List<String> tracks = new ArrayList<>(15);

	/* From musicplayingservice example*/
	private static final int NOTIFICATION_ID = 1;
	private MediaPlayer mPlayer;
	private int mStartID;
	//private Notification notification ; //perhaps not needed
	//private Button stopButton;
	//private Button StartButton;

	private static String CHANNEL_ID = "Music player style" ;


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// UB: Starting in Oreo notifications have a notification channel
		//     The channel defines basic properties of
		//this.createNotificationChannel(); //perhaps not needed
		mPlayer = new MediaPlayer();
		mPlayer = MediaPlayer.create(this, R.raw.yonaguni);

		setContentView(R.layout.main);
		nameView = (RecyclerView) findViewById(R.id.recycler_view);

		final TextView output = (TextView) findViewById(R.id.output);

		final Button stopButton = (Button) findViewById(R.id.StopButton);
		//String message = "There are 5 songs, enter the # (1-5)";
		//output.setText(message);

		stopButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				// Call KeyGenerator and get a new ID
				if (mIsBound) {

					String temp = "Paused";
					output.setText(temp);
					//output.setText(mKeyGeneratorService.retrieveForOne(1 , tracks).get(0));
					mPlayer.pause();
				} else {
					Log.i(TAG, "Ugo says that the service was not bound!");
				}

			}
		});

		final Button StartButton = (Button) findViewById(R.id.StartButton);
		StartButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				try {
					//mPlayer = MediaPlayer.create(this, R.raw.vete);

					//in intent videoIntent it would have been  critical to implement music player
					// to identify which song was clicked on and playing the song rather than
					// opening a youtube video but ran out of time for proper implementation

					// Call KeyGenerator and get a new ID
					if (mIsBound) {
						if(mKeyGeneratorService.checkEmpty()) {
							songList = new ArrayList<>();
							songList = (ArrayList<String>) mKeyGeneratorService.getTitles();
							artistList = new ArrayList<>();
							artistList = (ArrayList<String>) mKeyGeneratorService.getArtists();
							RVClickListener listener = (view,position)->{
								TextView name = (TextView)view.findViewById(R.id.textView);
								Toast.makeText(getBaseContext(),name.getText(),Toast.LENGTH_SHORT).show();
								Uri uri = Uri.parse((String) name.getText());
								Intent videoIntent = new Intent(
										Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + name.getText() + " song"));
								//videoIntent.putExtra(SearchManager.QUERY, uri);// (String) name.getText()));
								startActivity(videoIntent);
							};
							MyAdapter adapter = new MyAdapter(songList, artistList, listener);
							nameView.setHasFixedSize(true);
							nameView.setAdapter(adapter);
							layoutManager = new GridLayoutManager(getBaseContext(), 2);
							nameView.setLayoutManager(layoutManager);
						}


						output.setText("Currently Playing");
						//output.setText(mKeyGeneratorService.retrieveForOne(1 , tracks).get(0));
						mPlayer.setPlaybackParams(mPlayer.getPlaybackParams().setSpeed(0.57f));
						mPlayer.start();
						//mPlayer = MediaPlayer.create(this, R.raw.vete);
					} else {
						Log.i(TAG, "Ugo says that the service was not bound!");
					}

				} catch (RemoteException e) {

					Log.e(TAG, e.toString());

				}
			}
		});

		//mPlayer.start() ;

		if (null != mPlayer) {

			mPlayer.setLooping(false);

			// Stop Service when music has finished playing
			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {

					// stop Service if it was started with this ID
					// Otherwise let other start commands proceed
					//stopSelf(mStartID);
					//stopService(mStartID);
				}
			});
			//mPlayer.start() ;
		}
	}

	// Bind to KeyGenerator Service
	@Override
	protected void onStart() {
		super.onStart();

		if (checkSelfPermission("course.examples.Services.KeyService.GEN_ID")
			!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{"course.examples.Services.KeyService.GEN_ID"},
					PERMISSION_REQUEST);
		}
		else {
			checkBindingAndBind();
		}
	}

	protected void checkBindingAndBind() {
		if (!mIsBound) {

			boolean b = false;
			Intent i = new Intent(KeyGenerator.class.getName());


			// UB:  Stoooopid Android API-21 no longer supports implicit intents
			// to bind to a service #@%^!@..&**!@
			// Must make intent explicit or lower target API level to 20.
			ResolveInfo info = getPackageManager().resolveService(i, 0);
			i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

			b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);

			if (b) {
				Log.i(TAG, "Ugo says bindService() succeeded!");
			} else {
				Log.i(TAG, "Ugo says bindService() failed!");
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST: {

				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// Permission granted, go ahead and display map

					checkBindingAndBind();
				}
				else {
					Toast.makeText(this, "BUMMER: No Permission :-(", Toast.LENGTH_LONG).show() ;
				}
			}
			default: {
				// do nothing
			}
		}
	}
	// Unbind from KeyGenerator Service
	@Override
	protected void onStop() {

		super.onStop();
		super.onPause();

		if (mIsBound) {
			unbindService(this.mConnection);
		}
	}

	private final ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder iservice) {

			mKeyGeneratorService = KeyGenerator.Stub.asInterface(iservice);

			mIsBound = true;

		}

		public void onServiceDisconnected(ComponentName className) {

			mKeyGeneratorService = null;

			mIsBound = false;

		}
	};

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
