package com.abbyy.mobile.sample.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.IRecognitionCoreAPI;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.sample.Helpers.PathHelper;
import com.abbyy.mobile.sample.R;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends Activity {

	private int PICK_IMAGE_REQUEST = 1;
	private int ENABLE_READ_EXTERNAL_STORAGE = 1;
	private int ON_BACK_PRESSED_AT_BEGINNING = 0;

	private String license = "49ABAD1F8FE6D01472AD7732B7D0848B";

	private String picturePath = "";

	private Uri uri = null;

	public static Boolean imageSelected = false;

	/*Success screen*/
	private TextView successRecognition;

	/* ABBY */
	private static final String licenseFileName = "AbbyyRtrSdk.license";

	private Engine engine;

	private Language[] languages = {
		Language.ChineseSimplified,
		Language.ChineseTraditional,
		Language.English,
		Language.French,
		Language.German,
		Language.Italian,
		Language.Japanese,
		Language.Korean,
		Language.Polish,
		Language.PortugueseBrazilian,
		Language.Russian,
		Language.Spanish,
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		successRecognition = (TextView) findViewById(R.layout.activity_success_recognition);

		setImagePicker();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(imageSelected){
			imageSelected = false;

			setImagePicker();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode != ON_BACK_PRESSED_AT_BEGINNING){

			if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
				uri = data.getData();
			}

			picturePath = PathHelper.getPath(MainActivity.this,uri);

			checkPermissionReadMemory();

			new ImageRecognition().execute();

//			Intent intent = new Intent(MainActivity.this, );


//			if(barcode != null) {
//				sendApprovedData();
//			}
//			else{
//				sendDisapprovedData();
//			}
		}
		else {
			onBackPressed();
		}
	}

	private void setImagePicker(){

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
	}

	private void checkPermissionReadMemory(){
		// For Android 6.0 (API level 23) and later, you need to request permissions at runtime.
		if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) !=  PackageManager.PERMISSION_GRANTED) {

			if (!ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
				ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, ENABLE_READ_EXTERNAL_STORAGE);
			}
		}
	}

	//Recognize the selected image and retrieves a string
	private class ImageRecognition extends AsyncTask<Void,Void,String>{

		@Override
		protected String doInBackground(Void... voids) {

			List<String> recognitionMessages = new ArrayList<>();
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

			IRecognitionCoreAPI.TextBlock[] textBlocks = engine.createRecognitionCoreAPI().recognizeText(bitmap, new IRecognitionCoreAPI.TextRecognitionCallback() {
				@Override
				public boolean onProgress(int i, IRecognitionCoreAPI.Warning warning) {
					return false;
				}

				@Override
				public void onTextOrientationDetected(int i) {

				}

				@Override
				public void onError(Exception e) {

				}
			});

				//Convert TextBlock[] result from the conversion to string
				for (IRecognitionCoreAPI.TextBlock textBlock: textBlocks ) {
					for (IRecognitionCoreAPI.TextLine textLine: textBlock.TextLines) {
						recognitionMessages.add(textLine.Text);
					}
				}
				return concatStringsWSep(recognitionMessages,"\n");
			}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			successRecognition.setText(s);
		}

		private String concatStringsWSep(Iterable<String> strings, String separator) {
			StringBuilder sb = new StringBuilder();
			String sep = "";
			for(String s: strings) {
				sb.append(sep).append(s);
				sep = separator;
			}
			return sb.toString();
		}
	}
}