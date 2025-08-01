package net.afterday.compas.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import net.afterday.compas.R;
import net.afterday.compas.engine.events.CodeInputEventBus;
import net.afterday.compas.util.OnSwipeTouchListener;

import java.util.List;

/**
 * Created by Justas Spakauskas on 3/24/2018.
 */

public class ScannerFragment extends DialogFragment {
    private static final String TAG = "ScannerFragment";
    private View v;
    private DecoratedBarcodeView dbw;
    private int currentCam;
    private boolean flashOn;

    public static ScannerFragment newInstance() {
        //Log.d(TAG, "ScannerFragment: newInstance");
        return new ScannerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle);
        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Nullable
    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //Observable.timer(300, TimeUnit.SECONDS).take(1).subscribe((t) -> closePopup(v));
        v = inflater.inflate(R.layout.scanner_fragment, container, false);
        v.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()) {
            @Override
            public void onSwipeLeft() {
                changeCamera();
            }

            @Override
            public void onSwipeRight() {
                changeCamera();
            }

            @Override
            public void onSwipeUp() {
                toggleFlashLight();
            }

            @Override
            public void onSwipeDown() {
                toggleFlashLight();
            }
        });
        // Assign close button
        v.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    closePopup(v);
                } catch (Exception e) {
                }

            }
        });
        scanQr();
        //scanQr(savedInstanceState);


        //Log.d("SCANNER", "onCreateView");

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        v.setOnTouchListener(null);
    }

    public void closePopup(View view) {
        dismiss();
    }

    private void toggleFlashLight() {
        if (flashOn) {
            dbw.setTorchOff();
            flashOn = false;
        } else {
            dbw.setTorchOn();
            flashOn = true;
        }
//        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
//        {
//            if(cam == null)
//            {
//                cam = Camera.open();
//                Camera.Parameters p = cam.getParameters();
//                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//                cam.setParameters(p);
//                cam.startPreview();
//            }else
//            {
//                cam.stopPreview();
//                cam.release();
//                cam = null;
//            }
//        }
    }

    private void changeCamera() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (currentCam == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                currentCam = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                currentCam = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
        } else {
            if (currentCam == CameraCharacteristics.LENS_FACING_FRONT) {
                currentCam = CameraCharacteristics.LENS_FACING_BACK;
            } else {
                currentCam = CameraCharacteristics.LENS_FACING_FRONT;
            }
        }
        dbw.pause();
        dbw.getBarcodeView().setCameraSettings(getCameraSettings(currentCam));
        dbw.resume();
    }

    private void scanQr() {
        dbw = (DecoratedBarcodeView) v.findViewById(R.id.zxing_barcode_scanner);
        dbw.setStatusText(null);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            currentCam = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else {
            currentCam = CameraCharacteristics.LENS_FACING_FRONT;
        }
        dbw.getBarcodeView().setCameraSettings(getCameraSettings(currentCam));
        dbw.getBarcodeView().decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                Log.d(TAG, "ItemScanned: " + result.getText());
                CodeInputEventBus.codeScanned(result.getText());
                //ItemEventsBus.instance().addItem(result.getText());
                //net.afterday.compas.logging.Logger.d("Barcode scanned: " + result.getText());
                dbw.pause();
                getActivity().onBackPressed();
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                //Log.e(TAG, "Possible result points: " + resultPoints);
            }
        });
//        CaptureManager capture = new CaptureManager(getActivity(), mDecoratedBarcodeView);
//        capture.initializeFromIntent(getActivity().getIntent(), savedInstanceState);
//        capture.decode();
    }

    private CameraSettings getCameraSettings(int cameraType){
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CameraSettings cs = new CameraSettings();
            int cameraId = 0;
            int defaultId = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    defaultId = camIdx;
                }
                if (cameraInfo.facing == cameraType) {
                    cameraId = camIdx;
                    break;
                }
            }
            cs.setRequestedCameraId(cameraId > 0 ? cameraId : defaultId);
            return cs;
        } else {
            CameraSettings cs = new CameraSettings();
            int defaultId = 0;
            int cameraId = 0;
            CameraManager cameraManager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds;
            try {
                cameraIds = cameraManager.getCameraIdList();
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
            for (String camId : cameraIds) {
                CameraCharacteristics characteristics;
                try {
                    characteristics = cameraManager.getCameraCharacteristics(camId);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    defaultId = Integer.parseInt(camId);
                }
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == cameraType) {
                    cameraId = Integer.parseInt(camId);
                    break;
                }
            }
            cs.setRequestedCameraId(cameraId > 0 ? cameraId : defaultId);
            return cs;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        getDialog().getWindow().setLayout(width, height);
        dbw.getBarcodeView().resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        dbw.getBarcodeView().pause();
    }
}
