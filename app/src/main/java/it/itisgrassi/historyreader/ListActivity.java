package it.itisgrassi.historyreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Claudiu on 08/02/2015.
 */
public class ListActivity extends android.app.ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;
    private Intent intent;
    private ImageView img;
    private View riga;
    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String,Object >> lista;

    // url to get all products list
    private static String url_questionario = "http://hostingphpscuola.altervista.org/HistoryReader/questionario.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_QUESTIONARIO = "questionario";
    private static final String TAG_PID = "pid";
    private static final String TAG_PUNTI = "punti";
    private static final String TAG_DOMANDA = "domanda";
    private static final String TAG_RISPOSTA = "risposta";
    private static final String TAG_IMAGE = "check";

    // products JSONArray
    JSONArray products = null;

    int check = -1;
    int numIntent = 1;
    int uri;
    int status = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        // Hashmap for ListView
        lista = new ArrayList<HashMap<String, Object>>();

        intent = getIntent();
        riga = findViewById(R.id.riga);

        // Loading products in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = String.valueOf(position + 1);
                Log.d("PID", pid);
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),QuestionActivity.class);

                in.putExtra(TAG_PID, pid);

                startActivity(in);

                // starting new activity and expecting some response back
                //startActivityForResult(in, 100);  //<- questo genera errore
            }
        });

    }

    /*
    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }
    */

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListActivity.this);
            pDialog.setMessage("Caricamento in corso!\nPrego Attendere...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json=null;

            try {
                 json = jsonParser.makeHttpRequest(url_questionario, "GET", params);
                 //status = jsonParser.getStatusCode();
                 Log.d("HTTP-STATUS", "" + status);
            }
            catch(Exception internet){
                Log.e("HTTP-REQ", "Errore in HTTP_request");}

            try {
                // Checking for SUCCESS TAG

                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // products found
                        // Getting Array of Products
                        products = json.getJSONArray(TAG_QUESTIONARIO);

                        // looping through All Products
                        for (int i = 0; i < products.length(); i++) {

                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
                            String id = "Domanda n° ";

                            id += c.getString(TAG_PID);
                            String domanda = c.getString(TAG_DOMANDA);


                            // creating new HashMap
                            HashMap<String, Object> map = new HashMap<String, Object>();

                            map.put(TAG_PID, id);
                            map.put(TAG_DOMANDA, domanda);

                            // adding HashList to ArrayList
                            lista.add(map);
                        }
                    } else {


                    }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSONe", "Exception in json");
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            getApplicationContext(),
                            lista,
                            R.layout.list_item,
                            new String[] {TAG_PID,TAG_DOMANDA },
                            new int[] {R.id.pid, R.id.quest }
                    );
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}
