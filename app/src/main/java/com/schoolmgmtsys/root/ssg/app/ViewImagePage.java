package com.schoolmgmtsys.root.ssg.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.schoolmgmtsys.root.ssg.R;


public class ViewImagePage extends AppCompatActivity {

    private String TargetFragment = "Calendar";
    private Integer Res_PageLayout = R.layout.page_view_receipt;
    ImageView img_view_receipt;
TextView image_lebel;
    private String TOKEN;
private String URL;
    private ImageView img_back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(Res_PageLayout);
        Bundle extras = getIntent().getExtras();
        img_view_receipt=(ImageView)findViewById(R.id.img_view_receipt);
        img_back=(ImageView)findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if(extras!=null)
        TargetFragment = extras.getString("TARGET_FRAGMENT");
image_lebel = findViewById(R.id.image_lebel);

        String receipt_id=getIntent().getStringExtra("receipt_id");
       // DownloadReceit(receipt_id);

        if(TargetFragment.equalsIgnoreCase("ClassesSchPage")){
            URL ="https://rootssg.schoolmgmtsys.com/upload/assets/images/schedule.png";
            image_lebel.setText("Class Schedule");
        }else {
            URL = "https://rootssg.schoolmgmtsys.com/upload/assets/images/calendar.png";
            image_lebel.setText("Calendar");
        }
        Glide.with(getApplicationContext())
                .load(URL)
                .into(img_view_receipt);

    }


   /* private void DownloadReceit(String receipt_id) {

        ProgressDialog pd = new ProgressDialog(ViewReceiptPage.this);
        pd.setMessage("Please Wait,receipt downloading...");
        pd.setCancelable(false);
        pd.show();

        TOKEN = Concurrent.getAppToken(getApplicationContext());
        Log.e("TOKEN"," "+TOKEN);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,"https://axcel.schoolmgmtsys.com/invoices/download/"+receipt_id+"?token="+TOKEN,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        pd.dismiss();
                        Log.e("RESPONSE"," "+response);
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            String url =jsonObject.optString("url","");
                            if(!url.equals("")){
                                Glide.with(getApplicationContext())
                                        .load(url)
                                        .into(img_view_receipt);

                            }else{
                                  Toast.makeText(ViewReceiptPage.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError"," "+error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                // params.put("Content-Type", "application/json");
                params.put("Authorization","application/json");
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters = new HashMap<String,String>();
                parameters.put("token", TOKEN);
                Log.e("params"," " +parameters);
                return parameters;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        requestQueue.add(stringRequest);


    }
*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
