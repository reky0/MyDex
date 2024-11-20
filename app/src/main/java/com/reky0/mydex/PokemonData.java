package com.reky0.mydex;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.airbnb.lottie.LottieAnimationView;

// GSON
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// POKEMON
import com.reky0.mydex.pokemon.Pokemon;
import com.reky0.mydex.pokemon.PokemonBatch;
// JETBRAINS
import org.jetbrains.annotations.NotNull;

// JAVA STANDARD CLASSES
import java.io.IOException;
import java.util.ArrayList;

// OKHTTP3
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PokemonData extends AppCompatActivity {
    private static final String BASE_POKEMON_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final String BASE_SPECIE_URL = "https://pokeapi.co/api/v2/pokemon-species/";
    private String pokemonSearched;
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson =  new GsonBuilder().setPrettyPrinting().create();

    private static PokemonDataViewModel savedPokemon;

    private static LottieAnimationView loadingScreen;
    private static TextView errorScreen;
    private static ImageView logo;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pokemon_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        savedPokemon = new ViewModelProvider(this).get(PokemonDataViewModel.class);

        loadingScreen = findViewById(R.id.loadingScreen);
        loadingScreen.setRepeatCount(LottieDrawable.INFINITE);
        loadingScreen.playAnimation();

        errorScreen = findViewById(R.id.errorScreen);
        logo = findViewById(R.id.logo);
        name = findViewById(R.id.name);
        id = findViewById(R.id.pokedex_number);
        portrait = findViewById(R.id.portrait);
        type1 = findViewById(R.id.type1);
        type2 = findViewById(R.id.type2);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        isLegendary = findViewById(R.id.isLegendary);
        isMythical = findViewById(R.id.isMythical);
        cryButton = findViewById(R.id.cryButton);
        pokemonSprites = findViewById(R.id.pokemonSprites);

        errorScreen.setVisibility(View.GONE);

        logo.setOnClickListener(v -> finish());

        if (savedPokemon.savedPokemon != null) {
            loadPokemonInfo(savedPokemon.savedPokemon, this);
        } else {
            Intent intent = getIntent();
            pokemonSearched = intent.getStringExtra("name");
            Log.d("PokemonData-onCreate", pokemonSearched);

            // closes view when clicking the logo (sends back to main menu)

            try {
                getPokemon(pokemonSearched, this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void loadPokemonInfo(Pokemon p, Context context) {
        // when loading info on screen, also loads that pokemon to the ViewModel so that if the
        // activity has any change (eg.-change of theme mode) doesn't need to rerun the request
        savedPokemon.savedPokemon = p;

        Log.d("loadPokemonInfo", "NAME = "+p.getName());
        loadingScreen.setVisibility(View.GONE);

        name.setText(p.getName());
        id.setText(id.getText().toString() + p.getId());

        Glide.with(context)
            .load(p.getSprites().getFrontDefault())
            .into(portrait);

        setType(type1, p.getTypes().get(0).getType().getName(), context);
        if (p.getTypes().size() == 2) {
            setType(type2, p.getTypes().get(1).getType().getName(), context);
        } else {
            type2.setVisibility(View.INVISIBLE);
        }

        height.setText(height.getText().toString() + p.getHeight() + "m");
        weight.setText(weight.getText().toString() + p.getWeight() + "kg");


//        isLegendary.setText(isLegendary.getText().toString() + p.isLegendary());
//        isMythical.setText(isMythical.getText().toString() + p.isMythical());

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
                                                runOnUiThread(() -> {
                                                    try {
                                                        loadPokemonInfo(pokemon[0], context);
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

                                    runOnUiThread(() -> {
                                        try {
                                            loadPokemonInfo(pokemon[0], context);
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
                                runOnUiThread(() -> {
                                    try {
                                        loadPokemonInfo(pokemon[0], context);
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
                runOnUiThread(() -> {
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
                            runOnUiThread(() -> {
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
                    runOnUiThread(() -> {
                        errorScreen.setVisibility(View.VISIBLE);
                        loadingScreen.setVisibility(View.GONE);
                        callback.onError("Species request failed");
                    });
                }
            }
        });
    }
}