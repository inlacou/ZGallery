package com.mzelzoghbi.zgallery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.mzelzoghbi.zgallery.Constants;
import com.mzelzoghbi.zgallery.CustomViewPager;
import com.mzelzoghbi.zgallery.OnImgClick;
import com.mzelzoghbi.zgallery.R;
import com.mzelzoghbi.zgallery.adapters.HorizontalListAdapters;
import com.mzelzoghbi.zgallery.adapters.ViewPagerAdapter;
import com.mzelzoghbi.zgallery.entities.ZColor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by mohamedzakaria on 8/11/16.
 */
public class ZGalleryActivity extends BaseActivity {
	private RelativeLayout mainLayout;
	
	CustomViewPager mViewPager;
	ViewPagerAdapter adapter;
	RecyclerView imagesHorizontalList;
	LinearLayoutManager mLayoutManager;
	HorizontalListAdapters hAdapter;
	private int currentPos;
	private ZColor bgColor;
	
	
	@Override
	protected int getResourceLayoutId() {
		return R.layout.z_activity_gallery;
	}
	
	@Override
	protected void afterInflation() {
		// init layouts
		mainLayout = findViewById(R.id.mainLayout);
		mViewPager = findViewById(R.id.pager);
		imagesHorizontalList = findViewById(R.id.imagesHorizontalList);
		
		// get intent data
		currentPos = getIntent().getIntExtra(Constants.IntentPassingParams.SELECTED_IMG_POS, 0);
		bgColor = (ZColor) getIntent().getSerializableExtra(Constants.IntentPassingParams.BG_COLOR);
		
		if (bgColor == ZColor.WHITE) {
			mainLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
		}
		
		mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		// pager adapter
		adapter = new ViewPagerAdapter(this, imageURLs, mToolbar, imagesHorizontalList);
		mViewPager.setAdapter(adapter);
		// horizontal list adaapter
		hAdapter = new HorizontalListAdapters(this, imageURLs, new OnImgClick() {
			@Override
			public void onClick(int pos) {
				mViewPager.setCurrentItem(pos, true);
			}
		});
		imagesHorizontalList.setLayoutManager(mLayoutManager);
		imagesHorizontalList.setAdapter(hAdapter);
		hAdapter.notifyDataSetChanged();
		
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			
			@Override
			public void onPageSelected(int position) {
				currentPos = position;
				imagesHorizontalList.smoothScrollToPosition(position);
				hAdapter.setSelectedItem(position);
				Log.d("currentImage", imageURLs.get(position));
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
		
		hAdapter.setSelectedItem(currentPos);
		mViewPager.setCurrentItem(currentPos);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_share, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}else if (item.getItemId() == R.id.share) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					shareImage();
				}
			}).start();
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void shareImage() {
		try {
			Bitmap bitmap = Glide.with(this).load(imageURLs.get(currentPos)).asBitmap().into(-1, -1).get();
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("image/*");
			i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
			startActivity(Intent.createChooser(i, "Share Image"));
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public Uri getLocalBitmapUri(Bitmap bmp) {
		Uri bmpUri = null;
		try {
			File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
			FileOutputStream out = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
			bmpUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bmpUri;
	}
}
