package com.miui.securitycontact;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by luozhanwei on 17-7-24.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerPrintHelper extends FingerprintManager.AuthenticationCallback {
    private static final String TAG = "FingerPrintHelper";
    private Context mContext;
    private KeyguardManager mKeyguardManager;
    private FingerprintManager mFingerprintManager;

    public FingerPrintHelper( Context context){
        mContext = context;
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(mContext.KEYGUARD_SERVICE);
        mFingerprintManager = (FingerprintManager) mContext.getSystemService(mContext.FINGERPRINT_SERVICE);
    }

    public boolean testFingerPrintSettings() {
        Log.d(TAG, "Testing Fingerprint Settings");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            Log.d(TAG, "This Android version does not support fingerprint authentication.");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!mKeyguardManager.isKeyguardSecure()) {
                Log.d(TAG, "User hasn't enabled Lock Screen");
                return false;
            }
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "User hasn't granted permission to use Fingerprint");
            return false;
        }

        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            Log.d(TAG, "User hasn't registered any fingerprints");
            return false;
        }

        Log.d(TAG, "Fingerprint authentication is set.\n");

        return true;
    }

    private CancellationSignal cancellationSignal;

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject,
                          final FingerprintHelperListener listener) {
        cancellationSignal = new CancellationSignal();

        try {
            manager.authenticate(cryptoObject, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    listener.authenticationFailed("Authentication error\n" + errString);
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    listener.authenticationFailed("Authentication help\n" + helpString);
                }

                @Override
                public void onAuthenticationFailed() {
                    listener.authenticationFailed("Authentication failed.");
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    listener.authenticationSucceeded(result);
                }

            }, null);
        } catch (SecurityException ex) {
            listener.authenticationFailed("An error occurred:\n" + ex.getMessage());
        } catch (Exception ex) {
            listener.authenticationFailed("An error occurred\n" + ex.getMessage());
        }
    }

    interface FingerprintHelperListener {
        public void authenticationFailed(String error);
        public void authenticationSucceeded(FingerprintManager.AuthenticationResult result);
    }

    public FingerprintManager getFingerprintManager(){
        return mFingerprintManager;
    }
}
