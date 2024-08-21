package com.andreacioccarelli.androoster.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings({"WeakerAccess", "RedundantStringToString", "SpellCheckingInspection"})
public class PreferencesBuilder {

    private SecurePreferences mWriter;
    private SharedPreferences.Editor SettingsWriter;
    private SharedPreferences SettingsReader;
    public Context Context;

    private String key = "hE2=pzXgGavXJuwV%$jKlaXQ=k!kx=Us";
    static private String fileName = "";
    static public String defaultFilename = "$";
    static public String Hashes = "HashedKeys";
    static public String ThemeFilename = "[[kabouzeid_app-theme-helper]]";
    static public String SettingsFilename = "com.andreacioccarelli.androoster_preferences";

    public PreferencesBuilder(Context ctx, String FileName) {
        mWriter = new SecurePreferences(ctx, FileName, key, true);
        Context = ctx;
        fileName = FileName;

        SettingsReader = ctx.getSharedPreferences(defaultFilename, android.content.Context.MODE_PRIVATE);
        SettingsWriter = ctx.getSharedPreferences(defaultFilename, android.content.Context.MODE_PRIVATE).edit();
    }

    public PreferencesBuilder(Context ctx) {
        mWriter = new SecurePreferences(ctx, defaultFilename, key, true);
        Context = ctx;
        fileName = defaultFilename;

        SettingsReader = ctx.getSharedPreferences(defaultFilename, android.content.Context.MODE_PRIVATE);
        SettingsWriter = ctx.getSharedPreferences(defaultFilename, android.content.Context.MODE_PRIVATE).edit();
    }

    public PreferencesBuilder(Context ctx, String FileName, String encryptionKey) {
        mWriter = new SecurePreferences(ctx, FileName, key, true);
        Context = ctx;
        fileName = FileName;
        key = encryptionKey;

        SettingsReader = ctx.getSharedPreferences(defaultFilename, android.content.Context.MODE_PRIVATE);
        SettingsWriter = ctx.getSharedPreferences(defaultFilename, android.content.Context.MODE_PRIVATE).edit();
    }

    public void release() {
        SettingsReader = null;
        SettingsWriter = null;
        mWriter = null;
        Context = null;
    }

    public final void putPreferenceBoolean(String key, boolean value) {
        Context.getSharedPreferences(SettingsFilename, android.content.Context.MODE_PRIVATE).edit().putBoolean(key,value).commit();
    }

    public final void putPreferenceInt(String key, int value) {
        Context.getSharedPreferences(SettingsFilename, android.content.Context.MODE_PRIVATE).edit().putInt(key,value).commit();
    }

    public final void putPreferenceString(String key, String value) {
        Context.getSharedPreferences(SettingsFilename, android.content.Context.MODE_PRIVATE).edit().putString(key,value).commit();
    }

    public final boolean getPreferenceBoolean(String key, boolean value) {
        return Context.getSharedPreferences(SettingsFilename, android.content.Context.MODE_PRIVATE).getBoolean(key,value);
    }

    public final int getPreferenceInt(String key, int value) {
        return Context.getSharedPreferences(SettingsFilename, android.content.Context.MODE_PRIVATE).getInt(key,value);
    }

    public final String getPreferenceString(String key, String value) {
        return Context.getSharedPreferences(SettingsFilename, android.content.Context.MODE_PRIVATE).getString(key,value);
    }

    public final void putThemeBoolean(String key, boolean value) {
        Context.getSharedPreferences(ThemeFilename, android.content.Context.MODE_PRIVATE).edit().putBoolean(key,value).commit();
    }

    public final void putThemeInt(String key, int value) {
        Context.getSharedPreferences(ThemeFilename, android.content.Context.MODE_PRIVATE).edit().putInt(key,value).commit();
    }

    public final void putThemeString(String key, String value) {
        Context.getSharedPreferences(ThemeFilename, android.content.Context.MODE_PRIVATE).edit().putString(key,value).commit();
    }

    public final boolean getThemeBoolean(String key, boolean value) {
        return Context.getSharedPreferences(ThemeFilename, android.content.Context.MODE_PRIVATE).getBoolean(key,value);
    }

    public final int getThemeInt(String key, int value) {
        return Context.getSharedPreferences(ThemeFilename, android.content.Context.MODE_PRIVATE).getInt(key,value);
    }

    public final String getThemeString(String key, String value) {
        return Context.getSharedPreferences(ThemeFilename, android.content.Context.MODE_PRIVATE).getString(key,value);
    }

