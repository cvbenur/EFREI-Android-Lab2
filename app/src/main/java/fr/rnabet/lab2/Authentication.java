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

    private EditText authLogin;
    private EditText authPwd;
    private TextView authResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);


        this.authLogin = findViewById(R.id.auth_login);
        this.authPwd = findViewById(R.id.auth_pwd);
        this.authResult = findViewById(R.id.auth_result);
    }


    class AuthRunnable implements Runnable {

        private String login;
        private String pwd;

        private String res;


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

    public void authenticate (View view) throws InterruptedException {
        AuthRunnable r = new AuthRunnable(authLogin.getText().toString(), authPwd.getText().toString());
        Thread authThread = new Thread(r);
        authThread.start();
        authThread.join();

        authResult.setText(r.res);
    }

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