package it.itisgrassi.historyreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Claudiu on 08/02/2015.
 */
public class QuestionActivity extends Activity {
    // AQuery aq = null;
    TextView titolo;
    TextView risposta;
    TabHost mTabHost;

    Button menu;

    String pid = null;
    String link = null;
    int position = 0;
    int check = 0;
    int status = 0;
    boolean flag = false;
    private Intent intent;
    JSONObject obj = null;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // single product url
    private static final String url_domanda = "http://hostingphpscuola.altervista.org/HistoryReader/domanda.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_QUESTIONARIO = "questionario";
    private static final String TAG_PID = "pid";
    private static final String TAG_DOMANDA = "domanda";
    private static final String TAG_RISPOSTA = "risposta";
    private static final String TAG_IMAGE = "image_link";
    private static final String TAG_MESSAGE = "message";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domanda);

        //aq = new AQuery(this);

        menu = (Button) findViewById(R.id.menu);

        titolo = (TextView) findViewById(R.id.domanda);
        risposta = (TextView) findViewById(R.id.risposta);

        //immagine = (ImageView) findViewById(R.id.immagine);

        intent = getIntent();

        pid = intent.getStringExtra(TAG_PID);
        Log.d("PID_CLICK", pid);
        // Getting complete product details in background thread
        new DomandeAsync().execute();

        // Delete button click event
        menu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Context some = getApplicationContext();
                Intent i = new Intent(some, ListActivity.class);
                i.putExtra("check", check);
                i.putExtra("intent", pid);
                startActivity(i);
            }
        });

    }

    /**
     * Background Async Task to Get complete product details
     */
    class DomandeAsync extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(QuestionActivity.this);
            pDialog.setMessage("Caricamento in corso!\nAttendere...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting product details in background thread
         */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread


            // Check for success tag
            int success;

            // Building Parameters
            List<NameValuePair> param;
            JSONObject json = null;
            param = new ArrayList<NameValuePair>();
            param.add(new BasicNameValuePair(TAG_PID, pid));
            try {
                json = jsonParser.makeHttpRequest(url_domanda, "GET", param);
                status = jsonParser.getStatusCode();
                Log.d("HTTP-STATUS", "" + status);
            } catch (Exception internet) {
                Log.e("HTTP_REQ", "Errore http request");
            }

            try {

                    success = json.getInt(TAG_SUCCESS);
                    Log.d("SUCCESS", "" + success);

                    if (success == 1) {
                        // successfully received product details
                        JSONArray productObj = json.getJSONArray(TAG_QUESTIONARIO); // JSON Array

                        // get first product object from JSON Array
                        obj = productObj.getJSONObject(0);

                        link = obj.getString(TAG_IMAGE);
                        Log.d("JSON-LINK", link);


                    } else {
                        // product with pid not found

                        Log.d("NO-SUCC", "SUCCESS = 0");
                    }

            } catch (JSONException e) {
                //e.printStackTrace();
                Log.e("JSON-1", "Caricamento fallito!");
            }

            return null;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details

            try {
                titolo.setText(obj.getString(TAG_DOMANDA));
                risposta.setText(obj.getString(TAG_RISPOSTA));
                if (!link.equalsIgnoreCase("NULL")) { //prende il link dal json ricevuto e controlla che non sia un url nullo
                    //aquery : carica l'immagine da internet ma se tale immagine non viene trovata allora carica l'immagine di default
                    //aq.id(R.id.immagine).image(link, true, true, 0, R.mipmap.ic_launcher);
                    //aq.id(R.id.immagine).image(link, true, true);
                }
            } catch (JSONException e) {
                Log.e("JSON-E", "Errore in JSON");
            }

            pDialog.dismiss();
        }
    }

}