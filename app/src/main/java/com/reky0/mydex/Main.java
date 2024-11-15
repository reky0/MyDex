package com.reky0.mydex;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// OKHTTP
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// GSON
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.reky0.mydex.pokemon.PokemonBatch;
import com.reky0.mydex.pokemon.PokemonBatch.PokemonBatchSlot;

import java.io.IOException;
import java.util.ArrayList;

public class Main extends AppCompatActivity {
    private static final String BASE_POKEMON_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final int BATCH_SIZE = 20;
    private static int offset = 0;
    private static int pokedexNumberCount = 1;
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson =  new GsonBuilder().setPrettyPrinting().create();


    private ArrayList<PokemonBatchSlot> showedData = new ArrayList<>();
    private ArrayList<PokemonBatchSlot> batchBuffer = new ArrayList<>();

    private ScrollView scrollView;
    private LinearLayout pokemonList;
    private TextInputLayout searchLayout;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pokemonList = findViewById(R.id.pokemon_list);
        scrollView = findViewById(R.id.pokemon_list_container);
        searchLayout = findViewById(R.id.searchLayout);
        searchBar = findViewById(R.id.searchBar);

        loadNextBatch();

        final ConstraintLayout layout = findViewById(R.id.main);

        searchLayout.setStartIconOnClickListener(v -> {
            Log.d("CHECKPOINT", "Trying to search pokemon");
            if (searchBar.getText().toString().isEmpty()) {
                Snackbar.make(layout, "Empty Search Not Allowed", Snackbar.LENGTH_SHORT).show();
            } else {
                showPokemonInfo(searchBar.getText().toString());
                searchBar.clearFocus();
                searchBar.setText("");
            }
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Check if the action is "done" (Enter key pressed)
                if (actionId == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("CHECKPOINT", "ENTER Pressed, trying to search pokemon");
                    if (searchBar.getText().toString().isEmpty()) {
                        Snackbar.make(layout, "Empty Search Not Allowed", Snackbar.LENGTH_SHORT).show();
                    } else {
                        showPokemonInfo(searchBar.getText().toString());
                        searchBar.clearFocus();
                        searchBar.setText("");
                    }
                    return true; // Return true if the action was handled
                }
                return false; // Return false if the action was not handled
            }
        });

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (v instanceof ScrollView) {
                    ScrollView sv = (ScrollView) v;
                    if (scrollY == (sv.getChildAt(0).getMeasuredHeight() - sv.getMeasuredHeight())) {
                        loadNextBatch();
                    }
                }
            }
        });
    }

    /**
     * Loads next pokemon batch
     */
    public void loadNextBatch() {
        String url = BASE_POKEMON_URL +"?offset="+offset+"&limit="+BATCH_SIZE;

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

                        // next batch's url
//                        url = jo.get("next").getAsString();

                        // jsonarray with all pokemon found
                        JsonArray ja = jo.getAsJsonArray("results");
                        ArrayList data = gson.fromJson(ja.toString(), ArrayList.class);
                        ArrayList<PokemonBatch.PokemonBatchSlot> data2 = gson.fromJson(jo.getAsJsonArray("results").toString(), new TypeToken<ArrayList<PokemonBatch.PokemonBatchSlot>>(){}.getType());
                        PokemonBatch pokemonBatch = new PokemonBatch();
                        pokemonBatch.setBatch(data2);
                        runOnUiThread(() -> updateUIWithBatch(pokemonBatch));
                        offset += BATCH_SIZE;
                    } catch (Exception e) {
                        Log.e("ERROR", e.toString());
                    }
                } else {
                    Log.e("ERROR", String.valueOf(r.code()));
                }
            }
        });
    }

    private void updateUIWithBatch(PokemonBatch batch) {
        Log.d("CHECKPOINT", "updating UI");
        Log.d("CHECKPOINT", batch.toString());

        for (int i = 0; i < batch.getBatch().size(); i++) {
            PokemonBatchSlot slot = batch.getBatch().get(i);

            // Inflar la vista desde el XML
            LinearLayout pokemonView = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.pokemon_list_element_template, pokemonList, false);

            // Configurar los datos en la vista inflada
            TextView idTextView = pokemonView.findViewById(R.id.id);
            TextView nameTextView = pokemonView.findViewById(R.id.name);

            idTextView.setText(String.valueOf(pokedexNumberCount)); // Asigna el ID del Pokémon
            nameTextView.setText(slot.getName()); // Asigna el nombre del Pokémon

            pokemonView.setOnClickListener(view -> {
                showPokemonInfo(slot.getName());
            });

            // Añadir la vista al contenedor
            pokemonList.addView(pokemonView);
            pokedexNumberCount++;
        }
    }

    private void showPokemonInfo(String name) {
        Intent intent = new Intent(this, PokemonData.class);
        intent.putExtra("name", name);
        startActivity(intent);
    }
}