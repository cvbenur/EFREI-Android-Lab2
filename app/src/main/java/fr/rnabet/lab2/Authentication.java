package fr.rnabet.lab2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Authentication extends AppCompatActivity {

    // Attributes
    private EditText authLogin;
    private EditText authPwd;
    private TextView authResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);


        // Retrieve layout item references on Activity creation
        this.authLogin = (EditText) findViewById(R.id.auth_login);
        this.authPwd = (EditText) findViewById(R.id.auth_pwd);
        this.authResult = (TextView) findViewById(R.id.auth_result);
    }


    // Inner class AuthRunnable which implements Runnable
    class AuthRunnable implements Runnable {

        // Attributes
        private String login;
        private String pwd;

        private String res;


        public String getRes () {
            return this.res;
        }


        // Ctor
        public AuthRunnable (String login, String pwd) {
            this.login = login;
            this.pwd = pwd;
        }


        @Override
        public void run() {
            URL url = null;

            try {
                url = new URL("https://httpbin.org/basic-auth/bob/sympa");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                String basicAuth = "Basic " + Base64.encodeToString(
                        String.format("%s:%s", login, pwd).getBytes(),
                        Base64.NO_WRAP
                );

                urlConnection.setRequestProperty ("Authorization", basicAuth);

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readStream(in);
                    Log.i("JFL", s);

                    // Destructuring recieved JSON object and casting "authenticated" field to Boolean
                    res = Boolean.toString(new JSONObject(s).getBoolean("authenticated"));

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Click handler for "Authenticate" button
    public void authenticate (View view) throws InterruptedException {
        AuthRunnable r = new AuthRunnable(authLogin.getText().toString(), authPwd.getText().toString());
        Thread authThread = new Thread(r);

        authThread.start();     // Launch AuthThread with provided credentials as parameters
        authThread.join();      // Wait for auth to finish

        // Print query result on Activity
        authResult.setText(r.getRes());
    }


    // readStream() implementation to turn recieved bytes to parsable String
    private String readStream (InputStream in) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = in.read();
            while(i != -1) {
                bo.write(i);
                i = in.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
}