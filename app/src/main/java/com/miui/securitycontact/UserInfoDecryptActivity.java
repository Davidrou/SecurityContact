package com.miui.securitycontact;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by luozhanwei on 17-7-27.
 */
public class UserInfoDecryptActivity extends AppCompatActivity {
    private TextView mDebugInfo, mFingerVerifyText;
    private FingerPrintHelper mFingerPrintHelper;
    private CryptHelper mCryptHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        mDebugInfo = (TextView) findViewById(R.id.text_debug_info);
        mFingerVerifyText = (TextView) findViewById(R.id.text_confirm_finger);
        mFingerPrintHelper = new FingerPrintHelper(getApplicationContext());
        mCryptHelper = CryptHelper.getInstance(getApplicationContext());
        String telNumToCheck = "";
        Intent intent = getIntent();
        if (intent != null) {
            telNumToCheck = intent.getStringExtra("telNumToCheck");
        }
        if (telNumToCheck == null || telNumToCheck.equals("")) {
            Toast.makeText(this, "未传入电话号码", 0).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        if(!checkTelExist(telNumToCheck)){
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        mDebugInfo.setText("传入的电话为：" + telNumToCheck);
        startVerifyFinger(telNumToCheck);
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
                        new String[]{CryptHelper.getSHA256Digest(telNumToCheck)}, null);
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
}
