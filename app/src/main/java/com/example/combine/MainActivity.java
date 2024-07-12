package com.example.combine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button cameraButton1;
    private Button cameraButton;
    private final static int REQUEST_IMAGE_CAPTURE = 123;
    private final static int REQUEST_CAMERA_CAPPTURE = 124;
    private FirebaseVisionImage image;
    private FirebaseVisionTextRecognizer textRecognizer;
    private FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        cameraButton1 = findViewById(R.id.camera_button1);

        cameraButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        cameraButton = findViewById(R.id.camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureintent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureintent, REQUEST_CAMERA_CAPPTURE);
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras1 = data.getExtras();
            Bitmap bitmap1 = (Bitmap) extras1.get("data");
            detectface(bitmap1);
        }
        if (requestCode == REQUEST_CAMERA_CAPPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");

            recognizeMyText(bitmap);
        }
    }
    private void detectface(Bitmap bitmap1) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.5f)
                        .setTrackingEnabled(true)
                        .build();


        try {
            image = FirebaseVisionImage.fromBitmap(bitmap1);
            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                String resultText = "";
                int i = 1;
                for (FirebaseVisionFace face : firebaseVisionFaces) {
                    resultText = resultText.concat("\n" + i + ".")
                            .concat("\nSmile : " + face.getSmilingProbability() * 100 + "%")
                            .concat("\nLeftEye : " + face.getLeftEyeOpenProbability() * 100 + "%")
                            .concat("\nRightEye : " + face.getRightEyeOpenProbability() * 100 + "%")
                            .concat("\n" + face.getBoundingBox());


                    i++;
                }
                if (firebaseVisionFaces.size() == 0) {
                    Toast.makeText(MainActivity.this, "No Faces", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(LCOTaceDetection.RESULT_TEXT, resultText);
                    DialogFragment resultDialog = new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(), LCOTaceDetection.RESULT_DIALOG);
                }
            }
        });


    }

    private void recognizeMyText(Bitmap bitmap) {

        try {
            image = FirebaseVisionImage.fromBitmap(bitmap);
            textRecognizer = FirebaseVision
                    .getInstance()
                    .getOnDeviceTextRecognizer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String resultText = firebaseVisionText.getText();

                        if(resultText.isEmpty()){
                            Toast.makeText(MainActivity.this,"NO TEXT RECOGNIZED",Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent;
                            intent = new Intent(MainActivity.this,ResultActivity.class);
                            intent.putExtra(LCOTaceDetection.RESULT_TEXT,resultText);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
