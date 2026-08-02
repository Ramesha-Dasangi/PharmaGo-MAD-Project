package com.nibm.pharmagomadproject.customer.db;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupabaseStorageHelper {

    private static final String TAG = "SupabaseUpload";

    private static final String SUPABASE_URL =
            "https://hpshyyvmfxdldncfwawl.supabase.co";


    private static final String SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imhwc2h5eXZtZnhkbGRuY2Z3YXdsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM4NjE3ODYsImV4cCI6MjA5OTQzNzc4Nn0.Tl-SVFrwQggMjuCf-7b7vZxcmGCUwrk_FTiwtUy7yUw";


    public static final String BUCKET_PRESCRIPTIONS =
            "prescriptions";

    public static final String BUCKET_MEDICINES =
            "medicines";

    public static final String BUCKET_LICENSES =
            "licenses";



    public interface UploadCallback {

        void onSuccess(String publicUrl);

        void onFailure(String error);

    }



    public interface DeleteCallback {

        void onSuccess();

        void onFailure(String error);

    }



    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    private final Context context;



    public SupabaseStorageHelper(Context context){

        this.context =
                context.getApplicationContext();

    }

    // UPLOAD FILE

    public void uploadFile(
            String bucket,
            String filePath,
            Uri fileUri,
            UploadCallback callback
    ){

        executor.execute(() -> {
            try{

                Log.d(TAG, "Starting upload to bucket: " + bucket + ", path: " + filePath);

                String mimeType =
                        getMimeType(fileUri);



                if(mimeType == null){

                    mimeType = "image/jpeg";

                }

                Log.d(TAG, "Detected MIME type: " + mimeType);


                // FIX: URL encode the file path to handle special characters
                String encodedPath = encodeFilePath(filePath);

                String uploadUrl =
                        SUPABASE_URL
                                + "/storage/v1/object/"
                                + bucket
                                + "/"
                                + encodedPath;

                Log.d(TAG, "Upload URL: " + uploadUrl);

                URL url =
                        new URL(uploadUrl);

                HttpURLConnection conn =
                        (HttpURLConnection)
                                url.openConnection();

                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                // Use POST with x-upsert: true for Supabase Storage object upload
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                conn.setRequestProperty(
                        "Authorization",
                        "Bearer " + SUPABASE_ANON_KEY
                );

                conn.setRequestProperty(
                        "apikey",
                        SUPABASE_ANON_KEY
                );

                conn.setRequestProperty(
                        "Content-Type",
                        mimeType
                );

                conn.setRequestProperty(
                        "x-upsert",
                        "true"
                );

                // Read ALL bytes first to get the exact byte count
                // (ContentResolver metadata size can differ from actual bytes read)
                InputStream inputStream =
                        context.getContentResolver().openInputStream(fileUri);
                if (inputStream == null) throw new Exception("Cannot open input stream for URI");

                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = inputStream.read(buf)) != -1) bos.write(buf, 0, n);
                inputStream.close();
                byte[] fileBytes = bos.toByteArray();

                Log.d(TAG, "File bytes to upload: " + fileBytes.length);
                conn.setFixedLengthStreamingMode(fileBytes.length);

                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(fileBytes);
                outputStream.flush();
                outputStream.close();
                Log.d(TAG, "Uploaded " + fileBytes.length + " bytes");

                int responseCode =
                        conn.getResponseCode();

                Log.d(TAG, "Response code: " + responseCode);

                if(responseCode == 200 ||
                        responseCode == 201){

                    String publicUrl =
                            SUPABASE_URL
                                    + "/storage/v1/object/public/"
                                    + bucket
                                    + "/"
                                    + encodedPath;

                    String finalUrl =
                            publicUrl;

                    Log.d(TAG, "Upload successful: " + finalUrl);

                    new android.os.Handler(
                            android.os.Looper.getMainLooper()
                    ).post(() ->
                            callback.onSuccess(finalUrl)
                    );

                } else {
                    String errorBody = readErrorResponse(conn);
                    String msg = "Upload failed HTTP " + responseCode + ": " + errorBody;
                    Log.e(TAG, msg);

                    new android.os.Handler(
                            android.os.Looper.getMainLooper()
                    ).post(() ->
                            callback.onFailure(msg)
                    );
                }



                conn.disconnect();




            } catch (java.net.UnknownHostException uhe) {
                Log.e(TAG, "Network host resolution failed", uhe);
                new android.os.Handler(
                        android.os.Looper.getMainLooper()
                ).post(() ->
                        callback.onFailure(
                                "No internet connection. Please check your network and try again."
                        )
                );
            } catch(Exception e){

                Log.e(TAG, "Exception during upload", e);

                new android.os.Handler(
                        android.os.Looper.getMainLooper()
                ).post(() ->
                        callback.onFailure(
                                "Upload error: " + e.getMessage()
                        )
                );

            }



        });


    }

    // Helper method to read full error response from server
    private String readErrorResponse(HttpURLConnection conn) {
        try {
            InputStream errorStream = conn.getErrorStream();
            if (errorStream == null) errorStream = conn.getInputStream();
            if (errorStream != null) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(errorStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                return sb.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading error response", e);
        }
        return "No error details available";
    }

    // FIX: Helper method to properly encode file path
    private String encodeFilePath(String filePath) {
        try {
            // Split path and encode each part separately
            String[] parts = filePath.split("/");
            StringBuilder encoded = new StringBuilder();

            for(int i = 0; i < parts.length; i++) {
                if(i > 0) encoded.append("/");
                encoded.append(URLEncoder.encode(parts[i], "UTF-8")
                        .replace("+", "%20")); // Replace + with %20 for spaces
            }

            return encoded.toString();
        } catch (Exception e) {
            Log.w(TAG, "Error encoding file path, using original", e);
            return filePath;
        }
    }

    // Get file size using ContentResolver metadata (does NOT consume the stream)
    private long getFileSize(Uri uri) {
        try {
            android.database.Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[]{android.provider.OpenableColumns.SIZE},
                    null, null, null
            );
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                        if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                            return cursor.getLong(sizeIndex);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get file size from cursor", e);
        }
        return -1;
    }

    // PRESCRIPTION UPLOAD
    public void uploadPrescription(
            String userId,
            Uri fileUri,
            UploadCallback callback
    ){


        String filePath =
                userId
                        + "/prescription_"
                        + System.currentTimeMillis()
                        + ".jpg";



        uploadFile(
                BUCKET_PRESCRIPTIONS,
                filePath,
                fileUri,
                callback
        );

    }

    // DELETE FILE
    public void deleteFile(
            String bucket,
            String filePath,
            DeleteCallback callback
    ){


        executor.execute(() -> {


            try{


                String deleteUrl =
                        SUPABASE_URL
                                + "/storage/v1/object/"
                                + bucket
                                + "/"
                                + filePath;



                URL url =
                        new URL(deleteUrl);



                HttpURLConnection conn =
                        (HttpURLConnection)
                                url.openConnection();

                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                conn.setRequestMethod("DELETE");



                conn.setRequestProperty(
                        "Authorization",
                        "Bearer " + SUPABASE_ANON_KEY
                );



                conn.setRequestProperty(
                        "apikey",
                        SUPABASE_ANON_KEY
                );




                int responseCode =
                        conn.getResponseCode();




                if(responseCode == 200){


                    new android.os.Handler(
                            android.os.Looper.getMainLooper()
                    ).post(callback::onSuccess);


                }else{


                    new android.os.Handler(
                            android.os.Looper.getMainLooper()
                    ).post(() ->
                            callback.onFailure(
                                    "Delete failed HTTP "
                                            + responseCode
                            )
                    );

                }


                conn.disconnect();



            }catch(Exception e){

                Log.e(TAG, "Error deleting file", e);

                new android.os.Handler(
                        android.os.Looper.getMainLooper()
                ).post(() ->
                        callback.onFailure(
                                e.getMessage()
                        )
                );

            }


        });


    }

    // PUBLIC URL
    public String getPublicUrl(
            String bucket,
            String filePath
    ){

        return SUPABASE_URL
                + "/storage/v1/object/public/"
                + bucket
                + "/"
                + filePath;

    }

    // MIME TYPE
    private String getMimeType(Uri uri){


        String mimeType =
                context.getContentResolver()
                        .getType(uri);



        if(mimeType == null){


            String extension =
                    MimeTypeMap
                            .getFileExtensionFromUrl(
                                    uri.toString()
                            );


            mimeType =
                    MimeTypeMap
                            .getSingleton()
                            .getMimeTypeFromExtension(
                                    extension
                            );

        }


        return mimeType;
    }
}