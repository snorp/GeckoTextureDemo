package net.snorp.geckotexturedemo;

import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity implements View.OnTouchListener {
    private static final String DEFAULT_URL = "https://mozilla.org";
    private GeckoDemoRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SurfaceView view = new SurfaceView(this);
        view.setFrameRate(60.0);
        view.setRenderMode(ISurface.RENDERMODE_CONTINUOUSLY);

        mRenderer = new GeckoDemoRenderer(this, urlFromIntent(getIntent()));
        view.setSurfaceRenderer(mRenderer);
        view.setOnTouchListener(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);


        setContentView(view);
    }

    private String urlFromIntent(Intent intent) {
        if (intent == null) {
            return DEFAULT_URL;
        }

        Uri uri = intent.getData();
        if (uri == null) {
            return DEFAULT_URL;
        }

        return uri.toString();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return mRenderer.handleTouchEvent(motionEvent);
    }
}
