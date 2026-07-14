package com.nibm.pharmagomadproject.customer.db;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SupabaseStorageHelper
 *
 * Supabase Storage use cases in PharmaGo:
 *   1. Prescription images  → bucket: "prescriptions"
 *   2. Medicine images      → bucket: "medicines"     (pharmacy owner upload)
 *   3. License documents    → bucket: "licenses"      (pharmacy/rider register)
 *
 * Firestore is still used for all structured data (users, orders, medicines metadata).
 * Only image/file storage goes through Supabase.
 */
public class SupabaseStorageHelper {

    // Replace these with Supabase project values
    private static final String SUPABASE_URL    = "https://YOUR_PROJECT_ID.supabase.com";
    private static final String SUPABASE_ANON_KEY = "YOUR_ANON_PUBLIC_KEY";

    // Bucket names
    public static final String BUCKET_PRESCRIPTIONS = "prescriptions";
    public static final String BUCKET_MEDICINES      = "medicines";
    public static final String BUCKET_LICENSES       = "licenses";

    // Callback interfaces
    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onFailure(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Context         context;

    public SupabaseStorageHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    // UPLOAD IMAGE / FILE

    /**
     * Upload a file from Uri to Supabase Storage.
     *
     * @param bucket   bucket name (BUCKET_PRESCRIPTIONS / BUCKET_MEDICINES / BUCKET_LICENSES)
     * @param filePath path inside bucket e.g. "userId/orderId.jpg"
     * @param fileUri  local file Uri (from gallery / camera)
     * @param callback success → public URL, failure → error message
     */
    public void uploadFile(String bucket, String filePath,
                           Uri fileUri, UploadCallback callback) {
        executor.execute(() -> {
            try {
                // Get MIME type
                String ext      = getMimeType(fileUri);
                String mimeType = ext != null ? ext : "image/jpeg";

                // Build Supabase Storage URL
                // POST /storage/v1/object/{bucket}/{filePath}
                String uploadUrl = SUPABASE_URL
                        + "/storage/v1/object/"
                        + bucket + "/"
                        + filePath;

                URL url = new URL(uploadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                conn.setRequestProperty("Content-Type",  mimeType);
                conn.setRequestProperty("x-upsert",      "true"); // overwrite if exists
                conn.setConnectTimeout(30_000);
                conn.setReadTimeout(60_000);

                // Write file bytes
                InputStream  inputStream  = context.getContentResolver().openInputStream(fileUri);
                OutputStream outputStream = conn.getOutputStream();
                byte[] buffer = new byte[4096];
                int    bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                if (responseCode == 200 || responseCode == 201) {
                    // Build public URL
                    String publicUrl = SUPABASE_URL
                            + "/storage/v1/object/public/"
                            + bucket + "/"
                            + filePath;
                    // Callback on main thread
                    android.os.Handler mainHandler =
                            new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onSuccess(publicUrl));
                } else {
                    final int code = responseCode;
                    android.os.Handler mainHandler =
                            new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onFailure("Upload failed: HTTP " + code));
                }

            } catch (Exception e) {
                android.os.Handler mainHandler =
                        new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onFailure(e.getMessage()));
            }
        });
    }

    // DELETE FILE

    public void deleteFile(String bucket, String filePath, DeleteCallback callback) {
        executor.execute(() -> {
            try {
                String deleteUrl = SUPABASE_URL
                        + "/storage/v1/object/"
                        + bucket + "/"
                        + filePath;

                URL url = new URL(deleteUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                conn.setConnectTimeout(15_000);

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                android.os.Handler mainHandler =
                        new android.os.Handler(android.os.Looper.getMainLooper());
                if (responseCode == 200) {
                    mainHandler.post(callback::onSuccess);
                } else {
                    final int code = responseCode;
                    mainHandler.post(() -> callback.onFailure("Delete failed: HTTP " + code));
                }
            } catch (Exception e) {
                android.os.Handler mainHandler =
                        new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onFailure(e.getMessage()));
            }
        });
    }

    // GET PUBLIC URL (no upload needed)

    public String getPublicUrl(String bucket, String filePath) {
        return SUPABASE_URL
                + "/storage/v1/object/public/"
                + bucket + "/"
                + filePath;
    }

    // Helpers
    private String getMimeType(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }
}