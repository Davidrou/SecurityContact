package com.miui.securitycontact;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by luozhanwei on 17-7-24.
 */
public class CryptHelper {
    private static final String TAG = "CryptHelper";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "miui_security_contacts";
    private static final String PREFERENCES_KEY_IV = "iv";
    private static final String PREFERENCES_KEY_PASS = "pass";
    private KeyStore mKeyStore;
    private Cipher mEncryptCipher, mDecryptCipher;
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private FingerPrintHelper mFingerPrintHelper;
    private String mStringToEncrypt;
    private SecretKey mSecretKey;
    private static CryptHelper sInstance;

    private CryptHelper(Context context) throws InvalidKeyException {
        mContext = context;
        mFingerPrintHelper = new FingerPrintHelper(mContext);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mKeyStore = getKeyStore();
        mSecretKey = getSecretKey();
        initEncryptCipher();
        initDecryptCipher();
    }

    public static CryptHelper getInstance(Context context) throws InvalidKeyException {
        if (sInstance == null) {
            sInstance = new CryptHelper(context);
        }
        return sInstance;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initEncryptCipher() throws  InvalidKeyException{
        try {
            mEncryptCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_ECB + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            mEncryptCipher.init(Cipher.ENCRYPT_MODE, mSecretKey);
        }  catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initDecryptCipher(){
        try {
            mDecryptCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_ECB + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            mDecryptCipher.init(Cipher.DECRYPT_MODE, mSecretKey);
        }catch (UserNotAuthenticatedException e){
            Log.e(TAG, e.getMessage(), e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public String makeStringEncrypted(String rawString) throws IllegalBlockSizeException{
        mStringToEncrypt = rawString;
        return  encryptString(mStringToEncrypt);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public String authUserByFinger(FingerPrintHelper.FingerprintHelperListener listener) {
        try {
            mFingerPrintHelper.startAuth(mFingerPrintHelper.getFingerprintManager(), null, listener);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    private KeyStore getKeyStore() {
        Log.d(TAG, "Getting keystore...");
        try {
            mKeyStore = KeyStore.getInstance(KEYSTORE);
            mKeyStore.load(null); // Create empty keystore
            return mKeyStore;
        } catch (KeyStoreException e) {
            Log.d(TAG, e.getMessage(), e);
        } catch (CertificateException e) {
            Log.d(TAG, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage(), e);
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public SecretKey getSecretKey() {
        try {
           //mKeyStore.deleteEntry(KEY_ALIAS);
            if (!mKeyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator mGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
                mGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                .setRandomizedEncryptionRequired(false)
                                .setUserAuthenticationRequired(true)
                                .setUserAuthenticationValidityDurationSeconds(30)
                                .build()
                );
                Log.d(TAG, "Creating new key...");
                SecretKey secretKey = mGenerator.generateKey();
                Log.d(TAG, "Key created.");
                return secretKey;
            } else {
                Log.d(TAG, "Key exists.");
                mKeyStore.load(null);
                return (SecretKey) mKeyStore.getKey(KEY_ALIAS, null);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            return null;
        }

    }

    private String encryptString(String initialText) throws IllegalBlockSizeException {
        try {
            byte[] bytes = mEncryptCipher.doFinal(initialText.getBytes());
            String encryptedText = Base64.encodeToString(bytes, Base64.NO_WRAP);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(PREFERENCES_KEY_PASS, encryptedText);
            editor.commit();
            Log.d(TAG, "initialText:" + initialText + " encryptedText:" + encryptedText);
            return encryptedText;
        }
        catch (BadPaddingException e) {
            Log.d(TAG, e.getMessage(), e);
        }
        return null;
    }

    public String decryptString(String cipherText) {
        try {
            byte[] bytes = Base64.decode(cipherText, Base64.NO_WRAP);
            String finalText = new String(mDecryptCipher.doFinal(bytes));
            Log.d(TAG, "cipherText:" + cipherText + " finalText:" + finalText);
            return finalText;
        } catch (BadPaddingException e) {
            Log.d(TAG, e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            Log.d(TAG, e.getMessage(), e);
        }
        return "";

    }

    public static String getSHA256Digest(String string){
        Log.e(TAG, "getSHA256Digest fot "+string);
        byte[] input = string.getBytes();
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(input);
            byte [] out = sha.digest();
            String result = Base64.encodeToString(out, Base64.DEFAULT);
            Log.e(TAG, "getSHA256Digest fot "+result);
            return  result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "getSHA256Digest error" );
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onAuthenticated() throws InvalidKeyException {
        initDecryptCipher();
        initEncryptCipher();
    }

    public boolean checkIfNeedAuth(){
        try {
            makeStringEncrypted("test");
            return false;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return true;
        }
    }
}
