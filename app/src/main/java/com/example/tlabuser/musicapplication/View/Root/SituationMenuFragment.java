package com.example.tlabuser.musicapplication.View.Root;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tlabuser.musicapplication.JsonLoader;
import com.example.tlabuser.musicapplication.Main;
import com.example.tlabuser.musicapplication.Model.Situation;
import com.example.tlabuser.musicapplication.R;
import com.example.tlabuser.musicapplication.SQLOpenHelper;
import com.example.tlabuser.musicapplication.View.Situation.SituationDetailFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class SituationMenuFragment extends Fragment implements LoaderManager.LoaderCallbacks<JSONObject>{

    private JSONArray jsonArray = new JSONArray();

    private RecyclerView rvRecommendedSituations, rvSituations;

    private Main mainActivity;

    private SQLOpenHelper sqlOpenHelper;
    private SQLiteDatabase db;

    private List<Situation> recommendedSituations, situations;

    private SituationsRecyclerAdapter situationsRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mainActivity = (Main)getActivity();

        sqlOpenHelper = new SQLOpenHelper(mainActivity);
        db = sqlOpenHelper.getReadableDatabase();

        situations = Situation.getSituationsFromSQL(db);

        if(situations.isEmpty()){
            // JSONの取得
            Log.d("RootMenuFragment", "situations.isEmpty()");
            getLoaderManager().restartLoader(1, null, this);
            Toast.makeText(mainActivity, "SituationListを取得しています。\nしばらくお待ちください。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_situation_menu,container,false);

        rvRecommendedSituations = (RecyclerView) v.findViewById(R.id.rv_recommended_situations);
        rvRecommendedSituations.setLayoutManager(new GridLayoutManager(mainActivity, 2));
        rvRecommendedSituations.setNestedScrollingEnabled(false);
        rvSituations = (RecyclerView) v.findViewById(R.id.rv_situations);
        rvSituations.setLayoutManager(new LinearLayoutManager(mainActivity));
        rvSituations.setNestedScrollingEnabled(false);

        situationsRecyclerAdapter = new SituationsRecyclerAdapter(mainActivity, situations);
        situationsRecyclerAdapter.setItemClickedListener(new SituationsRecyclerAdapter.ItemClickedListener() {
            @Override
            public void onItemClicked(Situation situation) {
                mainActivity.focusSituation(situation);
                FragmentManager fm = mainActivity.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.root, new SituationDetailFragment()).addToBackStack(null).commit();
            }
        });
        rvSituations.setAdapter(situationsRecyclerAdapter);

        recommendedSituations = situations.subList(0, 4);
        situationsRecyclerAdapter = new SituationsRecyclerAdapter(mainActivity, recommendedSituations);
        situationsRecyclerAdapter.setItemClickedListener(new SituationsRecyclerAdapter.ItemClickedListener() {
            @Override
            public void onItemClicked(Situation situation) {
                mainActivity.focusSituation(situation);
                FragmentManager fm = mainActivity.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.root, new SituationDetailFragment()).addToBackStack(null).commit();
            }
        });
        rvRecommendedSituations.setAdapter(situationsRecyclerAdapter);

        return v;

    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        String head = "https://musicmetadata.herokuapp.com/ds/query?query=";
        String tail = "&output=json&stylesheet=/xml-to-html.xsl";
        String urlStr =
                "prefix situation: <http://music.metadata.database.situation/> \n" +
                        "\n" +
                        "SELECT distinct ?tag\n" +
                        "WHERE {\n" +
                        "  ?b situation:tag ?tag;\n" +
                        "      situation:weight ?weight.\n" +
                        "}\n" +
                        "order by (?tag)";
        try {
            urlStr = URLEncoder.encode(urlStr, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.d("RootMenuFragment","URLエンコードに失敗しました。 UnsupportedEncodingException=" + e);
        }
        urlStr = head + urlStr + tail;

        JsonLoader jsonLoader = new JsonLoader(getActivity(), urlStr);
        jsonLoader.forceLoad();
        return  jsonLoader;
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
        if (data != null) {

            try {
                jsonArray = data.getJSONObject("results").getJSONArray("bindings");
                if (jsonArray.getJSONObject(0).has("tag")) {
                    situations = Situation.getItems(jsonArray);
                    Situation.insertRows(db, situations);

                    situationsRecyclerAdapter = new SituationsRecyclerAdapter(mainActivity, situations);
                    rvSituations.setAdapter(situationsRecyclerAdapter);
                }

            } catch (JSONException e) {
                Log.d("RootMenuFragment","JSONのパースに失敗しました。 JSONException=" + e);
            }
        }else{
            Log.d("RootMenuFragment", "onLoadFinished error!");
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        // 処理なし
    }
}
