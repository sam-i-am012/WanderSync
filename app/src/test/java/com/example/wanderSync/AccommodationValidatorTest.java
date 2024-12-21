package com.example.wanderSync;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.wanderSync.model.Accommodation;
import com.example.wanderSync.model.AccommodationValidator;

import org.junit.Before;
import org.junit.Test;

public class AccommodationValidatorTest {
    private String checkIn;
    private String checkOut;
    private String location;
    private String hotel;
    private String locationName = "New York";
    private String hotelName = "Grand Hotel";
    private String hotelName2 = "Hotel Paradise";
    private String checkInTime = "2024-11-27 14:00";
    private String checkoutTime = "2024-11-30 11:00";
    private String roomType = "Deluxe";
    private String userId = "user123";

    @Before
    public void setUp() {
        checkIn = "";
        checkOut = "";
        location = "";
        hotel = "";
    }
    @Test
    public void emptyCheckOut() {
        // check out is empty
        checkIn = "5:00";
        checkOut = "";
        location = locationName;
        hotel = hotelName;

        boolean result = AccommodationValidator.validateInputs(checkIn, checkOut, location, hotel);

        assertFalse(result);
    }

    @Test
    public void allFieldsEmpty() {
        // all fields are empty
        checkIn = "";
        checkOut = "";
        location = "";
        hotel = "";

        boolean result = AccommodationValidator.validateInputs(checkIn, checkOut, location, hotel);

        assertFalse(result);
    }

    @Test
    public void allFieldsFilled() {
        //  all fields are filled
        checkIn = "2024-12-01";
        checkOut = "2024-12-07";
        location = locationName;
        hotel = hotelName;

        boolean result = AccommodationValidator.validateInputs(checkIn, checkOut, location, hotel);

        assertTrue(result);
    }

    @Test
    public void emptyCheckIn() {
        // check in is empty
        checkIn = "";
        checkOut = "2024-12-07";
        location = locationName;
        hotel = hotelName;

        boolean result = AccommodationValidator.validateInputs(checkIn, checkOut, location, hotel);

        assertFalse(result);
    }


    @Test
    public void testParameterizedConstructor() {
        Accommodation accommodation = new Accommodation(
                hotelName2,
                locationName,
                checkInTime,
                checkoutTime,
                2,
                roomType,
                userId
        );

        assertEquals(hotelName2, accommodation.getHotel());
        assertEquals(locationName, accommodation.getLocation());
        assertEquals(checkInTime, accommodation.getCheckInTime());
        assertEquals(checkoutTime, accommodation.getCheckOutTime());
        assertEquals(2, accommodation.getNumRooms());
        assertEquals(roomType, accommodation.getRoomType());
        assertEquals(userId, accommodation.getUserId());
    }

    @Test
    public void testGettersAndSetters() {
        Accommodation accommodation = new Accommodation();

        // Test hotel
        accommodation.setHotel(hotelName2);
        assertEquals(hotelName2, accommodation.getHotel());

        // Test location
        accommodation.setLocation(locationName);
        assertEquals(locationName, accommodation.getLocation());

        // Test check-in time
        accommodation.setCheckInTime(checkInTime);
        assertEquals(checkInTime, accommodation.getCheckInTime());

        // Test check-out time
        accommodation.setCheckOutTime(checkoutTime);
        assertEquals(checkoutTime, accommodation.getCheckOutTime());

        // Test number of rooms
        accommodation.setNumRooms(2);
        assertEquals(2, accommodation.getNumRooms());

        // Test room type
        accommodation.setRoomType(roomType);
        assertEquals(roomType, accommodation.getRoomType());

        // Test user ID
        accommodation.setUserId(userId);
        assertEquals(userId, accommodation.getUserId());

        // Test travel destination
        accommodation.setTravelDestination("California");
        assertEquals("California", accommodation.getTravelDestination());
    }
}
