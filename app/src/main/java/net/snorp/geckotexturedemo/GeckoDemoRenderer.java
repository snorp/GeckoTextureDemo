package net.snorp.geckotexturedemo;

import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.renderer.Renderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

public class GeckoDemoRenderer extends Renderer {
    private static final String LOGTAG = "GeckoDemoRenderer";

    private GeckoObject3D mGeckoObject;
    private String mInitialUrl;

    public GeckoDemoRenderer(Context context, String url) {
        super(context);
        mInitialUrl = url;
    }

    @Override
    protected void initScene() {
        getCurrentScene().setBackgroundColor(Color.BLACK);
        getCurrentScene().setFog(new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR, Color.BLACK, 40, 50));

        Plane floor = new Plane(100.0f, 100.0f, 1, 1, Vector3.Axis.Y);
        Material floorMaterial = new Material();
        floorMaterial.setColor(0x117391ff);
        Texture floorTexture = new Texture("floorTexture", R.raw.grid14);
        floorTexture.setRepeat(2.0f, 2.0f);
        try {
            floorMaterial.addTexture(floorTexture);
        } catch (ATexture.TextureException e) {
            Log.d(LOGTAG, "Failed to add floor texture!", e);
        }
        floor.setMaterial(floorMaterial);
        getCurrentScene().addChild(floor);


        mGeckoObject = new GeckoObject3D(getContext(), getViewportWidth(), getViewportHeight(), mInitialUrl);
        mGeckoObject.setPosition(0.0f, 10.0f, -2.0f);
        getCurrentScene().addChild(mGeckoObject);


        ArcballCamera arcball = new ArcballCamera(mContext, ((Activity)mContext).getWindow().getDecorView());
        arcball.setPosition(0, 10, 20);
        arcball.setTarget(mGeckoObject);
        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    public boolean handleTouchEvent(MotionEvent event) {
        return mGeckoObject.onTouchEvent(event, getCurrentCamera(), getViewportWidth(), getViewportHeight());
    }
}
