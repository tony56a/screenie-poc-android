package com.example.cameratest.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cameratest.app.R;
import com.example.cameratest.app.util.Constants;
import com.example.cameratest.app.view.CameraPreview;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CameraFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CameraPreview mPreview;
    private Button mTakeImageBtn;
    private ImageView mImageView;

    private Camera mCamera;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance(/*String param1, String param2*/) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnValue = inflater.inflate(R.layout.fragment_camera, container, false);
        mCamera = openCamera();
        mPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout previewView = (FrameLayout) returnValue.findViewById(R.id.preview_image);
        previewView.addView(mPreview);
        mImageView = (ImageView)returnValue.findViewById(R.id.result);
        mTakeImageBtn = (Button) returnValue.findViewById(R.id.cap_btn);
        mTakeImageBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera != null){
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            mCamera.takePicture(null,null,mCallback);
                        }
                    });
                }
            }
        });

        return returnValue;

    }

    @Override
    public void onPause(){
        super.onPause();
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onResume(){
        if(mCamera == null){
            mCamera = openCamera();
            mPreview = new CameraPreview(getActivity(), mCamera);
            FrameLayout previewView = (FrameLayout) getView().findViewById(R.id.preview_image);
            previewView.addView(mPreview);
        }else{
            mPreview.getHolder().addCallback(mPreview);
        }
        mCamera.startPreview();
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private Camera.PictureCallback mCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap inputBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
            Bitmap outputBitmap = Bitmap.createScaledBitmap(inputBitmap,264,176,false);
            inputBitmap.recycle();
            inputBitmap = null;
            new ImageProcessTask(outputBitmap,getActivity(),mImageView).execute();
            mCamera.startPreview();
        }
    };

    private Camera openCamera(){
            Camera camera = null;
            try {
                camera = Camera.open(); // attempt to get a Camera instance
            }
            catch (Exception e){
                // Camera is not available (in use or does not exist)
            }
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setRotation(90);
            parameters.setPictureSize(320,240);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);

        return camera; // returns null if camera is unavailable
    }


    private class ImageProcessTask extends AsyncTask<Void, Void, Void> {

        private Bitmap mImage;
        private ImageView mResult;
        private Context mContext;

        public ImageProcessTask(Bitmap bitmap, Context context,ImageView image){
            mImage = bitmap;
            mContext = context;
            mResult = image;
        }

        private void convertToGreyScale(){
            int h = mImage.getHeight();
            int w = mImage.getWidth();

            for (int i =0; i<h; i++){
                for (int j = 0; j< w; j++){
                   int pixel =  mImage.getPixel(j, i);
                   int gray = (int)(Color.red(pixel) * 0.21 + Color.green(pixel) * 0.79 + Color.blue(pixel) * 0.07);
                   gray = gray > 255 ? 255 : gray;
                   int returnColor = Color.argb(0xff,gray,gray,gray );
                   mImage.setPixel(j,i,returnColor);
                }
            }
        }

        private void dither(){
            int h = mImage.getHeight();
            int w = mImage.getWidth();
            int adjustedError;
            int sidePixel;
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int pixel = mImage.getPixel(j, i);
                    int newPixel = Color.red(pixel) > 127 ? Color.WHITE : Color.BLACK;
                    mImage.setPixel(j, i, newPixel);
                    int error = Color.red(pixel) - Color.red(newPixel);
                    if(j < w-1){
                        sidePixel = mImage.getPixel(j+1,i);
                        adjustedError = (int) (Color.red(sidePixel) + ((double)7/(double)16)*error);
                        adjustedError = adjustedError > 255 ? 255 : adjustedError;
                        mImage.setPixel(j+1,i,Color.rgb(adjustedError,adjustedError,adjustedError));
                    }
                    if(i<h-1){
                        if(j>0){
                            sidePixel = mImage.getPixel(j-1,i+1);
                            adjustedError = (int) (Color.red(sidePixel) + ((double)3/(double)16)*error);
                            mImage.setPixel(j-1,i+1,Color.rgb(adjustedError,adjustedError,adjustedError));
                        }
                        sidePixel = mImage.getPixel(j,i+1);
                        adjustedError = (int) (Color.red(sidePixel) + ((double)5/(double)16)*error);
                        mImage.setPixel(j,i+1,Color.rgb(adjustedError,adjustedError,adjustedError));
                        if(j < w-1){
                            sidePixel = mImage.getPixel(j+1,i+1);
                            adjustedError = (int) (Color.red(sidePixel) + ((double)1/(double)16)*error);
                            mImage.setPixel(j+1,i+1,Color.rgb(adjustedError,adjustedError,adjustedError));
                        }
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(Void... urls) {
            convertToGreyScale();
            dither();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Toast.makeText(getActivity().getApplicationContext(),mImage.getHeight()+" "+mImage.getWidth(),Toast.LENGTH_SHORT).show();
            mResult.setImageBitmap(mImage);
        }
    }
}