    public final void switchContext(Context BaseContext) {
        Context = BaseContext;
    }

    public final void putBoolean(String key, boolean Value) {
        try {
            mWriter.putBoolean(key, Value);
        } catch (NullPointerException Exception) {
            Exception.printStackTrace();
        }
    }

    public final void putInt(@NonNull String key, int Value) {
        try {
            mWriter.putInt(key, Value);
        } catch (NullPointerException Exception) {
            Exception.printStackTrace();
        }
    }

    public final void putString(String key, String Value) {
        try {
            mWriter.putString(key, Value);
        } catch (NullPointerException Exception) {
            Exception.printStackTrace();
        }
    }

    public final boolean getBoolean(@NonNull String _BooleanName, boolean defaultValue) {
        return mWriter.getBoolean(_BooleanName, defaultValue);
    }

    public final int getInt(@NonNull String _IntName, int defaultValue) {
        return mWriter.getInt(_IntName, defaultValue);
    }

    @NonNull
    public final String getString(@NonNull String _StringName, @NonNull String defaultValue) {
        return mWriter.getString(_StringName, defaultValue).toString();
    }

    public final void removeBoolean(@NonNull String key) {
        mWriter.remove(key);
    }

    public final void removeInt(@NonNull String key) {
        mWriter.remove(key);
    }

    public final void removeString(@NonNull String key) {
        mWriter.remove(key);
    }

