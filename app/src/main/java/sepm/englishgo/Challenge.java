package sepm.englishgo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.json.GoogleJsonResponseException;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.vision.v1.Vision;
//import com.google.api.services.vision.v1.VisionRequest;
//import com.google.api.services.vision.v1.VisionRequestInitializer;
//import com.google.api.services.vision.v1.model.AnnotateImageRequest;
//import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
//import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
//import com.google.api.services.vision.v1.model.EntityAnnotation;
//import com.google.api.services.vision.v1.model.Feature;
//import com.google.api.services.vision.v1.model.Image;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Challenge extends AppCompatActivity {

    public static Word VOCABULARY;

    private static boolean correctPhoto = false;



    // API
    public static final String FILE_NAME = "temp.jpg";
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = "EnglishGO-TakePhoto";
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private static HashMap<String, Float> resultList = new HashMap();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);

        FirebaseFirestore.getInstance().collection("word")
                .whereEqualTo("topic",ChooseTopic.TOPIC)
                .whereEqualTo("level",ChooseLevel.LEVEL)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Word> wordList = new ArrayList<>();

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot document : list) {
                            wordList.add(document.toObject(Word.class));

                        }

                        Random rand = new Random();
                        Word randomWord = wordList.get(rand.nextInt(wordList.size()));

                        VOCABULARY = randomWord;

                        TextView textView = findViewById(R.id.challengeWord);
                        textView.setText(randomWord.getContent());
                    }
                });

        Button takePhoto = findViewById(R.id.challengePhoto);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
        }

    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST,
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                detectLabels(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, "Something is wrong with that image. Pick a different one please.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, "Something is wrong with that image. Pick a different one please.", Toast.LENGTH_LONG).show();
        }
    }

    public void detectLabels(Bitmap bitmap){
        /**Cloud Label Detection**/
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(20)
                        .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
        Task<List<FirebaseVisionCloudLabel>> result =
                detector
                        .detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionCloudLabel> labels) {
                                        System.out.println("hello");
                                        for (FirebaseVisionCloudLabel label: labels) {
                                            String text = label.getLabel();
                                            String entityId = label.getEntityId();
                                            float confidence = label.getConfidence();
                                            System.out.println("Text: " + text +
                                                                " EntityID: " + entityId +
                                                                " Confidence: "+ confidence);
                                            resultList.put(text, confidence);
                                        }
                                    }
                                });


        /**On-device Label Detection**/
//        FirebaseVisionLabelDetectorOptions options =
//                new FirebaseVisionLabelDetectorOptions.Builder()
//                        .setConfidenceThreshold(0.8f)
//                        .build();
//
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
//        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(options);
//        Task<List<FirebaseVisionLabel>> result =
//                detector
//                        .detectInImage(image)
//                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
//                                    @Override
//                                    public void onSuccess(List<FirebaseVisionLabel> labels) {
//                                        System.out.println("hello");
//                                        for (FirebaseVisionLabel label: labels) {
//                                            String text = label.getLabel();
//                                            String entityId = label.getEntityId();
//                                            float confidence = label.getConfidence();
//                                            System.out.println("Text: " + text +
//                                                                " EntityID: " + entityId +
//                                                                " Confidence: "+ confidence);
//                                            resultList.put(text, confidence);
//                                        }
//                                    }
//                                });


    }


    public void check(View v){
        if (this.correctPhoto ){
            Intent changeView = new Intent( Challenge.this, Result.class);
            startActivity(changeView);
        }
    }

    public void getBack(View view){
        Intent changeView = new Intent( Challenge.this, ChooseTopic.class);
        startActivity(changeView);
    }

    public void showHint (View view){
        AlertDialog.Builder hint = new AlertDialog.Builder(this);

        hint
                .setMessage(VOCABULARY.getHint())
                .setTitle("Hint")
                .create();

        hint.show();
    }



}
