package com.example.nodejschat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AdapterMessaggi extends RecyclerView.Adapter {

    private static final int TYPE_MESSAGE_SENT = 0;
    private static final int TYPE_MESSAGE_RECEIVED = 1;
    private static final int TYPE_IMAGE_SENT = 2;
    private static final int TYPE_IMAGE_RECEIVED = 3;

    private LayoutInflater inflater;
    private List<JSONObject> messages = new ArrayList<>();

    public AdapterMessaggi (LayoutInflater inflater) {
        this.inflater = inflater;
    }

    // Creazione di quattro recyclerView differenti a seconda del tipo di dati da inviare / ricevere

    @Override
    public int getItemViewType(int position) { // Metodo che specifica l'itemViewType da utilizzare in base al messaggio(inviato/ricevuto) || immagine inviata/ricevuta
                                                // riutilizzato dal metodo sottostante

        JSONObject message = messages.get(position);

        try {
            if (message.getBoolean("inviato")) {

                if (message.has("message"))
                    return TYPE_MESSAGE_SENT;
                else
                    return TYPE_IMAGE_SENT;

            } else {

                if (message.has("message"))
                    return TYPE_MESSAGE_RECEIVED;
                else
                    return TYPE_IMAGE_RECEIVED;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        switch (viewType) { // Switch necessario a creare view differenti a seconda dei casi(messaggio inviato/ricevuto || immagine inviata/ricevuta)
            case TYPE_MESSAGE_SENT:
                view = inflater.inflate(R.layout.item_sent_message, parent, false);
                return new SentMessageHolder(view);
            case TYPE_MESSAGE_RECEIVED:

                view = inflater.inflate(R.layout.item_received_message, parent, false);
                return new ReceivedMessageHolder(view);

            case TYPE_IMAGE_SENT:

                view = inflater.inflate(R.layout.item_sent_image, parent, false);
                return new SentImageHolder(view);

            case TYPE_IMAGE_RECEIVED:

                view = inflater.inflate(R.layout.item_received_photo, parent, false);
                return new ReceivedImageHolder(view);

        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        JSONObject message = messages.get(position);

        try { // Potrebbe lanciare un'eccezione di tipo JSON exception quindi va racchiuso in un blocco try/catch
            if (message.getBoolean("inviato")) {

                if (message.has("message")) {

                    SentMessageHolder messageHolder = (SentMessageHolder) holder;
                    messageHolder.messageTxt.setText(message.getString("message")); // Se la textview messaggiInviati contiene la stringa "message",essa verrà visualizzata all'interno dell'apposito contenitore

                } else {

                    SentImageHolder imageHolder = (SentImageHolder) holder; // Se l'oggetto inviato è un'immagine,essa verrà visualizzata all'interno dell'apposita imageView
                    Bitmap bitmap = getBitmapFromString(message.getString("image"));

                    imageHolder.imageView.setImageBitmap(bitmap);

                }

            } else {

                if (message.has("message")) {

                    ReceivedMessageHolder messageHolder = (ReceivedMessageHolder) holder; // Se la textview messaggiRicevuti contiene la stringa "message",essa verrà visualizzata all'interno dell'apposito contenitore
                    messageHolder.nominativoTxt.setText(message.getString("name"));
                    messageHolder.messageTxt.setText(message.getString("message"));

                } else {

                    ReceivedImageHolder imageHolder = (ReceivedImageHolder) holder;
                    imageHolder.nameTxt.setText(message.getString("name"));

                    Bitmap bitmap = getBitmapFromString(message.getString("image")); // Se l'oggetto ricevuto è un'immagine,essa verrà visualizzata all'interno dell'apposita imageView
                    imageHolder.imagePhoto.setImageBitmap(bitmap);

                }

            }

        }

        catch (JSONException e) {

            e.printStackTrace();
        }

    }

    private Bitmap getBitmapFromString(String image) { // Metodo che crea un oggetto di tipo Bitmap(immagine JPEG) a partire da una stringa formato BASE64

        byte[] bytes = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length); // per poter visualizzare correttamente l'immagine ricevuta
    }

    @Override
    public int getItemCount() { // Restituisce la dimensione dell'ArrayList contenente i messaggi
        return messages.size();
    }

    public void addItem (JSONObject jsonObject) { // Aggiunge un item(messaggio/immagine) alla recyclerView e notifica l'adapter dei cambiamenti effettuati sugli elementi da visualizzare
        messages.add(jsonObject);
        notifyDataSetChanged();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {

        TextView messageTxt;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);

            messageTxt = itemView.findViewById(R.id.msgInviato);
        }
    }

    private class SentImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public SentImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView nominativoTxt, messageTxt;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);

            nominativoTxt = itemView.findViewById(R.id.nominativoTxt);
            messageTxt = itemView.findViewById(R.id.msgRicevuto);
        }
    }

    private class ReceivedImageHolder extends RecyclerView.ViewHolder {

        ImageView imagePhoto;
        TextView nameTxt;

        public ReceivedImageHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.imagePhoto);
            nameTxt = itemView.findViewById(R.id.nameTxt);

        }
    }

}