    public final void putUnencryptedBoolean(String key, boolean value) {
        try {
            SettingsWriter.putBoolean(key,value).apply();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public final void putUnencryptedInt(String key, int value) {
        try {
            SettingsWriter.putInt(key, value).apply();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public final void putUnencryptedString(String key, String value) {
        try {
            SettingsWriter.putString(key, value).apply();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public final boolean getUnencryptedBoolean(@NonNull String _BooleanName, boolean defaultValue) {
        return SettingsReader.getBoolean(_BooleanName, defaultValue);
    }

    public final int getUnencryptedInt(@NonNull String _IntName, int defaultValue) {
        return SettingsReader.getInt(_IntName, defaultValue);
    }

    @NonNull
    public final String getUnencryptedString(@NonNull String _StringName, @NonNull String defaultValue) {
        return SettingsReader.getString(_StringName, defaultValue).toString();
    }

    public final void removeUnencryptedBoolean(@NonNull String key) {
        mWriter.remove(key);
    }

    public final void removeUnencryptedInt(@NonNull String key) {
        mWriter.remove(key);
    }

    public final void removeUnencryptedString(@NonNull String key) {
        mWriter.remove(key);
    }

    public final void erasePreferences() {
        mWriter.clear();
    }

}

class SecurePreferences {

    @SuppressWarnings({"DeserializableClassInSecureContext", "SerializableClassInSecureContext"})
    private static class SecurePreferencesException extends RuntimeException {
        SecurePreferencesException(Throwable e) {
            super(e);
        }
    }

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY_HASH_TRANSFORMATION = "SHA-256";
    private static final String CHARSET = "UTF-8";

    private final boolean encryptKeys;
    private final Cipher writer;
    private final Cipher reader;
    private final Cipher KeyEncrypter;
    private final SharedPreferences MainStream;

    SecurePreferences(Context context, String preferenceName, String secureKey, boolean encryptKeys) throws SecurePreferencesException {
        try {
            writer = Cipher.getInstance(TRANSFORMATION);
            reader = Cipher.getInstance(TRANSFORMATION);
            KeyEncrypter = Cipher.getInstance(KEY_TRANSFORMATION);

            initCiphers(secureKey);

            MainStream = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);

            this.encryptKeys = encryptKeys;
        }
        catch (GeneralSecurityException | UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
    }

    private void initCiphers(String secureKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidAlgorithmParameterException {
        IvParameterSpec ivSpec = getIv();
        SecretKeySpec secretKey = getSecretKey(secureKey);

        writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        KeyEncrypter.init(Cipher.ENCRYPT_MODE, secretKey);
    }

    @NonNull
    private IvParameterSpec getIv() {
        byte[] iv = new byte[writer.getBlockSize()];
        java.lang.System.arraycopy("fldsjfodasjifudslfjdsaofshaufihadsf".getBytes(), 0, iv, 0, writer.getBlockSize());
        return new IvParameterSpec(iv);
    }

    @NonNull
    private SecretKeySpec getSecretKey(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] keyBytes = createKeyBytes(key);
        return new SecretKeySpec(keyBytes, TRANSFORMATION);
    }

    private byte[] createKeyBytes(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SECRET_KEY_HASH_TRANSFORMATION);
        md.reset();
        return md.digest(key.getBytes(CHARSET));
    }

    public final void putString(String key, String Value) {
        // Crashlytics.log(0, "PreferencesBuilder", "Setting " + key + " to " + String.valueOf(Value));
        MainStream.edit().putString(encrypt(key, KeyEncrypter), encrypt(Value, writer)).apply();
    }

    @NonNull
    public final String getString(String key, String Default) {
        if (MainStream.contains(encrypt(key, KeyEncrypter))) {
            try {
                return String.valueOf(decrypt(MainStream.getString(encrypt(key, KeyEncrypter), encrypt(String.valueOf(Default), writer))));
            } catch (SecurePreferencesException | IllegalArgumentException e) {
                e.printStackTrace();
                putString(key, Default);
                return Default;
            }
        } else {
            putString(key, Default);
            return Default;
        }
    }

    public final void putInt(String key, int Value) {
        //Crashlytics.log(0, "PreferencesBuilder", "Setting " + key + " to " + String.valueOf(Value));
        MainStream.edit().putString(encrypt(key, KeyEncrypter), encrypt(String.valueOf(Value), writer)).apply();
    }

    public final int getInt(String key, int Default) {
        if (MainStream.contains(encrypt(key, KeyEncrypter))) {
            try {
                return Integer.parseInt(decrypt(MainStream.getString(encrypt(key, KeyEncrypter), encrypt(String.valueOf(Default), writer))));
            } catch (SecurePreferencesException | IllegalArgumentException e) {
                e.printStackTrace();
                putInt(key, Default);
                return Default;
            }
        } else {
            putInt(key, Default);
            return Default;
        }
    }

    public final void putBoolean(String key, boolean Value) {
        // Crashlytics.log(0, "PreferencesBuilder", "Setting " + key + " to " + String.valueOf(Value));
        MainStream.edit().putString(encrypt(key, KeyEncrypter), encrypt(String.valueOf(Value), writer)).apply();
    }

    public final boolean getBoolean(String key, boolean Default) {
        if (MainStream.contains(encrypt(key, KeyEncrypter))) {
            try {
                return Boolean.parseBoolean(decrypt(MainStream.getString(encrypt(key, KeyEncrypter), encrypt(String.valueOf(Default), writer))));
            } catch (SecurePreferencesException | IllegalArgumentException e) {
                putBoolean(key, Default);
                return Default;
            }
        } else {
            putBoolean(key, Default);
            return Default;
        }
    }

    final void remove(String key) {
        MainStream.edit().remove(key).apply();
    }


    public final void put(String key, String value) {
        if (value == null) {
            MainStream.edit().remove(toKey(key)).apply();
        } else {
            putValue(toKey(key), value);
        }
    }

    public final boolean containsKey(String key) {
        return MainStream.contains(toKey(key));
    }

    public final void removeValue(String key) {
        MainStream.edit().remove(toKey(key)).apply();
    }

    @Nullable
    public final String getString(String key) throws SecurePreferencesException {
        if (MainStream.contains(toKey(key))) {
            String securedEncodedValue = MainStream.getString(toKey(key), "");
            return decrypt(securedEncodedValue);
        }
        return null;
    }

    final void clear() {
        MainStream.edit().clear().apply();
    }

    private String toKey(String key) {
        return encryptKeys ? encrypt(key, KeyEncrypter) : key;
    }

    private void putValue(String key, String value) throws SecurePreferencesException {
        String secureValueEncoded = encrypt(value, writer);

        MainStream.edit().putString(key, secureValueEncoded).apply();
    }

    private String encrypt(String value, Cipher writer) throws SecurePreferencesException {
        byte[] secureValue;
        try {
            secureValue = convert(writer, value.getBytes(CHARSET));
        }
        catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
        return Base64.encodeToString(secureValue, Base64.NO_WRAP);
    }

    @NonNull
    private String decrypt(String securedEncodedValue) {
        byte[] securedValue = Base64.decode(securedEncodedValue, Base64.NO_WRAP);
        byte[] value = convert(reader, securedValue);
        try {
            return new String(value, CHARSET);
        }
        catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
    }

    private static byte[] convert(Cipher cipher, byte[] bs) throws SecurePreferencesException {
        try {
            return cipher.doFinal(bs);
        }
        catch (Exception e) {
            throw new SecurePreferencesException(e);
        }
    }
}
