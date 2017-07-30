package com.miui.securitycontact;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;

/**
 * Created by luozhanwei on 17-7-27.
 */
public class UserInfoDecryptActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CONFRIM_FINGER_WHEN_INSERT = 1;
    private static final int REQUEST_CODE_CONFRIM_FINGER = 2;
    private TextView mDebugInfo, mFingerVerifyText;
    private FingerPrintHelper mFingerPrintHelper;
    private CryptHelper mCryptHelper;
    private String mTelNumToCheck="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        mDebugInfo = (TextView) findViewById(R.id.text_debug_info);
        mFingerVerifyText = (TextView) findViewById(R.id.text_confirm_finger);
        mFingerPrintHelper = new FingerPrintHelper(getApplicationContext());
        try {
            mCryptHelper = CryptHelper.getInstance(getApplicationContext());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            if(e instanceof UserNotAuthenticatedException) {
                Toast.makeText(this, "请先验证指纹", 0).show();
                Intent intent =new Intent();
                ComponentName componentName =new ComponentName("com.android.settings",
                        "com.android.settings.MiuiConfirmCommonPassword");
                intent.setComponent(componentName);
                intent.putExtra("businessId","security_core_add");
                intent.putExtra("com.android.settings.userIdToConfirm",0);
                startActivityForResult(intent, REQUEST_CODE_CONFRIM_FINGER);
                return;
            }
        }
        Intent intent = getIntent();
        if (intent != null) {
            mTelNumToCheck = intent.getStringExtra("telNumToCheck");
        }
        mTelNumToCheck = "18519135866";
        if (mTelNumToCheck==null || mTelNumToCheck.equals("")) {
            Toast.makeText(this, "未传入电话号码", 0).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        if(!checkTelExist(mTelNumToCheck)){
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        if(!mCryptHelper.checkIfNeedAuth()){
            final Cursor cursor = getContentResolver().query(ContactProvider.PersonColumns.CONTENT_URI,
                    new String[]{ContactProvider.PersonColumns.NAME, ContactProvider.PersonColumns.TEL,
                            ContactProvider.PersonColumns.DEPARTMENT}, ContactProvider.PersonColumns.TEL_HASH + "=?",
                    new String[]{CryptHelper.getSHA256Digest(mTelNumToCheck)}, null);
            while (cursor != null && cursor.moveToNext()) {
                Person cipherPerson = new Person(cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.NAME)),
                        cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.TEL)),
                        cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.DEPARTMENT)));
                Toast.makeText(UserInfoDecryptActivity.this, "解密成功 ： username:" + mCryptHelper.decryptString(cipherPerson.getmName()) +
                        " department:" + mCryptHelper.decryptString(cipherPerson.getmDepartment()), 0).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("username", mCryptHelper.decryptString(cipherPerson.getmName()));
                resultIntent.putExtra("department", mCryptHelper.decryptString(cipherPerson.getmDepartment()));
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
            return;
        }
        mDebugInfo.setText("传入的电话为：" + mTelNumToCheck);
        startVerifyFinger(mTelNumToCheck);
    }

    private boolean checkTelExist(String telNumToCheck) {
        Cursor cursor = getContentResolver().query(ContactProvider.PersonColumns.CONTENT_URI,
                new String[]{ContactProvider.PersonColumns.NAME}, ContactProvider.PersonColumns.TEL_HASH + "=?",
                new String[]{CryptHelper.getSHA256Digest(telNumToCheck)}, null);
        if(cursor == null || !cursor.moveToNext()){
            return false;
        }
        return true;
    }

    private void startVerifyFinger(final String telNumToCheck) {

        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.android.settings",
                "com.android.settings.MiuiConfirmCommonPassword");
        intent.setComponent(componentName);
        intent.putExtra("businessId", "security_core_add");
        intent.putExtra("com.android.settings.userIdToConfirm", 0);
        startActivityForResult(intent, REQUEST_CODE_CONFRIM_FINGER_WHEN_INSERT);
        Toast.makeText(this, "需要验证指纹", Toast.LENGTH_SHORT).show();
        mFingerPrintHelper.startAuth(mFingerPrintHelper.getFingerprintManager(), null, new FingerPrintHelper.FingerprintHelperListener() {
            @Override
            public void authenticationFailed(String error) {
                mFingerVerifyText.setText("指纹验证失败，请重新验证");
            }

            @Override
            public void authenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                mFingerVerifyText.setText("指纹验证成功，正在解密");
                final Cursor cursor = getContentResolver().query(ContactProvider.PersonColumns.CONTENT_URI,
                        new String[]{ContactProvider.PersonColumns.NAME, ContactProvider.PersonColumns.TEL,
                                ContactProvider.PersonColumns.DEPARTMENT}, ContactProvider.PersonColumns.TEL_HASH + "=?",
                        new String[]{CryptHelper.getSHA256Digest(mTelNumToCheck)}, null);
                while (cursor != null && cursor.moveToNext()) {
                    Person cipherPerson = new Person(cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.NAME)),
                            cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.TEL)),
                            cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.DEPARTMENT)));
                    Toast.makeText(UserInfoDecryptActivity.this, "解密成功 ： username:" + mCryptHelper.decryptString(cipherPerson.getmName()) +
                            " department:" + mCryptHelper.decryptString(cipherPerson.getmDepartment()), 0).show();
                    Intent intent = new Intent();
                    intent.putExtra("username", mCryptHelper.decryptString(cipherPerson.getmName()));
                    intent.putExtra("department", mCryptHelper.decryptString(cipherPerson.getmDepartment()));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_CONFRIM_FINGER_WHEN_INSERT) {
            if (resultCode == Activity.RESULT_OK) {
                final Cursor cursor = getContentResolver().query(ContactProvider.PersonColumns.CONTENT_URI,
                        new String[]{ContactProvider.PersonColumns.NAME, ContactProvider.PersonColumns.TEL,
                                ContactProvider.PersonColumns.DEPARTMENT}, ContactProvider.PersonColumns.TEL_HASH + "=?",
                        new String[]{CryptHelper.getSHA256Digest(mTelNumToCheck)}, null);
                while (cursor != null && cursor.moveToNext()) {
                    Person cipherPerson = new Person(cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.NAME)),
                            cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.TEL)),
                            cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.DEPARTMENT)));
                    Toast.makeText(UserInfoDecryptActivity.this, "解密成功 ： username:" + mCryptHelper.decryptString(cipherPerson.getmName()) +
                            " department:" + mCryptHelper.decryptString(cipherPerson.getmDepartment()), 0).show();
                    Intent intent = new Intent();
                    intent.putExtra("username", mCryptHelper.decryptString(cipherPerson.getmName()));
                    intent.putExtra("department", mCryptHelper.decryptString(cipherPerson.getmDepartment()));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }else {
                finish();
            }
        }else if(requestCode == REQUEST_CODE_CONFRIM_FINGER){
            if(resultCode == Activity.RESULT_OK){
                try {
                    mCryptHelper = CryptHelper.getInstance(this);
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                    Intent intent = getIntent();
                    if (intent != null) {
                        mTelNumToCheck = intent.getStringExtra("telNumToCheck");
                    }
                    mTelNumToCheck = "18519135866";
                    if (mTelNumToCheck==null || mTelNumToCheck.equals("")) {
                        Toast.makeText(this, "未传入电话号码", 0).show();
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                        return;
                    }
                    if(!checkTelExist(mTelNumToCheck)){
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                        return;
                    }
                    if(!mCryptHelper.checkIfNeedAuth()){
                        final Cursor cursor = getContentResolver().query(ContactProvider.PersonColumns.CONTENT_URI,
                                new String[]{ContactProvider.PersonColumns.NAME, ContactProvider.PersonColumns.TEL,
                                        ContactProvider.PersonColumns.DEPARTMENT}, ContactProvider.PersonColumns.TEL_HASH + "=?",
                                new String[]{CryptHelper.getSHA256Digest(mTelNumToCheck)}, null);
                        while (cursor != null && cursor.moveToNext()) {
                            Person cipherPerson = new Person(cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.NAME)),
                                    cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.TEL)),
                                    cursor.getString(cursor.getColumnIndex(ContactProvider.PersonColumns.DEPARTMENT)));
                            Toast.makeText(UserInfoDecryptActivity.this, "解密成功 ： username:" + mCryptHelper.decryptString(cipherPerson.getmName()) +
                                    " department:" + mCryptHelper.decryptString(cipherPerson.getmDepartment()), 0).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("username", mCryptHelper.decryptString(cipherPerson.getmName()));
                            resultIntent.putExtra("department", mCryptHelper.decryptString(cipherPerson.getmDepartment()));
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                        return;
                    }
                    mDebugInfo.setText("传入的电话为：" + mTelNumToCheck);
                    startVerifyFinger(mTelNumToCheck);
                }
            }
        }
    }
}

