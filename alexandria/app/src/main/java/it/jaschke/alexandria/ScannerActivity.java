package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.jaschke.alexandria.services.BookService;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by Dobrunov on 12.10.2015.
 */
public class ScannerActivity extends Activity implements ZBarScannerView.ResultHandler {
    private static final String LOG_TAG = ScannerActivity.class.getSimpleName();
    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        List barcods = new ArrayList<BarcodeFormat>();
        barcods.add(BarcodeFormat.EAN13);
        mScannerView.setFormats(barcods);
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        // Do something with the result here
        Log.v(LOG_TAG, result.getContents()); // Prints scan results
        Log.v(LOG_TAG, result.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

        String ean = result.getContents();
        if(ean.length() == 10 && !ean.startsWith("978")){
            ean="978"+ean;
        }

        if(ean.startsWith("918")){
            ean="978"+ean.substring(3);
        }

//        if (ean.endsWith("X") || ean.endsWith("x"))
//            ean = ean.substring(0, ean.length() - 1) + "1";

        if(ean.length()<13){
            Log.e(LOG_TAG, getString(R.string.error_input_digits));
            Toast.makeText(this, getString(R.string.error_input_digits), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            return;
        }

        Intent intent = new Intent();
        setResult(RESULT_OK, intent.putExtra(AddBook.EAN_CODE, ean));

        Intent bookIntent = new Intent(this, BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        startService(bookIntent);

        finish();
    }
}