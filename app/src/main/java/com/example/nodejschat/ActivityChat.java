package com.example.nodejschat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ActivityChat extends AppCompatActivity implements TextWatcher {

    private String name;
    private WebSocket webSocket; // Istanza della classe webSocket necessaria per creare la connessione
    private String SERVER_PATH = "https://testws--andreabellone.repl.co/"; // Indirizzo del server locale || replit sulla quale gira il websocket
    private EditText messageEdit;
    private View sendBtn, pickImgBtn;
    private RecyclerView recyclerView;
    private int IMAGE_REQUEST_ID = 1;
    private AdapterMessaggi messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        name = getIntent().getStringExtra("name"); // All'avvio della chatactivity prendiamo come intent il nome dei partecipanti
        creaSocketConnection(); // Ed effettuiamo la connessione al websocket locale

    }

    // Connessione al WebSocket

    private void creaSocketConnection() {

        OkHttpClient client = new OkHttpClient(); // Gestisce le richieste da inoltrare al webSocket
        Request request = new Request.Builder().url(SERVER_PATH).build(); // Oggetto della classe richiesta che contiene le informazioni relative ai JSONObject da inoltrare al webSocket
        webSocket = client.newWebSocket(request, new SocketListener()); // Istanziamo il webSockt che prende come parametro un oggetto della classe richiesta ed un socketListener

    }

    // Metodi della EditText per gestirla in base ai cambiamenti di testo al suo interno

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }


    @Override
    public void afterTextChanged(Editable s) {

        String string = s.toString().trim(); // Prende la string attuale e ne rimuove gli spazi

        if (string.isEmpty()) {

            reimpostaMessageEdit(); // Se il text edit non ha stringhe all'interno lo rendiamo invisibile e resettiamo il suo TextChangedListener
        }
            else {  // Altrimenti rendiamo invisibile il bottone per le immagini.

            sendBtn.setVisibility(View.VISIBLE);
            pickImgBtn.setVisibility(View.INVISIBLE);

        }
    }

    private void reimpostaMessageEdit() {

        messageEdit.removeTextChangedListener(this); // Rimuoviamo il listener per i cambiamenti del testo al suo interno
        messageEdit.setText(""); // resettiamo la textEdit ad una string vuota
        sendBtn.setVisibility(View.INVISIBLE);// e la rendiamo invisibile
        pickImgBtn.setVisibility(View.VISIBLE); // evidenziando il 'bottone' scegli immagine
        messageEdit.addTextChangedListener(this); // ed in seguito aggiungiamo nuovamente il listener del testo

    }

    private void creaView() { // Una volta effettuata la connessione al socket possiamo creare la recyclerView

        messageEdit = findViewById(R.id.messageEdit);
        sendBtn = findViewById(R.id.sendBtn);
        pickImgBtn = findViewById(R.id.pickImgBtn);
        recyclerView = findViewById(R.id.recyclerView);
        messageAdapter = new AdapterMessaggi(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageEdit.addTextChangedListener(this);

        sendBtn.setOnClickListener(v -> {

            JSONObject jsonObject = new JSONObject();
            try { // Questa operazione potrebbe lanciare un'eccezione di tipo Json Exception, va quindi messa in un blocco Try/Catch
                jsonObject.put("name", name);
                jsonObject.put("message", messageEdit.getText().toString());

                webSocket.send(jsonObject.toString());  // Inviamo il messaggio al server

                jsonObject.put("inviato", true); // Booleano utilizzato per mostrare il messaggio nella recycler view come inviato / ricevuto
                messageAdapter.addItem(jsonObject);

                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1); // Una volta ricevuto un nuovo messaggio,scrolliamo automaticamente alla sua posizione

               reimpostaMessageEdit(); // Una volta inviato il messaggio resettiamo il contenuto della EditText

            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        pickImgBtn.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // Permette al bottone di aprire la galleria del telefono per selezionare l'immagine da inviare
            intent.setType("image/*"); // L'intent è di tipo immagine( * qualsiasi formato)

            startActivityForResult(Intent.createChooser(intent, "Scegli immagine"),
                    IMAGE_REQUEST_ID);

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_ID && resultCode == RESULT_OK) { // Se il dato da inviare è un'immagine

            try {
                InputStream is = getContentResolver().openInputStream(data.getData());// creiamo un oggetto InputStream per prenderla(formato array di byte[])
                Bitmap image = BitmapFactory.decodeStream(is); // crea un oggetto Bitmap(immagine) da diverse fonti come file, streams, e array di byte[]
                inviaImmagine(image);  // e la inviamo al webSocket una volta convertita da Array di byte[] in formato Base64String



            } catch (FileNotFoundException e) { // Questa operazione potrebbe lanciare un'eccezione di tipo file non trovato va quindi messa in un blocco Try/Catch
                e.printStackTrace();
            }

        }

    }

    private void inviaImmagine(Bitmap image) { // Necessario a convertire l'immagine in formato stringa ed a comprimerla prima di inviarla al server

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        String base64String = Base64.encodeToString(outputStream.toByteArray(),
                Base64.DEFAULT);

        JSONObject jsonObject = new JSONObject();

        try { // Questa operazione potrebbe lanciare un'eccezione di tipo Json Exception, va quindi messa in un blocco Try/Catch-*
            jsonObject.put("name", name);
            jsonObject.put("image", base64String); // una volta convertita l'immagine in stringa,essa viene aggiunta al JSONObject che verrà inviato al webSocket

            webSocket.send(jsonObject.toString());

            jsonObject.put("inviato", true); // Booleano utilizzato per mostrare il messaggio nella recycler view come inviato / ricevuto

            messageAdapter.addItem(jsonObject); // Aggiungiamo l'item all'adapter attraverso il relativo metodo

            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) { // Metodo chiamato quando il client si collega al socket
            super.onOpen(webSocket, response);

            runOnUiThread(() -> {   // Funzione chiamata da un thread che lavora in background,per poterlo utilizzare sul main thread chiamiamo la funzione sottostante
                Toast.makeText(ActivityChat.this,
                        "Connessione al socket riuscita", // una volta riuscita la connessione al socket creiamo un MakeText di conferma
                        Toast.LENGTH_SHORT).show();

                creaView(); // Ed inizializziamo la recyclerView in base al messaggio / immagine (inviato / ricevuto)
            });

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) { // Metodo chiamato quando riceviamo un messaggio in formato String
            super.onMessage(webSocket, text);

            runOnUiThread(() -> {

                try { // Questa operazione potrebbe lanciare un'eccezione di tipo Json Exception, va quindi messa in un blocco Try/Catch
                    JSONObject jsonObject = new JSONObject(text);
                    jsonObject.put("inviato", false); // Booleano utilizzato per mostrare il messaggio come ricevuto nella recyclerView

                    messageAdapter.addItem(jsonObject);

                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1); // Per scrollare la recycler view all'ultimo messaggio ricevuto

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });

        }
    }
}
