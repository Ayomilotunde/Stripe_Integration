package com.ayomi.stripeinntegration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button button;
    String SECRET_KEY = "sk_test_51L6vS8Ap8p4eoh3TDmQ5bCS4BMWgIfBDFTEcXHsYHBJG4Gzrik0MGR5ohlExMh2woNJLlOFNgTmChOgW9a3lXupP00hDefcuI9";
    String PUBLISH_KEY = "pk_test_51L6vS8Ap8p4eoh3TzkBonltIbxwiQZCLt4JvsulKeOpS8ZxrR1SedGJVGiKNYix5go5jp10ck2H0m521HEIQXo1X00wpsGh271";

    PaymentSheet paymentSheet;

    String customerID;
    String ephericalkey;
    String clientSecretKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);

        button.setOnClickListener(v-> {
            paymentFlow();

        });

        PaymentConfiguration.init(this, PUBLISH_KEY);

        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);
        });

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    customerID = object.getString("id");
                    getEphericalKey(customerID);
//                    Toast.makeText(MainActivity.this, customerID, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer " + SECRET_KEY);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);


    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        }
    }

    private void getEphericalKey(String customerID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    ephericalkey = object.getString("id");
                    getClientSecret(customerID, ephericalkey);
//                    Toast.makeText(MainActivity.this, ephericalkey, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer " + SECRET_KEY);
                header.put("Stripe-Version","2020-08-27" );
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customerID, String ephericalkey) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    clientSecretKey = object.getString("client_secret");
                    getClientSecret(customerID, ephericalkey);
//                    Toast.makeText(MainActivity.this, clientSecretKey, Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer " + SECRET_KEY);
                return header;
            }

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                params.put("amount", "50"+"00");
                params.put("currency", "usd");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }

    private void paymentFlow() {
        paymentSheet.presentWithPaymentIntent(
                clientSecretKey, new PaymentSheet.Configuration("One waiter", new PaymentSheet.CustomerConfiguration(
                        customerID,
                        ephericalkey
                ))
        );
    }
}