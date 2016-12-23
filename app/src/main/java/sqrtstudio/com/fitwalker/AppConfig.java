package sqrtstudio.com.fitwalker;

public final class AppConfig {

    public static final String TAG = "fitwalker";
    public static final String SP = "sqrtstudio.com.fitwalker";

    public static final String RESULTS = "results";
    public static final String STATUS = "status";

    public static final String OK = "OK";
    public static final String ZERO_RESULTS = "ZERO_RESULTS";
    public static final String REQUEST_DENIED = "REQUEST_DENIED";
    public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    //    Key for nearby places json from google
    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    public static final String ICON = "icon";
    public static final String SUPERMARKET_ID = "id";
    public static final String NAME = "name";
    public static final String PLACE_ID = "place_id";
    public static final String REFERENCE = "reference";
    public static final String VICINITY = "vicinity";
    public static final String PLACE_NAME = "place_name";

    // remember to change the browser api key
    public static final String GOOGLE_BROWSER_API_KEY =
    "AIzaSyBAYCJ8VxyGbT6YQX2QuZTHBA49_b3DsN4";
    public static final int PROXIMITY_RADIUS = 5000;
    // The minimum distance to change Updates in meters
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    public static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    //URL to our login.php file, url bisa diganti sesuai dengan alamat server kita
    public static final String ADD_URL = "http://fitwalker.esy.es/add.php";
    public static final String SELECT_URL = "http://fitwalker.esy.es/select.php";
    public static final String SBID_URL = "http://fitwalker.esy.es/sbid.php";
    public static final String UPDATE_URL = "http://fitwalker.esy.es/update.php";
    public static final String SABID_URL = "http://fitwalker.esy.es/sabid.php";


    //Keys for email and password as defined in our $_POST['key'] in login.php
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    //If server response is equal to this that means login is successful
    public static final String RES_SUCCESS = "success";
    public static final String RES_FAILED = "failed";


}
