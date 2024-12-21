package com.example.wanderSync.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.wandersync.R;
import com.example.wanderSync.model.Accommodation;
import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.viewmodel.AccommodationViewModel;

public class AddAccommodationsDialog extends Dialog {
    private AccommodationViewModel accommodationViewModel;
    private final FirestoreSingleton firestore = FirestoreSingleton.getInstance();
    private String selectedDestinationId;

    public AddAccommodationsDialog(Context context, AccommodationViewModel accommodationViewModel,
                                   String selectedDestinationId) {
        super(context);  // Calls the Dialog constructor
        this.accommodationViewModel = accommodationViewModel;
        this.selectedDestinationId = selectedDestinationId;
    }

    @Override
    public void show() {
        super.show();  // Calls the show method of the Dialog class to display the dialog
        setContentView(R.layout.dialog_add_accommodation);

        // Set background drawable
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);

        // Find views
        EditText locationET = findViewById(R.id.editTextAccommodationLocation);
        EditText checkInTimeET = findViewById(R.id.editTextAccommodationCheckInTime);
        EditText checkOutTimeET = findViewById(R.id.editTextAccommodationCheckOutTime);
        EditText hotelNameET = findViewById(R.id.editTextHotelName);
        Spinner numberOfRoomsSpinner = findViewById(R.id.numberOfRoomsSpinner);
        Spinner roomTypeSpinner = findViewById(R.id.roomTypeSpinner);
        Button addAccommodationButton = findViewById(R.id.btnAddAccommodationDialog);

        // Handle add accommodation button click
        addAccommodationButton.setOnClickListener(view -> {
            String location = locationET.getText().toString().trim();
            String checkInTime = checkInTimeET.getText().toString().trim();
            String checkOutTime = checkOutTimeET.getText().toString().trim();
            String hotelName = hotelNameET.getText().toString().trim();
            String numRoomsStr = numberOfRoomsSpinner.getSelectedItem().toString();
            int numRooms = Integer.parseInt(numRoomsStr);
            String roomType = roomTypeSpinner.getSelectedItem().toString().trim();

            // Validate inputs
            if (validateInputs(locationET, checkInTimeET, checkOutTimeET, hotelNameET)) {
                // Create a new Accommodation object
                Accommodation newAccommodation = new Accommodation(
                        hotelName,
                        location,
                        checkInTime,
                        checkOutTime,
                        numRooms,
                        roomType,
                        firestore.getCurrentUserId()
                );

                // not part of the main constructor to pass checkstyle
                newAccommodation.setTravelDestination(selectedDestinationId);

                // Add the accommodation to the database
                accommodationViewModel.addAccommodation(newAccommodation);

                // Update the UI and clear input fields
                clearInputFields(locationET, checkInTimeET, checkOutTimeET, hotelNameET);

                // Show success message
                Toast.makeText(getContext(), "Accommodation added successfully",
                        Toast.LENGTH_SHORT).show();

                // Dismiss the dialog after adding the accommodation
                dismiss();
            }
        });
    }


    // Validate input fields
    private boolean validateInputs(EditText locationField, EditText checkInField,
                                   EditText checkOutField, EditText hotelField) {
        boolean isValid = true;

        // Check if any field is empty and show an error message
        if (TextUtils.isEmpty(locationField.getText())) {
            locationField.setError("Location is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(checkInField.getText())) {
            checkInField.setError("Check-in time is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(checkOutField.getText())) {
            checkOutField.setError("Check-out time is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(hotelField.getText())) {
            hotelField.setError("Hotel name is required");
            isValid = false;
        }
        if (isDateFormatInvalid(checkInField.getText().toString().trim())) {
            checkInField.setError("Enter YYYY-MM-DD");
            isValid = false;
        }
        if (isDateFormatInvalid(checkOutField.getText().toString().trim())) {
            checkOutField.setError("Enter YYYY-MM-DD");
            isValid = false;
        }

        return isValid;
    }

    // Clear input fields after submission
    private void clearInputFields(EditText locationET, EditText checkInET, EditText checkOutET,
                                  EditText hotelET) {
        locationET.setText("");
        checkInET.setText("");
        checkOutET.setText("");
        hotelET.setText("");
    }

    private boolean isDateFormatInvalid(String date) {
        // Regular expression to match the format YYYY-MM-DD
        String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";

        // Check if the date matches the pattern
        if (!date.matches(datePattern)) {
            return true;
        }

        // Additional validation for valid date values
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        // Check for valid month
        if (month < 1 || month > 12) {
            return true;
        }

        // Check for valid day based on the month
        switch (month) {
        case 1: case 3: case 5: case 7: case 8: case 10: case 12:
            return day < 1 || day > 31;
        case 4: case 6: case 9: case 11:
            return day < 1 || day > 30;
        case 2:
            // Leap year check
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            return day < 1 || day > (isLeapYear ? 29 : 28);
        default:
            return true;
        }
    }
}
