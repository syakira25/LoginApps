package com.example.jameedean.loginapps;

import android.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.database.Cursor;
import android.net.Uri;

import com.example.jameedean.loginapps.adapter.AgencyAdapter;
import com.example.jameedean.loginapps.data.Reference;
import com.example.jameedean.loginapps.model.NoteModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {

    private EditText mTVTitle;
    private TextView mTVAgency;
    private EditText mTVDescription;
    private ImageView imageView, drawView;
    Button Send;
    String title, agency, description;
    Uri URI = null;
    private Uri DrawURL;
    private Uri mImageUri;

    private static final int CAMERA_REQUEST_CODE = 10;
    private static final int SIGNATURE_REQUEST_CODE = 11;
    private DatabaseReference mNoteReference, mAgencyReference;
    private AgencyAdapter mAdapter;
    private ArrayList<String> mKeys;
    private Bitmap mCameraImg;
    private Bitmap mDrawImg;

    private boolean mHaveDrawing,mHaveImage;

    private String mId;
    Spinner simpleSpinner;

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseStorage mStorage;

    private ArrayAdapter<String> mAgencyAdapter;

    private NoteModel mCurrentNoteModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();

        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Binding
        mTVTitle = findViewById(R.id.et_title);
        mTVAgency = findViewById(R.id.et_agency);
        mTVDescription = findViewById(R.id.et_description);
        Send = findViewById(R.id.bt_send);

        drawView = findViewById(R.id.iv_draw);
        drawView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteActivity.this, SignatureActivity.class);
                startActivityForResult(intent, SIGNATURE_REQUEST_CODE);
            }
        });

        imageView = findViewById(R.id.cameraImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkForPermission(android.Manifest.permission.CAMERA) || !checkForPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) || !checkForPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermission(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
                } else {
                    takePicture();
                }

            }
        });

        initializeSpinner();

        mNoteReference = FirebaseDatabase.getInstance().getReference(mCurrentUser.getUid()).child(Reference.DB_NOTES);

        Intent intent = getIntent();
        // Load record
        if (intent != null) {
            mId = intent.getStringExtra(Reference.NOTE_ID);
            if (mId != null) {
                mNoteReference.child(mId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mCurrentNoteModel = dataSnapshot.getValue(NoteModel.class);
                        if (mCurrentNoteModel != null) {
                            mTVTitle.setText(mCurrentNoteModel.getTitle());
                            mTVDescription.setText(mCurrentNoteModel.getDescription());

                            if (!mCurrentNoteModel.getImageUrl().isEmpty()) {
                                Picasso.with(getApplicationContext()).load(mCurrentNoteModel.getImageUrl()).into(drawView);
                                mHaveDrawing = true;
                            }else if (!mCurrentNoteModel.getPictureUrl().isEmpty()){
                                Picasso.with(getApplicationContext()).load(mCurrentNoteModel.getPictureUrl()).into(imageView);
                                mHaveImage=true;
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        mAgencyReference = FirebaseDatabase.getInstance().getReference(mCurrentUser.getUid()).child(Reference.DB_AGENCY);
        mAgencyAdapter = new ArrayAdapter<String>(NoteActivity.this, android.R.layout.simple_spinner_item);
        simpleSpinner.setAdapter(mAgencyAdapter);

        // listening for changes
        mAgencyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    String name = noteSnapshot.child("email").getValue(String.class);
                    mAgencyAdapter.add(name);
                }

                if (mCurrentNoteModel != null) {
                    if (!mCurrentNoteModel.getAgency().isEmpty()) {
                        int index = mAgencyAdapter.getPosition(mCurrentNoteModel.getAgency());
                        simpleSpinner.setSelection(index);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // stop listening
            }

        });

        //send button listener
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_delete);

        if (mId == null) {
            item.setEnabled(false);
            item.setVisible(false);
        } else {
            item.setEnabled(true);
            item.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                break;
            case R.id.action_delete:
                if (!mId.isEmpty()) {
                    mNoteReference.child(mId).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            actionNotification(databaseError, R.string.done_deleted);
                        }
                    });
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /***
     * Initialize agency into spinner list
     */
    private void initializeSpinner() {

        simpleSpinner = findViewById(R.id.simple_spinner);
        simpleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                mTVAgency.setText("Selected : " + selectedItemText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void actionNotification(DatabaseError error, int successResourceId) {
        // close activity
        if (error == null) {
            Toast.makeText(NoteActivity.this, successResourceId, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(NoteActivity.this, error.getCode(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("LongLogTag")
    public void sendEmail() {
        try {
            title = mTVTitle.getText().toString();
            agency = simpleSpinner.getSelectedItem().toString();
            description = mTVDescription.getText().toString();
            final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{agency});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
            /*if (mImageUri != null) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, mImageUri);
            }*/
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, description);
            this.startActivity(Intent.createChooser(emailIntent, "Sending email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Request failed try again: ", Toast.LENGTH_LONG).show();
        }
    }

    private void takePicture() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);//zero can be replaced with any action code
    }

    private void requestPermission(final String[] permission, final int requestCode) {

        final Activity activity = this;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage("Allow Permissions For Camera and Storage")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Request permission
                        ActivityCompat.requestPermissions(activity,
                                permission,
                                requestCode);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length <= 0) {
            Log.i("PERMISSION REQUEST", "User interaction was cancelled.");
        } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePicture();
        } else {
            // Permission denied.
            corePermissionDenied();
        }
    }

    private void corePermissionDenied() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage("Unable to get Permission")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private boolean checkForPermission(String permission) {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    getImageFromCamera(data);
                    mHaveImage=true;
                    break;
                case SIGNATURE_REQUEST_CODE:
                    byte[] bytes = data.getByteArrayExtra("sign_image");
                    mDrawImg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (mDrawImg != null) {
                        drawView.setImageBitmap(mDrawImg);
                    }

                mHaveDrawing = true;
                    break;

            }
        }
    }

    private void getImageFromCamera(Intent data) {
        mCameraImg = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mCameraImg.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
            mImageUri = Uri.fromFile(destination);
            imageView.setImageBitmap(mCameraImg);
//            uploadRecepient();

        } catch (FileNotFoundException e) {
            Log.d("FileNotFoundException: ", e.getMessage());
        } catch (IOException e) {
            Log.d("IOException: ", e.getMessage());
        }
    }

    /***
     * Trigger save button
     */
    private void save() {

                // SIGNATURE
                if (mDrawImg != null) {
                    uploadDrawing();
                }

                // PICTURE
               else if (mCameraImg != null){
                   uploadImage();
                }
    }


    private void uploadDrawing() {

        if (!mHaveDrawing) {
            saveToDB("");
        } else {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mDrawImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            OnSuccessListener successListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    saveToDB(taskSnapshot.getDownloadUrl().toString());
                }
            };

            OnFailureListener failureListener = new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            mStorage
                    .getReference("reference")
                    .child(System.currentTimeMillis() + ".jpg")
                    .putBytes(data)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener);
        }
    }

    private void uploadImage(){

        if (!mHaveImage) {
            savetoDB("");
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mCameraImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            OnSuccessListener successListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    savetoDB(taskSnapshot.getDownloadUrl().toString());

                }
            };
            OnFailureListener failureListener = new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            mStorage
                    .getReference("reference")
                    .child(System.currentTimeMillis() + ".jpg")
                    .putBytes(data)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener);
        }

    }

    /***
     * Save to Database
     * @param imageUrl
     */
    private void saveToDB(String imageUrl) {

        NoteModel model = new NoteModel(
                mTVTitle.getText().toString(),
                simpleSpinner.getSelectedItem().toString(),
                mTVDescription.getText().toString(),
                imageUrl,
                "",
                System.currentTimeMillis()
        );

        if (mId == null) {
            // generate id
            mId = mNoteReference.push().getKey();
        }

        mNoteReference.child(mId)
                .setValue(model)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(NoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void savetoDB(String drawUrl){

        NoteModel model = new NoteModel(
                mTVTitle.getText().toString(),
                simpleSpinner.getSelectedItem().toString(),
                mTVDescription.getText().toString(),
                "",
                drawUrl,
                System.currentTimeMillis()
        );

        if (mId == null) {
            // generate id
            mId = mNoteReference.push().getKey();
        }

        mNoteReference.child(mId)
                .setValue(model)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(NoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }
}

