package webScraper;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Firestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class Firebase {

    private static FileStore db;

    /**
     * Initializes Firebase with the provided service account key file.
     *
     * @param serviceAccountKeyPath Path to the service account key file.
     * @throws IOException If an error occurs while reading the service account key file.
     */
    public static void initialize(String serviceAccountKeyPath) throws IOException {
        FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId("happybibimbap-ba0a1") // Set your project ID here
                .build();

        FirebaseApp.initializeApp(options);
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Uploads data to Firestore.
     *
     * @param collectionName The name of the collection to upload the data to.
     * @param documentId     The ID of the document to upload the data to.
     * @param data           The data to upload.
     */
    public static void uploadData(String collectionName, String documentId, Map<String, Object> data) {
        DocumentReference docRef = db.collection(collectionName).document(documentId);

        ApiFuture<WriteResult> result = docRef.set(data);

        try {
            System.out.println("Update time : " + result.get().getUpdateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Firestore getDb() {
        return db;
    }
}