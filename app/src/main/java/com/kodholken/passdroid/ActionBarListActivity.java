package com.kodholken.passdroid;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class ActionBarListActivity extends AppCompatTimeoutListActivity {

    private final class ListOnItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> lv, View v, int position, long id) {
            onListItemClick((ListView) lv, v, position, id);
            // String str = ((TextView) arg1).getText().toString();
            // Toast.makeText(getBaseContext(), str,
            // Toast.LENGTH_LONG).show();
            // Intent intent = new Intent(getBaseContext(),
            // your_new_Intent.class);
            // intent.putExtra("list_view_value", str);
            // startActivity(intent);
        }
    }

    private ListView mListView;

    protected ListView getListView() {

        if (mListView == null) {
            initListView();
        }
        return mListView;
    }

    private void initListView() {
        mListView = findViewById(getListViewId());
        if (mListView == null) {
            throw new RuntimeException(
                    "ListView cannot be null. Please set a valid ListViewId");
        }

        mListView.setOnItemClickListener(new ListOnItemClickListener());
    }

    protected abstract int getListViewId();

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected void onListItemClick(ListView lv, View v, int position, long id) {
        // No default functionality. To override
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }
}