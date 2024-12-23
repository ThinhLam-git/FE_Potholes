package com.example.authentication_uiux;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.authentication_uiux.ui.notifications.NotificationsFragment;

public class Profile extends AppCompatActivity {
    private TextView profileImage, profileName, profileEmail, emailText, usernameText;
    private ImageView editProfile;
    private TextView editInfo;
    private RelativeLayout rankButton, updatingDataButton, ratingAppButton, companyInfoButton;
    private View settingChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Initialize views
        initializeViews();

        //Set initial profile data
        setProfileData();

        //Set Click Listeners
        setClickListeners();

        TextView emailTextView = findViewById(R.id.profile_email);
        TextView emailTex = findViewById(R.id.email_text);

        // Lấy SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "No email found");

        // Hiển thị email trong TextView
        emailTextView.setText(savedEmail);
        emailTex.setText(savedEmail);

    }

    private void initializeViews(){
        //Profile section
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        editProfile = findViewById(R.id.edit_profile);

        //Information Section
        emailText = findViewById(R.id.email_text);
        usernameText = findViewById(R.id.username_text);
        editInfo = findViewById(R.id.edit_info);

        //Buttons
        rankButton = findViewById(R.id.rank_button);
        updatingDataButton = findViewById(R.id.updating_data_button);
        ratingAppButton = findViewById(R.id.rating_app_button);
        companyInfoButton = findViewById(R.id.company_info_button);
        settingChange = findViewById(R.id.setting_change);
    }

    private void setProfileData(){
        //Im gonna use this function with data from database after I finish all the UI
        //Now I just set the hardcoded for displaying the layout
        String username = "PTDat";


        profileName.setText(username);
        usernameText.setText(username);

        //Set first letter of username to display on the profile image
        if(!username.isEmpty()){
            profileImage.setText(String.valueOf(username.charAt(0)));
        }
    }

    private void setClickListeners(){
        editProfile.setOnClickListener(v -> {
            showImagePickerDialog();
        });

        //Edit information
        editInfo.setOnClickListener(v -> {
            showEditInfoDialog();
        });

        //rankButton
        rankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Rank.class);
                startActivity(intent);
            }
        });

        //updatingDataButton
        updatingDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, UpdatingData.class);
                startActivity(intent);
            }
        });

        //Rating App
        ratingAppButton.setOnClickListener(v -> {
            showRatingAppDialog();
        });

        //Company Information
        companyInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, AppInfo.class);
                startActivity(intent);
            }
        });

        //Setting Navigation
        settingChange.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, MainActivity.class);
            intent.putExtra("SELECTED_TAB", "setting_change");
            startActivity(intent);
            finish();
        });
    }

    private void showImagePickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture");
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        builder.setItems(options, (dialog, which) -> {
            switch (which){
                case 0:
                    //Launch camera
                    launchCamera();
                    break;
                case 1:
                    //Choose picture from gallery
                    launchGallery();
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void showEditInfoDialog(){
        //Tạo custom dialog de thay doi thong tin nguoi dung
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_info);

        EditText usernameEdit = dialog.findViewById(R.id.username_edit);
        EditText emailEdit = dialog.findViewById(R.id.email_edit);
        Button saveButton = dialog.findViewById(R.id.save_button);

        //Pre-fill current values
        usernameEdit.setText(usernameText.getText());
        emailEdit.setText(emailText.getText());
        saveButton.setOnClickListener(v -> {
            //Update profile information
            String newUsername = usernameEdit.getText().toString();
            String newEmail = emailEdit.getText().toString();

            if(validateInput(newUsername, newEmail)){
                updateProfileInfo(newUsername, newEmail);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showRatingAppDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_rating_app);

        RatingBar ratingBar = dialog.findViewById(R.id.rating_bar);
        Button submitButton = dialog.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v ->{
            float rating = ratingBar.getRating();
            //Save rating into database
            //Currently I've not created database to store my data so I'll just print the message
            Toast.makeText(this, "Thank you for rating: " + rating + "star!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean validateInput(String username, String email){
        if(username.isEmpty()){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateProfileInfo(String username, String email){
        //Update UI
        profileName.setText(username);
        profileEmail.setText(email);
        usernameText.setText(username);
        emailText.setText(email);

        //Update profile image if username has changed
        if(!username.isEmpty()){
            profileImage.setText(String.valueOf(username.charAt(0)));
        }

        //Here you probably gonna update data into database
        //But i've not got that one so Imma raise a message

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void launchCamera(){
        //Implement camera launch logic
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void launchGallery(){
        //Implement gallery launch logic
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_IMAGE_CAPTURE && data != null){
                //Handle camera image
                Bundle extras = data.getExtras();
                assert extras != null;
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                updateProfilePicture(imageBitmap);
            }else if(requestCode == REQUEST_IMAGE_PICK && data != null){
                //Handle gallery image
                Uri selectedImage = data.getData();
                try{
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    updateProfilePicture(imageBitmap);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateProfilePicture(Bitmap bitmapImage) {
        // Here you would typically:
        // 1. Upload the image to your server
        // 2. Update the local storage
        // 3. Update the UI
        Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
    }
}
