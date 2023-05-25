package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private int port;
    private String currency;
    private TextView resultTextView;

    private Socket socket;

    public ClientThread(int port, String currency, TextView resultTextView) {
        this.port = port;
        this.currency = currency;
        this.resultTextView = resultTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(Constants.ADDRESS, port);
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            printWriter.println(currency);
            printWriter.flush();
            String resultInformation;
            while ((resultInformation = bufferedReader.readLine()) != null) {
                final String finalizedResultInformation = resultInformation;
                System.out.println(finalizedResultInformation);
                resultTextView.post(() ->
                        resultTextView.setText(finalizedResultInformation));
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
