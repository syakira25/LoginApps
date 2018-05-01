package com.example.jameedean.loginapps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import kivaaz.com.signaturelibrary.Signature.Signature.SignatureView;

public class SignatureActivity extends AppCompatActivity {

    private SignatureView mDrawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        mDrawingView = findViewById(R.id.signatureview);
        Button fABAccept = findViewById(R.id.bt_send);
        Button fABCancel = findViewById(R.id.bt_clear);
        // FloatingActionButton fABCancel = findViewById(R.id.fab_cancel);

        setupSignature();

        fABAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignatureActivity.this, NoteActivity.class);
                intent.putExtra("sign_image", compressSign(mDrawingView.getCanvasBitmap()));
                setResult(RESULT_OK, intent);
                finish();
            }
        });

       fABCancel.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick (View view){
            //setResult(RESULT_CANCELED);
            // finish();

            if (mDrawingView != null) {
                mDrawingView.clearCanvas();// Clear Canvas
            }
        }
    });
}

    private void setupSignature() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mDrawingView.setCanvasColor(Color.WHITE);
        mDrawingView.setPenColor(Color.BLACK);
        mDrawingView.setStrokeWidth(10);
        mDrawingView.setupDrawing(metrics);
    }

    private byte[] compressSign(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        return stream.toByteArray();
    }
}
