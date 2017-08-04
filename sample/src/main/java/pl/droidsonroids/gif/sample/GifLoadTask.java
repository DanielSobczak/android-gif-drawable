package pl.droidsonroids.gif.sample;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class GifLoadTask extends FutureTask<ByteBuffer> {
    private static final String[] GIF_URLS = {
            "https://media.giphy.com/media/12LJ7RL7X88cUw/giphy.gif",
            "https://media.giphy.com/media/3o6vXGDGZvALd2TalW/giphy.gif",
            "https://media.giphy.com/media/cXSL9YcxPx5wA/giphy.gif",
            "https://media.giphy.com/media/NvLzDSmFEGY6c/giphy.gif",
            "https://media.giphy.com/media/5sp7yacDghUHu/giphy.gif",
    };

    private final WeakReference<HttpFragment> mFragmentReference;

    GifLoadTask(HttpFragment httpFragment, final int fragmentId) {
        super(new Callable<ByteBuffer>() {
            @Override
            public ByteBuffer call() throws Exception {
                URLConnection urlConnection = new URL(GIF_URLS[fragmentId % GIF_URLS.length]).openConnection();
                urlConnection.connect();
                final int contentLength = urlConnection.getContentLength();
                if (contentLength < 0) {
                    throw new IOException("Content-Length not present");
                }
                ByteBuffer buffer = ByteBuffer.allocateDirect(contentLength);
                ReadableByteChannel channel = Channels.newChannel(urlConnection.getInputStream());
                while (buffer.remaining() > 0)
                    channel.read(buffer);
                channel.close();
                return buffer;
            }
        });
        mFragmentReference = new WeakReference<>(httpFragment);
    }

    @Override
    protected void done() {
        final HttpFragment httpFragment = mFragmentReference.get();
        if (httpFragment == null) {
            return;
        }
        try {
            httpFragment.onGifDownloaded(get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            httpFragment.onDownloadFailed(e);
        }
    }
}
