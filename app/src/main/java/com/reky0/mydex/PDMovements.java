package com.reky0.mydex;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.reky0.mydex.pokemon.Move;
import com.reky0.mydex.pokemon.Pokemon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PDMovements#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PDMovements extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    PokemonDataViewModel savedPokemon;
    LinearLayout innateMoves;
    LinearLayout tmTrMoves;
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson =  new GsonBuilder().setPrettyPrinting().create();

    public PDMovements() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PDMovements.
     */
    // TODO: Rename and change types and number of parameters
    public static PDMovements newInstance(String param1, String param2) {
        PDMovements fragment = new PDMovements();
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
        return inflater.inflate(R.layout.fragment_p_d_movements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        savedPokemon = new ViewModelProvider(requireActivity()).get(PokemonDataViewModel.class);

        // loop through every move in the arraylist
        // then fetch every move and put the info in a inflated template of a move
        addMovements(savedPokemon.getSavedPokemon());
    }

    public void addMovements(Pokemon p) {
        innateMoves = requireActivity().findViewById(R.id.innateMoves);
        tmTrMoves = requireActivity().findViewById(R.id.tmTrMoves);


        String moveURL;
        String name;
        final String[] type = new String[1];
        final String[] damageClass = new String[1];
        final int[] power = new int[1];
        final int[] accuracy = new int[1];
        final int[] pp = new int[1];
        final String[] description = new String[1];
        ArrayList<Pokemon.MoveSlot.VersionGroupDetails> versionGroupDetails;
        Pokemon.MoveSlot.VersionGroupDetails.MoveLearnMethod moveLearnMethod;
        String moveLearnMethodName;


        for (Pokemon.MoveSlot ms : p.getMoves()) {
            name = ms.getMove().getName();
            moveURL = ms.getMove().getUrl();
            versionGroupDetails = ms.getVersion_group_details();
            moveLearnMethod = versionGroupDetails.get(versionGroupDetails.size()-1).getMove_learn_method();
            moveLearnMethodName = moveLearnMethod.getName();

            // then fetch every move and put the info in a inflated template of a move
            Request moveRequest = new Request.Builder().
                    url(moveURL)
                    .build();

            Log.d("PDMovements-addMovements", "Performing Request: "+moveURL);
            String finalMoveLearnMethodName = moveLearnMethodName;
            String finalName = name;
            client.newCall(moveRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("PDMovements-addMovements", "Request ERROR -> "+e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body().string();

                    Log.d("PDMovements-addMovements", "BODY = "+responseBody);

                    if (response.code() != 200) {
                        Log.e("PDMovements-addMovements", "Request ERROR -> "+response.code());
                    } else {
                        Log.d("PDMovements-addMovements", "Successful request");
                        Log.d("PDMovements-addMovements", "Parsing to JSON");
                        Move move = gson.fromJson(responseBody, Move.class);

                        type[0] = move.getType().getName();
                        damageClass[0] = move.getDamage_class().getName();
                        power[0] = move.getPower();
                        accuracy[0] = move.getAccuracy();
                        pp[0] = move.getPp();
                        ArrayList<Move.EffectEntry> effectEntries = move.getEffect_entries();

                        for (Move.EffectEntry ee : effectEntries) {
                            if (ee.getLanguage().getName().equals("en")) {
                                description[0] = ee.getShort_effect();
                            }
                        }

                        requireActivity().runOnUiThread(() -> {
                            LinearLayout moveList;

                            if (finalMoveLearnMethodName.equals("level-up")) {
                                moveList = innateMoves;
                            } else {
                                moveList = tmTrMoves;
                            }

//                            LinearLayout moveElement = (LinearLayout) LayoutInflater.from(requireContext()).inflate(R.layout.pokemon_movement_template, moveList, false);
                            CardView moveElement = (CardView) LayoutInflater.from(requireContext()).inflate(R.layout.pokemon_movement_template, moveList, false);

                            ((TextView) moveElement.findViewById(R.id.name)).setText(formatMoveName(finalName));

//                            setType(moveElement.findViewById(R.id.type), type[0], getContext());
                            setType(moveElement, type[0], getContext());

                            int image = R.drawable.pokebal_placeholder;

                            switch (damageClass[0]) {
                                case "special":
                                    image = R.drawable.special;
                                    break;
                                case "status":
                                    image = R.drawable.status;
                                    break;
                                case "physical":
                                    image = R.drawable.physical;
                                    break;
                            }

                            Glide.with(getView())
                                    .load(image)
                                    .into((ImageView) moveElement.findViewById(R.id.damageClass));

                            ((TextView) moveElement.findViewById(R.id.power)).setText("Power\n"+((power[0] == 0) ? "-" : power[0]));
                            ((TextView) moveElement.findViewById(R.id.accuracy)).setText("Accuracy\n"+((accuracy[0] == 0) ? "-" : accuracy[0]));
                            ((TextView) moveElement.findViewById(R.id.pp)).setText("PP\n"+pp[0]);
                            ((TextView) moveElement.findViewById(R.id.description)).setText(description[0]);

                            moveList.addView(moveElement);
                        });
                    }
                }
            });
        }
    }

