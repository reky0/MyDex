package com.reky0.mydex;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.reky0.mydex.pokemon.Pokemon;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PDInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PDInfo extends Fragment {
    private static final String POKEMON_SEARCHED = "nothing";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final String BASE_POKEMON_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final String BASE_SPECIE_URL = "https://pokeapi.co/api/v2/pokemon-species/";
    private String pokemonSearched;
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson =  new GsonBuilder().setPrettyPrinting().create();

    private static PokemonDataViewModel savedPokemon;

    private static LottieAnimationView loadingScreen;
    private static TextView errorScreen;
    private static TextView name;
    private static TextView id;
    private static ImageView portrait;
    private static TextView type1;
    private static TextView type2;
    private static TextView height;
    private static TextView weight;
    private static TextView isLegendary;
    private static TextView isMythical;
    private static ImageButton cryButton;
    private static LinearLayout pokemonSprites;


    public PDInfo() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment PDInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static PDInfo newInstance(String param1) {
        PDInfo fragment = new PDInfo();
        Bundle args = new Bundle();
        args.putString(POKEMON_SEARCHED, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pokemonSearched = getArguments().getString(POKEMON_SEARCHED);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_p_d_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieves ViewModel of the Activity
        savedPokemon = new ViewModelProvider(this.requireActivity()).get(PokemonDataViewModel.class);

        loadingScreen = view.findViewById(R.id.loadingScreen);
        loadingScreen.setRepeatCount(LottieDrawable.INFINITE);
        loadingScreen.playAnimation();

        errorScreen = view.findViewById(R.id.errorScreen);
        errorScreen.setVisibility(View.GONE);

        name = view.findViewById(R.id.name);
        id = view.findViewById(R.id.pokedex_number);
        portrait = view.findViewById(R.id.portrait);
        type1 = view.findViewById(R.id.type1);
        type2 = view.findViewById(R.id.type2);
        height = view.findViewById(R.id.height);
        weight = view.findViewById(R.id.weight);
        isLegendary = view.findViewById(R.id.isLegendary);
        isMythical = view.findViewById(R.id.isMythical);
        cryButton = view.findViewById(R.id.cryButton);
        pokemonSprites = view.findViewById(R.id.pokemonSprites);

        Log.d("PDInfo", "saved pokemon: "+savedPokemon.getSavedPokemon());

        // check if pokemon has already been loaded (for activity reload trigger)
        if (savedPokemon.getSavedPokemon() != null) {
            // loads previously got info again
            loadInfoIntoUI(savedPokemon.getSavedPokemon(), view.getContext());
            // after loading the data of the pokemon into the UI, enable the menu buttons so that the app
            // doesnt crash due to null pokemon data
            ((PokemonData) getActivity()).enableBottomNavButtons();
        } else {

            Log.d("PokemonData-onCreate", pokemonSearched);

            try {
                getPokemon(pokemonSearched, view.getContext());        // after loading the data of the pokemon into the UI, enable the menu buttons so that the app
                // doesnt crash due to null pokemon data
                ((PokemonData) getActivity()).enableBottomNavButtons();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
    public static void loadInfoIntoUI(Pokemon p, Context context) {
        // when loading info on screen, also loads that pokemon to the ViewModel so that if the
        // activity has any change (eg.-change of theme mode) doesn't need to rerun the request
        savedPokemon.setSavedPokemon(p);

        Log.d("loadPokemonInfo", "NAME = "+p.getName());
        loadingScreen.setVisibility(View.GONE);

        name.setText(p.getName());
        id.setText(id.getText().toString() + p.getId());

        Glide.with(context)
                .load(p.getSprites().getFrontDefault())
                .into(portrait);

        setType(type1, p.getTypes().get(0).getType().getName(), context);

        // check if the pokemon has two types
        if (p.getTypes().size() == 2) {
            setType(type2, p.getTypes().get(1).getType().getName(), context);
        } else {
            type2.setVisibility(View.INVISIBLE);
        }

        height.setText(height.getText().toString() + p.getHeight() + "m");
        weight.setText(weight.getText().toString() + p.getWeight() + "kg");

        cryButton.setOnClickListener(view -> {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(p.getCries().getLatest());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        String[] spriteNames = {"Front",
                "Back",
                "Front Shiny",
                "Back Shiny",
                "Front Female",
                "Back Female",
                "Front Female Shiny",
                "Back Female Shiny"
        };

        for (int i = 0; i < p.getSprites().getAllSprites().size(); i++) {
            String url = p.getSprites().getAllSprites().get(i);
            String name = spriteNames[i];

            if (url == null) {
                continue;
            }

            // Inflar la vista desde el XML
            LinearLayout spriteView = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.pokemon_sprite_element_template, pokemonSprites, false);

            // Configurar los datos en la vista inflada
            ImageView spriteImg = spriteView.findViewById(R.id.sprite);
            TextView spriteName = spriteView.findViewById(R.id.name);

            Glide.with(context)
                    .load(url)
                    .error(url) // if throws error retries once again to load the img
                    .into(spriteImg);

            spriteName.setText(name);

            pokemonSprites.addView(spriteView);
        }

        isLegendary.setText(isLegendary.getText().toString() + ((p.isLegendary()) ? "Yes" : "No"));
        isMythical.setText(isMythical.getText().toString() + ((p.isMythical()) ? "Yes" : "No"));
    }


    private static void setType(TextView type, String name, Context context) {
        type.setText(name);
        int color = -1;

        switch (name) {
            case "normal":
                color = ContextCompat.getColor(context, R.color.color_type_normal);
                break;
            case "fire":
                color = ContextCompat.getColor(context, R.color.color_type_fire);
                break;
            case "water":
                color = ContextCompat.getColor(context, R.color.color_type_water);
                break;
            case "electric":
                color = ContextCompat.getColor(context, R.color.color_type_electric);
                break;
            case "grass":
                color = ContextCompat.getColor(context, R.color.color_type_grass);
                break;
            case "ice":
                color = ContextCompat.getColor(context, R.color.color_type_ice);
                break;
            case "fighting":
                color = ContextCompat.getColor(context, R.color.color_type_fighting);
                break;
            case "poison":
                color = ContextCompat.getColor(context, R.color.color_type_poison);
                break;
            case "ground":
                color = ContextCompat.getColor(context, R.color.color_type_ground);
                break;
            case "flying":
                color = ContextCompat.getColor(context, R.color.color_type_flying);
                break;
            case "psychic":
                color = ContextCompat.getColor(context, R.color.color_type_psychic);
                break;
            case "bug":
                color = ContextCompat.getColor(context, R.color.color_type_bug);
                break;
            case "rock":
                color = ContextCompat.getColor(context, R.color.color_type_rock);
                break;
            case "ghost":
                color = ContextCompat.getColor(context, R.color.color_type_ghost);
                break;
            case "dragon":
                color = ContextCompat.getColor(context, R.color.color_type_dragon);
                break;
            case "dark":
                color = ContextCompat.getColor(context, R.color.color_type_dark);
                break;
            case "steel":
                color = ContextCompat.getColor(context, R.color.color_type_steel);
                break;
            case "fairy":
                color = ContextCompat.getColor(context, R.color.color_type_fairy);
                break;
        }
        type.setBackgroundColor(color);
    }


    // getSpecieData
    public static void getSpecieData(@NotNull Pokemon p, PokemonSpecieCallback callback) throws IOException, InterruptedException {
        Request request = new Request.Builder()
                .url(p.getSpecies().getUrl())
                .build();

        Log.d("getSpecieData", "Performing Request: "+p.getSpecies().getUrl());
        client.newCall(request).enqueue(new Callback() {
            // in case of failure
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("getSpecieData", e.toString());
            }

            // in case of success
            @Override
            public void onResponse(Call call, Response r) throws IOException {
                if (r.isSuccessful()) {
                    Log.d("getSpecieData", "Successful Request");
                    String response = r.body().string();

                    Log.d("getSpecieData", "Parsing Json with specie data");
                    try {
                        // parses to jsonobject
                        JsonObject speciesData = gson.fromJson(response, JsonObject.class);

                        // legendary and mythical are fields of the specie
                        p.setLegendary(speciesData.get("is_legendary").getAsBoolean());
                        p.setMythical(speciesData.get("is_mythical").getAsBoolean());


                        // ------------------------------------------------------------------
                        //                            VARIETIES
                        // ------------------------------------------------------------------
                        for (JsonElement je : speciesData.getAsJsonArray("varieties")) {
                            p.addVarietySlot(gson.fromJson(je.toString(), Pokemon.VarietySlot.class));
                        }


                        // ------------------------------------------------------------------
                        //                        EVOLUTION CHAIN
                        // ------------------------------------------------------------------
                        // from the specie data we get the evolution chain url
                        String evolutionChainURL = speciesData.getAsJsonObject("evolution_chain").get("url").getAsString();

                        getEvolutionChain(p, evolutionChainURL, callback);
                    } catch (Exception e) {
                        Log.e("getSpecieData", e.toString());
                    }
                } else {
                    Log.e("getSpecieData", "Request Failed");
                }
            }
        });
    }


    public static void getEvolutionChain(Pokemon p, String evolutionChainURL, PokemonSpecieCallback callback) {
        // request for the evolution chain
        Request evolutionRequest = new Request.Builder()
                .url(evolutionChainURL)
                .build();

        client.newCall(evolutionRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("getSpecieData-evolutionChain", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response r) throws IOException {
                if (r.isSuccessful()) {
                    Log.d("getSpecieData-evolutionChain", "Successful Request");
                    String evolutionResponse = r.body().string();

                    JsonObject evolutionChainData = gson.fromJson(evolutionResponse, JsonObject.class);

                    // we isolate the chain field
                    JsonObject chain = evolutionChainData.getAsJsonObject("chain");

                    // adds first stage to the evolution chain
                    String speciesURL = chain.getAsJsonObject("species").get("url").getAsString();
                    p.addEvolution(speciesURL);

                    // adding each stage of the chain to the pokemon evolution_chain ArrayList
                    while (chain != null) {
                        // gets the next stage
                        JsonArray evolvesTo = chain.getAsJsonArray("evolves_to");

                        // for each possible evolution in that stage, adds its URL (like pokemons suchs as Gardevoir/Gallade)
                        for (JsonElement je : evolvesTo) {
                            p.addEvolution(((JsonObject) je).getAsJsonObject("species").get("url").getAsString());
                        }

                        if (evolvesTo.isEmpty()) { // if it has not more content (is in last stage) then sets to null -> end loop
                            chain = null;
                        } else { // if it has content (there's a next stage) shortens the chain,
                            chain = evolvesTo.get(0).getAsJsonObject();
                        }
                    }

                    callback.onSuccess("Evolution chain and species data requests succeed.");
                } else {
                    Log.e("getSpecieData-evolutionChain", "Failed request.");
                    callback.onError("Evolution chain request failed.");
                }
            }
        });
    }


    /**
     *
     * @param pokemonSearched
     * @param context
     */
    public void getPokemon(@NotNull String pokemonSearched, Context context) {
        String url = BASE_POKEMON_URL + pokemonSearched;
        final Pokemon[] pokemon = {new Pokemon()};

        // sets request
        Request request = new Request.Builder()
                .url(url)
                .build();

        Log.d("getPokemon", "Performing Request: "+url);
        // performs request
        client.newCall(request).enqueue(new Callback() {
            // in case of failure
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("getPokemon", e.toString());
            }

            // in case of success
            @Override
            public void onResponse(Call call, Response r) throws IOException {
                String response = r.body().string();
                Log.d("getPokemon-TEST", response);

                if (r.code() != 200) {
                    Log.e("getPokemon", "Request Failed, retrieving from specie");
                    if (r.code() == 404) {
                        try {
                            getPokemonFromSpecie(pokemonSearched, new PokemonSpecieCallback() {
                                @Override
                                public void onSuccess(String pokemonData) {
                                    Log.d("getPokemon-getFromSpecie", "Successful Request");
                                    Log.d("getPokemon-getFromSpecie", "Parsing Json to Pokemon");
                                    // parsing data to Pokemon class
                                    pokemon[0] = gson.fromJson(pokemonData, Pokemon.class);

                                    // gets evolution chain and some other specie-related data
                                    try {
                                        getSpecieData(pokemon[0], new PokemonSpecieCallback() {
                                            @Override
                                            public void onSuccess(String pokemonData) {
                                                requireActivity().runOnUiThread(() -> {
                                                    try {
                                                        loadInfoIntoUI(pokemon[0], context);
                                                    } catch (Exception e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                Log.e("getPokemon", errorMessage);
                                            }
                                        });
                                    } catch (InterruptedException | IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                    requireActivity().runOnUiThread(() -> {
                                        try {
                                            loadInfoIntoUI(pokemon[0], context);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Log.e("getPokemon", errorMessage);
                                }
                            });
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                } else {
                    Log.d("getPokemon", "Successful Request");
                    Log.d("getPokemon", "Parsing Json to Pokemon");
                    // parsing data to Pokemon class
                    pokemon[0] = gson.fromJson(response, Pokemon.class);

                    // gets evolution chain and some other specie-related data
                    try {
                        getSpecieData(pokemon[0], new PokemonSpecieCallback() {
                            @Override
                            public void onSuccess(String pokemonData) {
                                requireActivity().runOnUiThread(() -> {
                                    try {
                                        loadInfoIntoUI(pokemon[0], context);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e("getPokemon", errorMessage);
                            }
                        });
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * Searchs from the specie of that pokemon, then fetching its default form
     * @param pokemonSearched
     * @param callback
     * @return A String with the JSON data of the pokemon to be parsed in the getPokemon Method
     */
    public void getPokemonFromSpecie(@NotNull String pokemonSearched, PokemonSpecieCallback callback) {
        String url = BASE_SPECIE_URL + pokemonSearched;

        Request specieRequest = new Request.Builder()
                .url(url)
                .build();

        Log.d("getPokemonFromSpecie", "Performing Request: " + url);
        client.newCall(specieRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("getPokemonFromSpecie", e.toString());
                requireActivity().runOnUiThread(() -> {
                    errorScreen.setVisibility(View.VISIBLE);
                    loadingScreen.setVisibility(View.GONE);
                    callback.onError("Failed to fetch species data");
                });
            }

            @Override
            public void onResponse(Call call, Response sr) throws IOException {
                if (sr.isSuccessful()) {
                    Log.d("getPokemonFromSpecie", "Successful Request");
                    String specieResponse = sr.body().string();

                    Log.d("getPokemonFromSpecie", "Parsing Json with specie data");
                    JsonObject specieResponseData = gson.fromJson(specieResponse, JsonObject.class);

                    // Obtén la URL del Pokémon
                    String pokemonSearchedURL = "";
                    for (JsonElement je : specieResponseData.getAsJsonArray("varieties")) {
                        if (je.getAsJsonObject().get("is_default").getAsBoolean()) {
                            pokemonSearchedURL = je.getAsJsonObject().getAsJsonObject("pokemon").get("url").getAsString();
                        }
                    }

                    // Realiza la solicitud al Pokémon
                    Request pokemonSearchedRequest = new Request.Builder()
                            .url(pokemonSearchedURL)
                            .build();

                    Log.d("getPokemonFromSpecie-pokemonSearchedRequest", "Performing request: "+pokemonSearchedURL);
                    client.newCall(pokemonSearchedRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("getPokemonFromSpecie-pokemonSearchedRequest", e.toString());
                            requireActivity().runOnUiThread(() -> {
                                errorScreen.setVisibility(View.VISIBLE);
                                loadingScreen.setVisibility(View.GONE);
                                callback.onError("Failed to fetch Pokémon data");
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response psr) throws IOException {
                            if (psr.isSuccessful()) {
                                Log.d("getPokemonFromSpecie-pokemonSearchedRequest", "Succesful Request");
                                String pokemonData = psr.body().string();
                                callback.onSuccess(pokemonData);
                            } else {
                                Log.d("getPokemonFromSpecie-pokemonSearchedRequest", "Error with request");
                                callback.onError("Pokémon request failed");
                            }
                        }
                    });
                } else {
                    Log.e("getPokemonFromSpecie", "Request Failed");
                    requireActivity().runOnUiThread(() -> {
                        errorScreen.setVisibility(View.VISIBLE);
                        loadingScreen.setVisibility(View.GONE);
                        callback.onError("Species request failed");
                    });
                }
            }
        });
    }
}