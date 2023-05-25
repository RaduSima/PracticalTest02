package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class ServerThread extends Thread {

    private int port;
    private ServerSocket serverSocket = null;

    private HashMap<String, TimeAndValue> data;

    Timer timer = new Timer();

    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        this.data = new HashMap<>();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public synchronized void setData(String currency, TimeAndValue timeAndValue) {
        this.data.put(currency, timeAndValue);
    }

    public synchronized HashMap<String, TimeAndValue> getData() {
        return data;
    }

    @Override
    public void run() {
        timer.schedule( new TimerTask() {
            public void run() {
                Log.i(Constants.TAG, "[SERVER THREAD] Caching...");
                for (String currency : Constants.currencies) {
                    HttpClient httpClient = new DefaultHttpClient();
                    String pageSourceCode = "";

                    System.out.println(Constants.WEB_SERVICE_ADDRESS + currency + ".json");
                    HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + currency + ".json");
                    HttpResponse httpGetResponse;
                    try {
                        httpGetResponse = httpClient.execute(httpGet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    HttpEntity httpGetEntity = httpGetResponse.getEntity();
                    if (httpGetEntity != null) {
                        try {
                            pageSourceCode = EntityUtils.toString(httpGetEntity);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    JSONObject content;
                    try {
                        content = new JSONObject(pageSourceCode);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        String time = content.getJSONObject("time").getString("updated");
                        String value = content.getJSONObject("bpi").getJSONObject(currency).getString("rate");
                        setData(currency, new TimeAndValue(time, value));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 0, 2 * 1000);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();
            }
        } catch (ClientProtocolException clientProtocolException) {
           Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + clientProtocolException.getMessage());
            if (Constants.DEBUG) {
               clientProtocolException.printStackTrace();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }

        timer.cancel();
    }

    public void stopThread() {
        timer.cancel();
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