//
//    private void setType(TextView type, String name, Context context) {
//        type.setText(name);
//        int color = -1;
//
//        switch (name) {
//            case "normal":
//                color = ContextCompat.getColor(context, R.color.color_type_normal);
//                break;
//            case "fire":
//                color = ContextCompat.getColor(context, R.color.color_type_fire);
//                break;
//            case "water":
//                color = ContextCompat.getColor(context, R.color.color_type_water);
//                break;
//            case "electric":
//                color = ContextCompat.getColor(context, R.color.color_type_electric);
//                break;
//            case "grass":
//                color = ContextCompat.getColor(context, R.color.color_type_grass);
//                break;
//            case "ice":
//                color = ContextCompat.getColor(context, R.color.color_type_ice);
//                break;
//            case "fighting":
//                color = ContextCompat.getColor(context, R.color.color_type_fighting);
//                break;
//            case "poison":
//                color = ContextCompat.getColor(context, R.color.color_type_poison);
//                break;
//            case "ground":
//                color = ContextCompat.getColor(context, R.color.color_type_ground);
//                break;
//            case "flying":
//                color = ContextCompat.getColor(context, R.color.color_type_flying);
//                break;
//            case "psychic":
//                color = ContextCompat.getColor(context, R.color.color_type_psychic);
//                break;
//            case "bug":
//                color = ContextCompat.getColor(context, R.color.color_type_bug);
//                break;
//            case "rock":
//                color = ContextCompat.getColor(context, R.color.color_type_rock);
//                break;
//            case "ghost":
//                color = ContextCompat.getColor(context, R.color.color_type_ghost);
//                break;
//            case "dragon":
//                color = ContextCompat.getColor(context, R.color.color_type_dragon);
//                break;
//            case "dark":
//                color = ContextCompat.getColor(context, R.color.color_type_dark);
//                break;
//            case "steel":
//                color = ContextCompat.getColor(context, R.color.color_type_steel);
//                break;
//            case "fairy":
//                color = ContextCompat.getColor(context, R.color.color_type_fairy);
//                break;
//        }
//        type.setBackgroundColor(color);
//    }

    private void setType(CardView container, String name, Context context) {
        int color = -1;

        switch (name) {
            case "normal":
                color = R.color.color_type_normal;
                break;
            case "fire":
                color = R.color.color_type_fire;
                break;
            case "water":
                color = R.color.color_type_water;
                break;
            case "electric":
                color = R.color.color_type_electric;
                break;
            case "grass":
                color = R.color.color_type_grass;
                break;
            case "ice":
                color = R.color.color_type_ice;
                break;
            case "fighting":
                color = R.color.color_type_fighting;
                break;
            case "poison":
                color = R.color.color_type_poison;
                break;
            case "ground":
                color = R.color.color_type_ground;
                break;
            case "flying":
                color = R.color.color_type_flying;
                break;
            case "psychic":
                color = R.color.color_type_psychic;
                break;
            case "bug":
                color = R.color.color_type_bug;
                break;
            case "rock":
                color = R.color.color_type_rock;
                break;
            case "ghost":
                color = R.color.color_type_ghost;
                break;
            case "dragon":
                color = R.color.color_type_dragon;
                break;
            case "dark":
                color = R.color.color_type_dark;
                break;
            case "steel":
                color = R.color.color_type_steel;
                break;
            case "fairy":
                color = R.color.color_type_fairy;
                break;
        }
        container.setCardBackgroundColor(ContextCompat.getColor(context, color));
        container.setRadius(100);
    }


    String formatMoveName(String moveName) {
        moveName = moveName.replace("-", " ");

        moveName = capitalizeString(moveName);

        return moveName;
    }

    String capitalizeString(String input) {
        return Arrays.stream(input.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}