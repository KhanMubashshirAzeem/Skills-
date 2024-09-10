package com.example.skills_plus.activity;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.skills_plus.databinding.ActivityReadBlogBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ReadBlogActivity extends AppCompatActivity {

    // View Binding for easy access to views
    private ActivityReadBlogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using View Binding
        binding = ActivityReadBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the back button functionality
        setupBackPress();

        // Retrieve blog data passed via Intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String timestamp = getIntent().getStringExtra("timestamp");

        // Populate the views with the blog data
        binding.titleBD.setText(title);
        binding.descriptionBD.setText(description);
        binding.timeStampBD.setText(timestamp);

        // Load the blog image using Glide library
        Glide.with(this).load(imageUrl).into(binding.imageBD);

        // Set up the download button to trigger PDF creation
        binding.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create PDF from the blog content layout (including scrolling content)
                createPdf(binding.blogContentLayout);
            }
        });
    }

    /**
     * Sets up the back button functionality in the toolbar.
     */
    private void setupBackPress() {
        // Elevate the toolbar for better visibility
        binding.blogDetailToolbar.setElevation(8);

        // Set a click listener to handle back navigation
        binding.blogDetailToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * Creates a PDF from the provided scrollable view and saves it to the device.
     *
     * @param scrollView The ScrollView containing the blog content to be converted into PDF.
     */
    public void createPdf(ScrollView scrollView) {
        // Measure the total height of the scrollable content
        int totalHeight = scrollView.getChildAt(0).getHeight();
        int width = scrollView.getWidth();

        // Create a new Bitmap with the entire scrollable content
        Bitmap bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);

        // Create a new PDF document
        PdfDocument document = new PdfDocument();

        // Define the page size (A4 paper size in pixels)
        int pageHeight = totalHeight; // Adjust this for different paper sizes
        int pageWidth = 1120;
        int totalPages = (int) Math.ceil((float) totalHeight / pageHeight);

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            // Create page info for each page in the document
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            // Calculate the portion of the bitmap to draw for each page
            int top = pageIndex * pageHeight;
            int bottom = Math.min(top + pageHeight, totalHeight);
            Bitmap pageBitmap = Bitmap.createBitmap(bitmap, 0, top, width, bottom - top);

            // Draw the page bitmap onto the canvas
            Canvas pageCanvas = page.getCanvas();
            pageCanvas.drawBitmap(pageBitmap, 0, 0, null);

            // Finish the page
            document.finishPage(page);
        }

        // Save the PDF document
        try {
            OutputStream outputStream;

            // Check the Android version to decide the file saving method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 (API 29) and above, use MediaStore API

                // Prepare the content values with file metadata
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Blog.pdf"); // File name
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf"); // File type
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS); // Save to Downloads folder

                // Insert the content values into MediaStore and get the URI
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                if (uri != null) {
                    // Open an output stream to the URI
                    outputStream = getContentResolver().openOutputStream(uri);
                } else {
                    throw new IOException("Failed to create new MediaStore record.");
                }

            } else {
                // For Android versions below 10, use traditional file saving

                // Get the path to the Downloads directory
                String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File file = new File(directoryPath, "Blog.pdf"); // Define the file name and path

                // Create an output stream to the file
                outputStream = new FileOutputStream(file);
            }

            // Write the PDF document to the output stream
            document.writeTo(outputStream);

            // Close the output stream
            outputStream.close();

            // Notify the user that the PDF has been successfully downloaded
            Toast.makeText(this, "PDF downloaded successfully", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            // Notify the user about the error
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the PDF document to free resources
            document.close();
        }
    }
}
