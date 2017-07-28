package com.miui.securitycontact;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private CryptHelper mCryptHelper;
    private EditText mNameText, mTelText, mDepartmentText, mSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNameText = (EditText) findViewById(R.id.text_name);
        mTelText = (EditText) findViewById(R.id.text_tel);
        mDepartmentText = (EditText) findViewById(R.id.text_department);
        mSearchText = (EditText) findViewById(R.id.text_tel_to_search);
        mCryptHelper =CryptHelper.getInstance(getApplicationContext());
    }

//    @TargetApi(Build.VERSION_CODES.M)
//    public void encryptText(View view){
//                mCryptHelper.makeStringEncrypted(mEditText.getText().toString());
//    }
//
//    public void readTextFromDB(View view){
//        final Cursor cursor = getContentResolver().query(ContactProvider.PersonColumns.CONTENT_URI,
//                new String[]{ContactProvider.PersonColumns.NAME, ContactProvider.PersonColumns.TEL,
//                        ContactProvider.PersonColumns.DEPARTMENT }, ContactProvider.PersonColumns.TEL_HASH+"=?",
//                new String[]{CryptHelper.getSHA256Digest("18519135866")}, null);
//        Toast.makeText(this, "请验证指纹",0).show();
//        mCryptHelper.authUserByFinger(new FingerPrintHelper.FingerprintHelperListener() {
//
//            @Override
//            public void authenticationFailed(String error) {
//                Toast.makeText(MainActivity.this, "指纹不正确", 0).show();
//            }
//
//            @Override
//            public void authenticationSucceeded(FingerprintManager.AuthenticationResult result) {
//                Toast.makeText(MainActivity.this, "指纹验证成功", 0).show();
//                while(cursor!=null && cursor.moveToNext()){
//                    Log.d("MainActivity", ""+cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.NAME))+"  "
//                            +cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.TEL))+" "
//                            +cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.DEPARTMENT)));
//                    Log.d("MainActivity", ""+mCryptHelper.decryptString(cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.NAME)))+"  "
//                            +mCryptHelper.decryptString(cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.TEL)))+" "
//                            +mCryptHelper.decryptString(cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.DEPARTMENT))));
//                }
//            }
//        });
//    }


    public void queryFromDb(View view){
        Intent intent = new Intent(this, UserInfoDecryptActivity.class);
        intent.putExtra("telNumToCheck",mSearchText.getText().toString());
        startActivityForResult(intent, 0);
    }


    public void insertToDb(View view){
        String name = mNameText.getText().toString();
        String tel = mTelText.getText().toString();
        String department = mDepartmentText.getText().toString();
        Toast.makeText(this, "name:"+name+" tel:"+tel+" department:"+department, Toast.LENGTH_SHORT).show();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactProvider.PersonColumns.NAME, name);
        contentValues.put(ContactProvider.PersonColumns.TEL, tel);
        contentValues.put(ContactProvider.PersonColumns.DEPARTMENT, department);
        getContentResolver().insert(ContactProvider.PersonColumns.CONTENT_URI,contentValues);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(this, "找不到联系人信息",Toast.LENGTH_SHORT).show();
        }else if(resultCode == Activity.RESULT_OK){
            String name = data.getStringExtra("username");
            String department = data.getStringExtra("department");
            Toast.makeText(this, "name:"+name+" department"+department,Toast.LENGTH_SHORT).show();
        }
    }

    //    public void decryptText(View view){
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        final String encryptedText = sharedPreferences.getString("pass", "");
//        Toast.makeText(this, "请验证指纹",0).show();
//        mCryptHelper.authUserByFinger(new FingerPrintHelper.FingerprintHelperListener() {
//            @Override
//            public void authenticationFailed(String error) {
//                Toast.makeText(MainActivity.this, "指纹不正确", 0).show();
//            }
//
//            @Override
//            public void authenticationSucceeded(FingerprintManager.AuthenticationResult result) {
//                Toast.makeText(MainActivity.this, "指纹验证成功", 0).show();
//                mCryptHelper.decryptString(encryptedText);
//                mCryptHelper.decryptString(encryptedText);
//                mCryptHelper.decryptString(encryptedText);
//            }
//        });
//    }


}
