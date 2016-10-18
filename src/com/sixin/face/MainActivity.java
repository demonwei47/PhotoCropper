package com.sixin.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	private ImageView imageView;
	public String[] menu = new String[] { "相册", "相机" };
	public String TAG = "face";
    public final static int REQUEST_CAMERA =1;
    public final static int REQUEST_GALLERY = 2;
    public final static int REQUEST_CROP = 3;
	private Uri imguri;
	private File file;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initPath();
		initView();
	}

	private void initPath() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log.e(TAG,"有sd卡");
		}else {
			Toast.makeText(MainActivity.this, "没有sd卡", 0).show();
			finish();
		}
		
		file = new File(Environment.getExternalStorageDirectory()+File.separator+getPackageName());
		if (!file.exists()) {
			file.mkdirs();
		}
		
		imguri = null;
	}

	private void initView() {
		imageView=(ImageView) findViewById(R.id.iv);
		imageView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv:
			final Intent intent = new Intent();
			imguri=Uri.fromFile(new File(file,getHeadPictureName()));
			new AlertDialog.Builder(MainActivity.this).setTitle("进行操作").setItems(menu, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if ("相册".equals(menu[which]) ) {
						Log.e(TAG,"相册");
						
				        intent.setAction(Intent.ACTION_GET_CONTENT);
				        intent.setType("image/*");
				        
				        startActivityForResult(intent, REQUEST_GALLERY);
						
					}else if ("相机".equals(menu[which])) {
						Log.e(TAG,"相机");
						intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT,imguri);
				        startActivityForResult(intent,REQUEST_CAMERA);
						
					}
				}
			}).setNegativeButton("取消", null).show();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode==RESULT_OK) {
			Intent intent = new Intent("com.android.camera.action.CROP");
			
			switch (requestCode) {
			case REQUEST_CAMERA:
				intent.setDataAndType(imguri,"image/*");
				CropPic(intent);
				break;

			case REQUEST_GALLERY:
				Log.e(TAG,"相册 "+data.getData());
				if (data.getData().toString().startsWith("content://com.android.providers.media.documents")) {
					String path=PathUtils.getPath(MainActivity.this, data.getData());
					Log.e(TAG,"4.4 path "+path);
					intent.setDataAndType(Uri.fromFile(new File(path)),"image/*");
				}else {
					Log.e(TAG,"path "+data.getData());
					intent.setDataAndType(data.getData(),"image/*");
				}
				
				CropPic(intent);
				break;
				
			case REQUEST_CROP:
				Log.e(TAG,"crop ");
				//Toast.makeText(MainActivity.this, ""+imguri, 1).show();
				
				Bitmap bitmap;
				try {
					bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(imguri));
					imageView.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					Toast.makeText(MainActivity.this, "剪裁失败 请重试", 1).show();
					e.printStackTrace();
				}
                
				break;
				
			default:
				break;
			}
			
			
			
		}
		
	}
	
	private void CropPic(Intent intent){
		intent.putExtra("crop","true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 600);
        intent.putExtra("outputY", 600);
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imguri);
        startActivityForResult(intent,REQUEST_CROP);
	}
	

    public String getHeadPictureName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }
}
