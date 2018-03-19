package com.example.jameedean.loginapps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jameedean.loginapps.adapter.AgencyAdapter;
import com.example.jameedean.loginapps.data.Reference;
import com.example.jameedean.loginapps.model.NoteModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {

    private EditText mTVTitle;
    private TextView mTVAgency;
    private EditText mTVDescription;

    private DatabaseReference mReference;
    private AgencyAdapter mAdapter;
    private ArrayList<String> mKeys;

    private String mId;
    Spinner simpleSpinner;

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        //final FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Binding
        mTVTitle = findViewById(R.id.et_title);
        mTVAgency = findViewById(R.id.et_agency);
        mTVDescription = findViewById(R.id.et_description);

        mReference = FirebaseDatabase.getInstance().getReference(mCurrentUser.getUid()).child(Reference.DB_NOTES);
        mReference = FirebaseDatabase.getInstance().getReference(mCurrentUser.getUid()).child(Reference.DB_AGENCY);

        Intent intent = getIntent();
        // Load record
        if(intent != null) {
            mId = intent.getStringExtra(Reference.NOTE_ID);
            if (mId != null) {
                mReference.child(mId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        NoteModel model = dataSnapshot.getValue(NoteModel.class);
                        if (model != null) {
                            mTVTitle.setText(model.getTitle());
                            mTVDescription.setText(model.getDescription());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
        mReference = FirebaseDatabase.getInstance().getReference(mCurrentUser.getUid()).child(Reference.DB_AGENCY);
                // listening for changes
                mReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final List<String> simpleList = new ArrayList<String>();
                        // load data
                        for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                            String name = noteSnapshot.child("name").getValue(String.class);
                            simpleList.add(name);
                        }
                        simpleSpinner = (Spinner) findViewById(R.id.simple_spinner);
                        ArrayAdapter<String> simpleAdapter = new ArrayAdapter<String>(NoteActivity.this, android.R.layout.simple_spinner_dropdown_item, simpleList);
                        simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        simpleSpinner.setAdapter(simpleAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // stop listening
                    }

                });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_delete);

        if(mId == null) {
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

                // What to do when save
                NoteModel model = new NoteModel(
                        mTVTitle.getText().toString(),
                        mTVAgency.getText().toString(),
                        mTVDescription.getText().toString(),
                        System.currentTimeMillis()
                );

                save(model, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        actionNotification(databaseError, R.string.done_saved);
                    }
                });
                break;
            case R.id.action_delete:
                if(!mId.isEmpty()) {
                    mReference.child(mId).removeValue(new DatabaseReference.CompletionListener() {
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
     * Save record to firebase
     * @param model
     */
    private void save(NoteModel model,
                      DatabaseReference.CompletionListener listener) {

        if(mId == null) {
            // generate id
            mId = mReference.push().getKey();
        }

        mReference.child(mId).setValue(model, listener);
    }

    private void actionNotification(DatabaseError error, int successResourceId) {
        // close activity
        if(error == null) {
            Toast.makeText(NoteActivity.this, successResourceId, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(NoteActivity.this, error.getCode(), Toast.LENGTH_SHORT).show();
        }
    }

}
