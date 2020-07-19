package io.github.parthav46.httphandler;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

public class HttpRequestCallback implements LoaderManager.LoaderCallbacks<String> {
    Context context;
    HttpResponseCallback callback;

    public HttpRequestCallback (Context context, HttpResponseCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        assert args != null;
        return new HttpRequestAsyncLoader(this.context, args.getString("url", null), args.getString("data", null), HttpRequestAsyncLoader.Request.POST);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        callback.onResponse(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
