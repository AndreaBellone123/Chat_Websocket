package com.example.nodejschat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class MainActivity extends AppCompatActivity {
    private WebSocket webSocket; // Istanza della classe webSocket necessaria per creare la connessione
    private String SERVER_PATH = "https://testws--andreabellone.repl.co/"; // Indirizzo del server locale || replit sulla quale gira il websocket
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (checkPermessiPerReadExternalStorage() == false) ; // Controlla se l'app ha i permessi per accedere alla galleria,se il metodo ritorna false i permessi vengono richiesti per continuare
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);

        EditText editText = findViewById(R.id.editText);

        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creaSocketConnection();
                final EditText nameEdit = findViewById(R.id.editText);
                final EditText passwordEdit = findViewById(R.id.editTextp);
                JSONObject obj = new JSONObject();
                try {
                    obj.putOpt("name", nameEdit.getText().toString());
                    obj.putOpt("password", passwordEdit.getText().toString());
                    obj.putOpt("tipo", "Login");

                    webSocket.send(obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

});
        findViewById(R.id.buttonEntra).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creaSocketConnection();
                final EditText nameEdit = findViewById(R.id.editText);
                final EditText passwordEdit = findViewById(R.id.editTextp);
                JSONObject obj = new JSONObject();
                try {
                    obj.putOpt("name", nameEdit.getText().toString());
                    obj.putOpt("password", passwordEdit.getText().toString());
                    obj.putOpt("tipo", "Registrato");
                    webSocket.send(obj.toString());
                    //                 webSocket.send("{\"name\":\"".concat(nameEdit.getText().toString()).concat("\", \"password\": \"".concat(passwordEdit.getText().toString()).concat("\"}")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            });
    }

    public boolean checkPermessiPerReadExternalStorage() { // Metodo che restituisce 1 / false a seconda del risultato del metodo checkSelfPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void creaSocketConnection() {

        //OkHttpClient client = new OkHttpClient(); // Gestisce le richieste da inoltrare al webSocket
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        // Oggetto della classe richiesta che contiene le informazioni relative ai JSONObject da inoltrare al webSocket
        webSocket = client.newWebSocket(request, new MainActivity.SocketListener());
        // Istanziamo il webSockt che prende come parametro un oggetto della classe richiesta ed un socketListener

    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) { // Metodo chiamato quando il client si collega al socket
            super.onOpen(webSocket, response);

            runOnUiThread(() -> {   // Funzione chiamata da un thread che lavora in background,per poterlo utilizzare sul main thread chiamiamo la funzione sottostante
                Toast.makeText(MainActivity.this,
                        "Connessione al socket riuscita", // una volta riuscita la connessione al socket creiamo un MakeText di conferma
                        Toast.LENGTH_SHORT).show();

                // creaView(); // Ed inizializziamo la recyclerView in base al messaggio / immagine (inviato / ricevuto)
            });

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) { // Metodo chiamato quando riceviamo un messaggio in formato String
            super.onMessage(webSocket, text);

            runOnUiThread(() -> {

                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                TextView textView = findViewById(R.id.editText);
                if("Login Effettuato".equals(text)){
                    Intent intent = new Intent(getApplicationContext(), ActivityChat.class);
                    intent.putExtra("name", textView.getText().toString());
                    startActivity(intent);
                }

                if("Credenziali errate".equals(text)){

                    Toast.makeText(MainActivity.this,
                            "Credenziali  errate!", // se  la connessione  fallisce visualizziamo un  toast di errore
                            Toast.LENGTH_LONG).show();
                }

                if("Utente già presente,effettuare il logine".equals(text)){

                    Toast.makeText(MainActivity.this,
                            "Utente già presente,effettuare il login!", // se  la connessione  fallisce visualizziamo un  toast di errore
                            Toast.LENGTH_LONG).show();
                }
            });

        }
    }
}