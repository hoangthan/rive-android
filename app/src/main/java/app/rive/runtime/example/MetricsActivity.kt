package app.rive.runtime.example

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Trace
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.rive.runtime.kotlin.core.Artboard
import app.rive.runtime.kotlin.core.File
import app.rive.runtime.kotlin.core.Rive
import app.rive.runtime.kotlin.renderers.RendererMetrics
import app.rive.runtime.kotlin.renderers.RendererSkia
import java.util.*


class MetricsActivity : AppCompatActivity() {
    private val containerView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<LinearLayout>(R.id.container)
    }
    private lateinit var swappyView: SwappyView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure Rive C++ is loaded.
        Rive.init()
        setContentView(R.layout.activity_metrics)

        swappyView = SwappyView(this, null)
        val density = resources.displayMetrics.density
        swappyView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (366 * density).toInt(),
        )
        swappyView.setZOrderOnTop(true)
        swappyView.holder.setFormat(PixelFormat.TRANSLUCENT)
        containerView.addView(swappyView, 0)
        // TODO: add a second view here to check that it still works.
    }
}

class SwappyView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Choreographer.FrameCallback {
    private val TAG = "SwappyView"
    private val riveRenderer = RendererSkia()
    private lateinit var file: File
    private var artboard: Artboard? = null
    private var frameMetricsListener: Window.OnFrameMetricsAvailableListener? = null

    private fun getMaybeActivity(): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    private val activity by lazy(LazyThreadSafetyMode.NONE) {
        // If this fails we have a problem.
        this.getMaybeActivity()!!
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.activity.let { activity ->
            val fileBytes = activity.resources.openRawResource(R.raw.off_road_car_blog).readBytes()
            file = File(fileBytes)
            file.firstArtboard.getInstance().let {
                artboard = it
                riveRenderer.artboard = artboard
            }
            // Attach callbacks
            holder.addCallback(this)
            Choreographer.getInstance().postFrameCallback(this)

            startFrameMetrics(activity)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun startFrameMetrics(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            frameMetricsListener = RendererMetrics(activity)
        } else {
            Log.w(
                this.TAG,
                "FrameMetrics can work only with Android SDK 24 (Nougat) and higher"
            );
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun stopFrameMetrics(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            frameMetricsListener?.let {
                activity.window.removeOnFrameMetricsAvailableListener(it)
            }
        }
    }

    override fun onDetachedFromWindow() {
        Log.d(this.TAG, "onDetachedFromWindow()")
        super.onDetachedFromWindow()
        stopFrameMetrics(activity)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(this.TAG, "surfaceCreated()")
        nStart(riveRenderer.address)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(this.TAG, "surfaceChanged(format: $format, width: $width, height: $height)")
        nSetViewport(holder.surface, riveRenderer.address)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(this.TAG, "surfaceDestroyed()")
        nStop(riveRenderer.address)
        nClearSurface()
    }

    override fun doFrame(frameTimeNanos: Long) {
        Trace.beginSection("doFrame");
        val fpsView = activity.findViewById<TextView>(R.id.fps)
        val fps = nGetAverageFps(riveRenderer.address)
        fpsView?.setText(
            java.lang.String.format(
                Locale.US,
                "Frame rate: %.1f Hz (%.2f ms)",
                fps,
                1e3f / fps
            )
        )
        Choreographer.getInstance().postFrameCallback(this)
        Trace.endSection()
    }

    private external fun nSetViewport(surface: Surface, rendererAddress: Long)
    private external fun nStart(rendererAddress: Long)
    private external fun nClearSurface()
    private external fun nStop(rendererAddress: Long)
    private external fun nGetAverageFps(rendererAddress: Long): Float
}