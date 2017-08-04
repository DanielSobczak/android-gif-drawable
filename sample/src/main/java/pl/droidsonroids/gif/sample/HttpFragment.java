package pl.droidsonroids.gif.sample;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class HttpFragment extends BaseFragment implements View.OnClickListener {

    private static final String KEY_ID = "key_id";
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private GifImageView mGifImageView;

    public static HttpFragment createInstance(int id) {
        HttpFragment httpFragment = new HttpFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_ID, id);
        httpFragment.setArguments(args);
        return httpFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (container != null) {
                Snackbar.make(container, R.string.gif_texture_view_stub_api_level, Snackbar.LENGTH_LONG).show();
            }
            return null;
        } else {
            mGifImageView = (GifImageView) inflater.inflate(R.layout.http, container, false);
            downloadGif();
            return mGifImageView;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !mGifImageView.isHardwareAccelerated()) {
            Snackbar.make(mGifImageView, R.string.gif_texture_view_stub_acceleration, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        mExecutorService.shutdown();
        super.onDestroy();
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void onGifDownloaded(ByteBuffer buffer) {
        try {
            mGifImageView.setImageDrawable(new GifDrawable(buffer));
        } catch (IOException e) {
            Log.e("GIF", "GifDrawable error: ", e);
        }
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void onDownloadFailed(Exception e) {
        mGifImageView.setOnClickListener(HttpFragment.this);
        if (isDetached()) {
            return;
        }
        final String message = getString(R.string.gif_texture_view_loading_failed, e.getMessage());
        Snackbar.make(mGifImageView, message, Snackbar.LENGTH_LONG).show();

    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void downloadGif() {
        mExecutorService.submit(new GifLoadTask(this, getFragmentId()));
    }

    private int getFragmentId() {
        return getArguments().getInt(KEY_ID);
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onClick(View v) {
        downloadGif();
    }
}
