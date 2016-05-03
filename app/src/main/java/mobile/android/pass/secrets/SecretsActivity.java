package mobile.android.pass.secrets;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import mobile.android.pass.R;
import mobile.android.pass.settings.SettingsActivity;

// TODO: https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
public class SecretsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    SecretsAdapter mSecretsAdapter;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secrets);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Construct the data source.
        ArrayList<Secret> arrayOfSecrets = new ArrayList<>();

        // Create the adapter to convert the array to views.
        mSecretsAdapter = new SecretsAdapter(this, arrayOfSecrets);

        // Attach the adapter to a RecyclerView.
        mRecyclerView = (RecyclerView) findViewById(R.id.list_secrets);
        mRecyclerView.setAdapter(mSecretsAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_secrets);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_secrets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.open_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        RetrieveSecretsTask retrieveSecretsTask = new RetrieveSecretsTask();
        retrieveSecretsTask.execute((Void) null);
    }

    public class RetrieveSecretsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // Fetching some data, data has now returned
            String json = "[\n" +
                    "  {\n" +
                    "    \"domain\": \"gmail.com\",\n" +
                    "    \"path\": \"gmail.com\",\n" +
                    "    \"username\": \"rcaldwell\",\n" +
                    "    \"username_normalized\": \"rcaldwell\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  }\n" +
                    "]";
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsonArray != null) {
                final ArrayList<Secret> secrets = Secret.fromJson(jsonArray);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSecretsAdapter = new SecretsAdapter(SecretsActivity.this, secrets);
                        mRecyclerView.setAdapter(mSecretsAdapter);
                    }
                });
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSwipeRefreshLayout.setRefreshing(false);

            super.onPostExecute(success);
        }

        @Override
        protected void onCancelled() {
            mSwipeRefreshLayout.setRefreshing(false);

            super.onCancelled();
        }
    }
}
