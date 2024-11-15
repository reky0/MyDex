package com.reky0.mydex;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// GSON
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// POKEMON
import com.google.gson.reflect.TypeToken;
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
    private String pokemonURL;
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson =  new GsonBuilder().setPrettyPrinting().create();

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

        Intent intent = getIntent();
        pokemonURL = BASE_POKEMON_URL + intent.getStringExtra("name");
        Log.d("AAAAAAAAA", pokemonURL);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try {
            getPokemon(pokemonURL, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadPokemonInfo(Pokemon p, Context context) throws Exception {
        Log.d("AAA", "NAME = "+p.getName());

        name.setText(p.getName());
        id.setText(id.getText().toString() + p.getId());

        Glide.with(context)
            .load(p.getSprites().getFrontDefault())
            .into(portrait);

        type1.setText(p.getTypes().get(0).getType().getName());
        if (p.getTypes().size() == 2) {
            type2.setText(p.getTypes().get(1).getType().getName());
        } else {
            type2.setVisibility(View.INVISIBLE);
        }

        height.setText(height.getText().toString() + p.getHeight() + "m");
        weight.setText(weight.getText().toString() + p.getWeight() + "kg");

        isLegendary.setText(isLegendary.getText().toString() + ((p.isLegendary()) ? "Yes" : "No"));
        isMythical.setText(isMythical.getText().toString() + ((p.isMythical()) ? "Yes" : "No"));

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
    }

    // getSpecieData
    public static Pokemon getSpecieData(@NotNull Pokemon p) throws IOException, InterruptedException {
        Request request = new Request.Builder()
                .url(p.getSpecies().getUrl())
                .build();

        client.newCall(request).enqueue(new Callback() {
            // in case of failure
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            // in case of success
            @Override
            public void onResponse(Call call, Response r) throws IOException {
                if (r.isSuccessful()) {
                    Log.d("CHECKPOINT", "Successful Specie Request");
                    String response = r.body().string();

                    Log.d("CHECKPOINT", "Parsing Json to PokemonBatch");
                    try {
                        // parses to jsonobject
                        JsonObject speciesData = gson.fromJson(response, JsonObject.class);

                        // legendary and mythical are fields of the specie
                        p.setLegendary(speciesData.get("is_legendary").getAsBoolean());
                        p.setMythical(speciesData.get("is_mythical").getAsBoolean());

                        // ------------------------------------------------------------------
                        //                        EVOLUTION CHAIN
                        // ------------------------------------------------------------------

                        // from the specie data we get the evolution_chain url
                        String evolutionChainURL = speciesData.getAsJsonObject("evolution_chain").get("url").getAsString();

                        // request for the evolution chain
                        Request evolutionRequest = new Request.Builder()
                                .url(evolutionChainURL)
                                .build();

                        client.newCall(evolutionRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.e("ERROR", e.toString());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response r) throws IOException {
                                Log.d("CHECKPOINT", "Successful Evolution Request");
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
//            chain = !evolvesTo.isEmpty() ? chain : null;
                                }

                                // ------------------------------------------------------------------
                                //                            VARIETIES
                                // ------------------------------------------------------------------

                                for (JsonElement je : speciesData.getAsJsonArray("varieties")) {
                                    p.addVarietySlot(gson.fromJson(je.toString(), Pokemon.VarietySlot.class));
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ERROR", e.toString());
                    }
                } else {
                    Log.e("ERROR", "Request Failed");
                }
            }
        });

        return p;
    }

    public void getPokemon(@NotNull String url, Context context) throws Exception {
        final Pokemon[] pokemon = {new Pokemon()};

        // sets request
        Request request = new Request.Builder()
                .url(url)
                .build();

        Log.d("CHECKPOINT", "Performing Request");
        // performs request
        client.newCall(request).enqueue(new Callback() {
            // in case of failure
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            // in case of success
            @Override
            public void onResponse(Call call, Response r) throws IOException {
                if (r.isSuccessful()) {
                    Log.d("CHECKPOINT", "Successful Request");
                    String response = r.body().string();

                    Log.d("CHECKPOINT", "Parsing Json to PokemonBatch");
                    try {
                        // parses to jsonobject
                        JsonObject jo = gson.fromJson(response, JsonObject.class);
                        Log.d("HEIGHT", jo.get("height").getAsString());

                        // parsing data to Pokemon class
                        pokemon[0] = gson.fromJson(response, Pokemon.class);

                        // gets evolution chain and some other specie-related data
                        pokemon[0] = getSpecieData(pokemon[0]);

                        runOnUiThread(() -> {
                            try {
                                loadPokemonInfo(pokemon[0], context);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ERROR", "onResponse: "+e);
                    }
                } else {
                    Log.e("ERROR", "Request Failed");
                }

            }
        });
    }
}