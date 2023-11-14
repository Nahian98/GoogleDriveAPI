package com.android.googledriveapi.demos

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Collections


object DriveQuickKotlin {
    /**
     * Application name.
     */
    private const val APPLICATION_NAME = "GDriveKanon"

    /**
     * Global instance of the JSON factory.
     */
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

    /**
     * Directory to store authorization tokens for this application.
     */
    private const val TOKENS_DIRECTORY_PATH = "tokens"

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES: List<String> = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY)
    private const val CREDENTIALS_FILE_PATH = "/credentials.json"

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    // Add the 'suspend' modifier to make the function coroutine-friendly
    @Throws(IOException::class)
    private suspend fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential? {
        return withContext(Dispatchers.IO) {
            try {
                // Load client secrets.
                val input: InputStream = DriveQuickstart::class.java.getResourceAsStream(
                    CREDENTIALS_FILE_PATH
                )
                    ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
                val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(input))

                // Build flow and trigger user authorization request.
                val flow = GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
                )
                    .setDataStoreFactory(FileDataStoreFactory(java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build()
                val receiver = LocalServerReceiver.Builder().setPort(8888).build()
                val credential: Credential = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
                // returns an authorized Credential object.
                credential
            } catch (e: IOException) {
                // Handle IOException, for example, log the error or throw a custom exception
                e.printStackTrace() // You might want to log this exception or handle it in another way
                null
            }
        }
    }
    fun main() = runBlocking {
        // Build a new authorized API client service.
        val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val service = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build()

        // Print the names and IDs for up to 10 files.
        val result: FileList = service.files().list()
            .setPageSize(10)
            .setFields("nextPageToken, files(id, name)")
            .execute()
        val files: List<File> = result.files
        if (files == null || files.isEmpty()) {
            println("No files found.")
        } else {
            println("Files:")
            for (file in files) {
                println("${file.name} (${file.id})")
            }
        }
    }
}