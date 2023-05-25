package ro.pub.cs.systems.eim.practicaltest02;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Constants {

    String TAG = "[PracticalTest02]";

    boolean DEBUG = true;

    String ADDRESS = "localhost";

    String EUR = "EUR";
    String USD = "USD";

    List<String> currencies = new ArrayList<String>(Arrays.asList(EUR, USD));

    String WEB_SERVICE_ADDRESS = "https://api.coindesk.com/v1/bpi/currentprice/";
}
