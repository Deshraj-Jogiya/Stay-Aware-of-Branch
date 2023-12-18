package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.models.PaymentsModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.MediaType;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.RequestBody;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class PaymentsPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StaticPagesHolder holder;

    private PaymentsModel posValues;
    private ArrayList<PaymentsModel> LIST_DATA = new ArrayList<PaymentsModel>();
    private Integer Res_PageLayout = R.layout.page_pay;
    private Integer Res_PageList = R.id.pay_view_list;
    private Integer Res_PageItemList = R.layout.page_pay_list_item;
    private String TOKEN;
    private ProgressBar mProgressBar;
    private boolean TokenRetry;
    private ImageView mRefresh;
    private String EXTRA_SEARCH;
    private int nextPage = 1;
    private boolean Locked;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(Res_PageLayout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            EXTRA_SEARCH = extras.getString("EXTRA_SEARCH");
        }
        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        ListFragment mFrag;
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LIST_DATA.clear();
                nextPage = 1;
                loadData(nextPage);
            }
        });
        mRefresh.setVisibility(View.VISIBLE);
        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("Invoices", "Invoices"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar.setVisibility(View.INVISIBLE);

        ImageView mSearch = (ImageView) findViewById(R.id.search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent MyIntent = new Intent(getBaseContext(), ControlActivity.class);
                MyIntent.putExtra("TARGET_FRAGMENT", "SearchView");
                MyIntent.putExtra("EXTRA_STRING_1", "PaymentsPage");
                MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Search");
                MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Search");
                startActivity(MyIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        ViewList = (ListView) findViewById(Res_PageList);

        ViewList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mListManager = new ListManager(this, ViewList, this, LIST_DATA);

        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage = 1;
                loadData(nextPage);
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage = 1;
                loadData(nextPage);
            }
        });

        nextPage = 1;
        loadData(nextPage);
    }


    public void loadData(final Integer page) {
        if (!Locked) {
            Locked = true;
            TOKEN = Concurrent.getAppToken(this);
            if (TOKEN != null) {
                mProgressBar.setVisibility(View.VISIBLE);
                mRefresh.setVisibility(View.INVISIBLE);

                //String mLink = App.getAppBaseUrl() + Constants.TASK_PAY_LIST + "/" + page;
                String mLink = App.TASK_BASE_URL+"upload"+ Constants.TASK_PAY_LIST + "/" + page;


                OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .url(mLink);


                if (EXTRA_SEARCH != null && !EXTRA_SEARCH.equals("")){
                    MediaType MEDIA_TYPE = MediaType.parse("application/json");
                    JSONObject paramsObject = new JSONObject();
                    JSONObject insideParamsObject = new JSONObject();
                    try {
                        insideParamsObject.put("text", EXTRA_SEARCH);
                        paramsObject.put("searchInput", insideParamsObject);
                    } catch (Exception ignored) {
                    }
                    RequestBody body = RequestBody.create(MEDIA_TYPE, paramsObject.toString());
                    requestBuilder.post(body);
                }else{
                    requestBuilder.get();
                }


                Request request = requestBuilder.build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                mProgressBar.setVisibility(INVISIBLE);
                                mRefresh.setVisibility(VISIBLE);
                                if (e instanceof ConnectException) {
                                    Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                } else {
                                    if (Concurrent.isFloat(e.getMessage())) {
                                        showError(e.getMessage());
                                    } else {
                                        showError("5012");
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Object serverResponse) {
                        final Response responseObj = (Response) serverResponse;
                        try {
                            response = responseObj.body().string();
                            Log.e("Invoices"," "+response);
                        } catch (Exception e) {
                            showError("5001");
                            return;
                        }

                        if (response != null) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        if (responseObj.isSuccessful()) {

                                            JsonParser parser = new JsonParser();
                                            JsonObject ValuesHolder = null;

                                            try {
                                                ValuesHolder = parser.parse(response).getAsJsonObject();
                                            } catch (Exception e) {
                                                Toast.makeText(PaymentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                            }
                                            if (ValuesHolder == null) {
                                                Toast.makeText(PaymentsPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                                mRefresh.setVisibility(View.VISIBLE);
                                                return;
                                            }

                                            JsonArray ValuesArray = ValuesHolder.getAsJsonArray("invoices");
                                            Log.e("Invoices"," "+ValuesArray);
                                            if (ValuesArray != null) {
                                                if (ValuesArray.size() > 0) {
                                                    if (page == null || page == 1)
                                                        LIST_DATA.clear();

                                                    for (JsonElement aValuesArray : ValuesArray) {
                                                        JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                        String title = Concurrent.tagsStringValidator(CurrObj, "fullName");
                                                        int status = Concurrent.tagsIntValidator(CurrObj, "paymentStatus");

                                                        String statusText = Concurrent.getLangSubWords("unpaid", "UNPAID");
                                                        if (status == 0)
                                                            statusText = Concurrent.getLangSubWords("unpaid", "UNPAID");
                                                        else if (status == 1)
                                                            statusText = Concurrent.getLangSubWords("paid", "PAID");
                                                        else if (status == 2)
                                                            statusText = Concurrent.getLangSubWords("ppaid", "Partially Paid");

                                                        if (title != null && !title.equals(""))
                                                            LIST_DATA.add(new PaymentsModel(Concurrent.tagsIntValidator(CurrObj, "id"),
                                                                    title,
                                                                    Concurrent.tagsStringValidator(CurrObj, "paymentTitle"),
                                                                    Concurrent.tagsStringValidator(CurrObj, "paymentDescription"),
                                                                    Concurrent.tagsStringValidator(CurrObj, "paymentAmount"),
                                                                    statusText,
                                                                    Concurrent.tagsStringValidator(CurrObj, "paymentDate"),
                                                                    Concurrent.tagsIntValidator(CurrObj, "studentId"),
                                                                    Concurrent.tagsStringValidator(CurrObj, "dueDate"),
                                                                    Concurrent.tagsStringValidator(CurrObj, "paidAmount")));
                                                    }

                                                    mListManager.getListAdapter().notifyDataSetChanged();
                                                    nextPage = page + 1;
                                                } else {
                                                    mListManager.removeFooter();
                                                }
                                                if (LIST_DATA.size() == 0)
                                                    mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                else
                                                    mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                            } else {
                                                mListManager.removeFooter();
                                            }
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            mRefresh.setVisibility(View.VISIBLE);
                                            TokenRetry = false;
                                            Locked = false;

                                        } else {
                                            renewToken();
                                        }
                                    } catch (final Exception e) {
                                        showError("5002");
                                    }
                                }
                            });
                        } else {
                            showError("5001");
                        }
                    }
                });

            }
        }
    }


    public void renewToken() {
        if (!TokenRetry) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("username", Concurrent.getAppUsername(getBaseContext()));
            formBody.add("password", Concurrent.getAppPassword(getBaseContext()));

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();            if(refreshedToken != null)formBody.add("android_token", refreshedToken);

            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_LOGIN);

            requestBuilder.post(formBody.build());

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                            mProgressBar.setVisibility(INVISIBLE);
                            mRefresh.setVisibility(VISIBLE);
                            if (e instanceof ConnectException) {
                                Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                            } else {
                                if (Concurrent.isFloat(e.getMessage())) {
                                    showError(e.getMessage());
                                } else {
                                    showError("5010");
                                }
                            }
                        }
                    });
                    Locked = false;
                }

                @Override
                public void onResponse(Call call, final Object serverResponse) {
                    final Response responseObj = (Response) serverResponse;
                    try {
                        response = responseObj.body().string();
                    } catch (Exception e) {
                        showError("5001");
                        return;
                    }

                    if (response != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if (responseObj.isSuccessful()) {

                                        String token;
                                        JsonParser parser = new JsonParser();
                                        JsonObject ValuesHolder = null;

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder != null) {
                                            token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                            if (token != null && token.length() > 1) {

                                                token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                                Concurrent.setAppToken(getBaseContext(), token);
                                                nextPage = 1;
                                                loadData(nextPage);

                                            } else {
                                                showError("5011");
                                            }
                                        } else {
                                            showError("5001");
                                        }
                                    } else {
                                        showError(Concurrent.checkErrorType(getBaseContext(), response));
                                    }
                                } catch (final Exception e) {
                                    showError("5002");
                                }
                            }
                        });
                    } else {
                        showError("5001");
                    }
                    Locked = false;
                    TokenRetry = true;
                }
            });

        } else {
            showError("5010");
            mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
            mProgressBar.setVisibility(INVISIBLE);
            mRefresh.setVisibility(VISIBLE);
        }
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
        mProgressBar.setVisibility(INVISIBLE);
        mRefresh.setVisibility(VISIBLE);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    @Override
    public void loadMore() {
        loadData(nextPage);
    }

    @Override
    public void AdapterConstructor() {

    }

    @Override
    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            holder = new StaticPagesHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.AmountData = (ParentStyledTextView) convertView.findViewById(R.id.amount_data);
            holder.DateData = (ParentStyledTextView) convertView.findViewById(R.id.date_data);
            holder.PayTitle = (ParentStyledTextView) convertView.findViewById(R.id.payment_title);
            holder.PayDesc = (ParentStyledTextView) convertView.findViewById(R.id.payment_description);
            holder.status = (ParentStyledTextView) convertView.findViewById(R.id.status_data);
            holder.studentName = (ParentStyledTextView) convertView.findViewById(R.id.student_data);
            holder.DueDateData = (ParentStyledTextView) convertView.findViewById(R.id.due_date_data);
            holder.PaidAmountData = (ParentStyledTextView) convertView.findViewById(R.id.paid_amount_data);
            holder.StudentImg = (CustomImageView) convertView.findViewById(R.id.header_student_img);
            holder.container = (RelativeLayout) convertView.findViewById(R.id.holder);
            holder.txt_studentId = (ParentStyledTextView) convertView.findViewById(R.id.txt_studentId);

            holder.student = (TextView) convertView.findViewById(R.id.student);
            holder.Date = (TextView) convertView.findViewById(R.id.Date);

            holder.Amount = (TextView) convertView.findViewById(R.id.Amount);

            convertView.setTag(holder);
        } else {
            holder = (StaticPagesHolder) convertView.getTag();
        }
        posValues = LIST_DATA.get(position);
        if (posValues != null) {
            holder.AmountData.setNotNullText(posValues.Amount);
            holder.DateData.setNotNullText(posValues.Date);
            holder.PayTitle.setNotNullText(posValues.paymentTitle);
            holder.PayDesc.setNotNullText(posValues.paymentDesc);
            holder.status.setNotNullText(posValues.Status);
            holder.studentName.setNotNullText(posValues.studentName);
            holder.DueDateData.setNotNullText(posValues.DueDate);
            holder.PaidAmountData.setNotNullText(posValues.PaidAmount);
            holder.txt_studentId.setNotNullText(String.valueOf(posValues.studentId));
            holder.StudentImg.profileID = String.valueOf(posValues.studentId);
            holder.StudentImg.load();

            holder.container.setTag(posValues);
            if (Concurrent.isUserHavePermission(this, "Invoices.View")) {
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PaymentsModel payModel = (PaymentsModel) v.getTag();

                        Intent MyIntent = new Intent(getBaseContext(), PaymentInvoiceView.class);
                        MyIntent.putExtra("invoice_id", payModel.id.toString());
                        startActivity(MyIntent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }

           // Concurrent.setLangWords(this, holder.student, holder.Date, holder.Amount);
            Concurrent.setLangWords(this, holder.student, holder.Amount);

        }

        return convertView;
    }

    class StaticPagesHolder {
        TextView Date;
        TextView Amount;
        TextView student;
        ParentStyledTextView PayTitle;
        ParentStyledTextView PayDesc;
        ParentStyledTextView studentName;
        ParentStyledTextView status;
        ParentStyledTextView AmountData;
        ParentStyledTextView PaidAmountData;
        ParentStyledTextView DateData;
        ParentStyledTextView DueDateData;
        CustomImageView StudentImg;
        RelativeLayout container;
        ParentStyledTextView txt_studentId;
    }
}
