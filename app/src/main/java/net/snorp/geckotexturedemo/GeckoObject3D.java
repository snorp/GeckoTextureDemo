package net.snorp.geckotexturedemo;

import org.mozilla.gecko.GeckoSession;
import org.mozilla.gecko.gfx.GeckoDisplay;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.vector.Vector2;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.util.GLU;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import java.nio.FloatBuffer;


public class GeckoObject3D extends Plane {
    private static final String LOGTAG = "GeckoObject3D";

    private int mWidth;
    private int mHeight;

    private StreamingTexture mTexture;
    private GeckoSession mSession;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public GeckoObject3D(Context context, int width, int height, String url) {
        super(width / gcm(width, height), height / gcm(width, height), 1, 1, Vector3.Axis.Z);
        mWidth = width;
        mHeight = height;
        mSession = new GeckoSession();
        init(context, url);
    }

    public static int gcm(int a, int b) {
        return b == 0 ? a : gcm(b, a % b);
    }


    private void init(final Context context, final String url) {
        if (!mSession.isOpen()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSession.openWindow(context);
                    mSession.loadUri(url);
                }
            });
        }
        mTexture = new StreamingTexture("geckoOutput", new StreamingTexture.ISurfaceListener() {
            @Override
            public void setSurface(final Surface surface) {
                final GeckoDisplay display = new GeckoDisplay() {
                    private Listener mListener;

                    @Override
                    public Listener getListener() {
                        return mListener;
                    }

                    @Override
                    public void setListener(Listener listener) {
                        mListener = listener;
                        mListener.surfaceChanged(surface, mWidth, mHeight);
                    }
                };

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTexture.getSurfaceTexture().setDefaultBufferSize(mWidth, mHeight);
                        mSession.addDisplay(display);
                    }
                });
            }
        });
        mTexture.setWidth(mWidth);
        mTexture.setHeight(mHeight);

        Material material = new Material();
        material.setColorInfluence(0);
        try {
            material.addTexture(mTexture);
        } catch (ATexture.TextureException e) {
            Log.e(LOGTAG, "Failed to set texture!", e);
        }

        setMaterial(material);
    }

    public GeckoSession getSession() {
        return mSession;
    }

    @Override
    protected void preRender() {
        super.preRender();
        mTexture.update();
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    private double remap(double fromValue, double fromMin, double fromMax, double toMin, double toMax) {
        return toMin + (fromValue - fromMin) * (toMax - toMin) / (fromMax - fromMin);
    }

    public boolean onTouchEvent(MotionEvent event, Camera camera, int viewportWidth, int viewportHeight) {
        Vector2 xy = transformInput(camera, viewportWidth, viewportHeight, event.getX(), event.getY());
        if (xy != null) {
            MotionEvent transformed = MotionEvent.obtain(event);
            transformed.setLocation((float)xy.getX(), (float)xy.getY());
            mSession.getPanZoomController().onTouchEvent(transformed);
            return true;
        }

        return false;
    }

    private Vector2 transformInput(Camera camera, int viewportWidth, int viewportHeight, float x, float y) {
        int[] viewport = new int[] { 0, 0, viewportWidth, viewportHeight };
        double[] nearPos4 = new double[4];
        GLU.gluUnProject(x, viewportHeight - y, 0, camera.getViewMatrix().getDoubleValues(), 0,
                camera.getProjectionMatrix().getDoubleValues(), 0, viewport, 0, nearPos4, 0);

        double[] farPos4 = new double[4];
        GLU.gluUnProject(x, viewportHeight - y, 1, camera.getViewMatrix().getDoubleValues(), 0,
                camera.getProjectionMatrix().getDoubleValues(), 0, viewport, 0, farPos4, 0);

        Vector3 nearPos = new Vector3();
        nearPos.setAll(nearPos4[0] / nearPos4[3], nearPos4[1]
                / nearPos4[3], nearPos4[2] / nearPos4[3]);

        Vector3 farPos = new Vector3();
        farPos.setAll(farPos4[0] / farPos4[3],
                farPos4[1] / farPos4[3], farPos4[2] / farPos4[3]);

        // Things are in world space now, convert to model space
        nearPos.multiply(getModelMatrix().clone().inverse());
        farPos.multiply(getModelMatrix().clone().inverse());

        Vector3 direction = farPos.subtract(nearPos);
        direction.normalize();

        FloatBuffer verts = getGeometry().getVertices();

        int i = 0;
        Vector3 bottomLeft = new Vector3(verts.get(i++), verts.get(i++), verts.get(i++));
        Vector3 topLeft = new Vector3(verts.get(i++), verts.get(i++), verts.get(i++));
        Vector3 bottomRight = new Vector3(verts.get(i++), verts.get(i++), verts.get(i++));
        Vector3 topRight = new Vector3(verts.get(i++), verts.get(i++), verts.get(i++));

        double t = (0 - nearPos.z) / direction.z;

        double planeX = nearPos.x + (direction.x * t);
        double planeY = nearPos.y + (direction.y * t);

        double geckoX = remap(planeX, topLeft.x, topRight.x, 0, mWidth);
        double geckoY = mHeight - remap(planeY, bottomLeft.y, topLeft.y, 0, mHeight);

        if (geckoX < 0 || geckoX > mWidth || geckoY < 0 || geckoY > mHeight) {
            return null;
        }

        return new Vector2(geckoX, geckoY);
    }
}
