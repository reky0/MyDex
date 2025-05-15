package com.reky0.mydex;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.reky0.mydex.pokemon.Pokemon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PDStats#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PDStats extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    PokemonDataViewModel savedPokemon;
    private TextView name;
    private TextView id;
    private ImageView portrait;

    public PDStats() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PDStats.
     */
    // TODO: Rename and change types and number of parameters
    public static PDStats newInstance(String param1, String param2) {
        PDStats fragment = new PDStats();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_p_d_stats, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieves ViewModel of the Activity
        savedPokemon = new ViewModelProvider(this.requireActivity()).get(PokemonDataViewModel.class);
        name = view.findViewById(R.id.name);
        id = view.findViewById(R.id.pokedex_number);
        portrait = view.findViewById(R.id.portrait);

        loadInfoIntoUI(savedPokemon.getSavedPokemon(), requireContext());
        ((PokemonData) getActivity()).enableBottomNavButtons();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the tag of the current fragment
        Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (currentFragment != null) {
            outState.putString("last_fragment", currentFragment.getTag());
        }
    }

    // loads pokemon info into layout
    public void loadInfoIntoUI(Pokemon p, Context context) {
        Log.d("PDStats-loadPokemonInfo", "NAME = "+p.getName());

        // sets pokemon info into the header
        name.setText(p.getName());
        id.setText(id.getText().toString() + p.getId());

        // loads portrait image
        Glide.with(context)
                .load(p.getSprites().getFrontDefault())
                .into(portrait);

        // creates radar chart with pokemon data
        createRadarChart(p);
    }

    /**
     * Creates {@link RadarChart} based on Pokemon stats
     * @param p
     */
    public void createRadarChart(Pokemon p) {
        ArrayList<Pokemon.StatSlot> stats = p.getStats();
        String[] labels = new String[0];
        // get the radarChart placed in the layout
        RadarChart radarChart = requireActivity().findViewById(R.id.radarChart);

        // data for the chart
        ArrayList<RadarEntry> entries = new ArrayList<>();
        // for every stat of the pokemon, add a new entry
        for (Pokemon.StatSlot sl : stats) {
            // adds the base stat value to the chart
            entries.add(new RadarEntry((float) sl.getBaseStat()));
            // also adds the name of the stat to the labels array
            labels = Arrays.copyOf(labels, labels.length+1);
            labels[labels.length-1] = sl.getStat().getName();
        }


        Log.d("PDStats-createRadarChart", "entries: "+entries);
        Log.d("PDStats-createRadarChart", "labels: "+ Arrays.toString(labels));

        // Create dataset
        RadarDataSet dataSet = new RadarDataSet(entries, "Player Stats");
//        dataSet.setColor(R.color.pokew);
        dataSet.setFillColor(R.color.pokemon_black);
        dataSet.setDrawFilled(true);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(11f);

        // Create radar data
        RadarData data = new RadarData(dataSet);
        radarChart.setData(data);

        // Customize X-Axis (labels)
        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.BLACK);

        // Customize Y-Axis (values)
        YAxis yAxis = radarChart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(200f);
        yAxis.setGranularity(2f);
        yAxis.setTextColor(Color.TRANSPARENT);

        // General chart settings
        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.getLegend().setTextColor(Color.BLACK);
        radarChart.setWebColor(R.color.pokemon_black);
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColorInner(R.color.pokemon_white);
        radarChart.setWebLineWidthInner(0.5f);

        radarChart.setTouchEnabled(false);
        radarChart.setRotationEnabled(false);

        // Refresh chart
        radarChart.invalidate();
    }
}