package com.reky0.mydex;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

// OKHTTP
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// GSON
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
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

    private PokemonViewModel viewModel;

    private LottieAnimationView loadingScreen;
    private TextView loadingText;
    private ScrollView scrollView;
    private LinearLayout pokemonList;
    private TextInputLayout searchLayout;
    private EditText searchBar;

    private boolean isFetching = false;

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

        // gets a new ViewModel if doesnt exist, if not, gets the same that's been created sometime before
        viewModel = new ViewModelProvider(this).get(PokemonViewModel.class);

        loadingScreen = findViewById(R.id.loadingScreen);
        loadingScreen.setRepeatCount(LottieDrawable.INFINITE);
        loadingScreen.playAnimation();

        loadingText = findViewById(R.id.loadingText);

        scrollView = findViewById(R.id.pokemon_list_container);
        pokemonList = findViewById(R.id.pokemon_list);
        searchLayout = findViewById(R.id.searchLayout);
        searchBar = findViewById(R.id.searchBar);

        // if we're starting from scratch
        if (viewModel.showedData.isEmpty()) {
            loadNextBatch();
        } else { // if has been a change on the activity but there's previously retrieved data
            // reset pokedex number count since we'll show the data from the beginning
            pokedexNumberCount = 1;
            // create pokemonBatch with all data stored
            PokemonBatch storedData = new PokemonBatch();
            storedData.setBatch(viewModel.showedData);
            // update UI (which will be empty since it has been a change on the activity) with
            // the previously retrieved data
            updateUIWithBatch(storedData);
        }

        final ConstraintLayout layout = findViewById(R.id.main);

        searchLayout.setStartIconOnClickListener(v -> {
            Log.d("main", "Trying to search pokemon");
            if (searchBar.getText().toString().isEmpty()) {
                Snackbar.make(layout, "Empty Search Not Allowed", Snackbar.LENGTH_SHORT).show();
            } else {
                showPokemonInfo(searchBar.getText().toString().toLowerCase());
                searchBar.clearFocus();
                searchBar.setText("");
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                Log.d("main", "ENTER Pressed, trying to search pokemon");
                if (searchBar.getText().toString().isEmpty()) {
                    Snackbar.make(layout, "Empty Search Not Allowed", Snackbar.LENGTH_SHORT).show();
                } else {
                    showPokemonInfo(searchBar.getText().toString().toLowerCase());
                    searchBar.clearFocus();
                    searchBar.setText("");
                }
                return true; // Return true if the action was handled
            }
            return false; // Return false if the action was not handled
        });

        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v instanceof ScrollView) {
                ScrollView sv = (ScrollView) v;
                if (scrollY == (sv.getChildAt(0).getMeasuredHeight() - sv.getMeasuredHeight())) {
                    loadNextBatch();
                }
            }
        });
    }

    /**
     * Loads next pokemon batch
     */
    public void loadNextBatch() {
        String url = BASE_POKEMON_URL +"?offset="+offset+"&limit="+BATCH_SIZE;

        // so that the same request cannot be done twice at the same time
        if (isFetching) {
            Log.w("main-loadNextBatch", "Another request is already being processed.");
            return;
        }

        Log.w("main-loadNextBatch", "doing some things");

        isFetching = true;

        // sets request
        Request request = new Request.Builder()
                .url(url)
                .build();

        Log.d("main-loadNextBatch", "Performing Request");
        // performs request
        client.newCall(request).enqueue(new Callback() {
            // in case of failure
            @Override
            public void onFailure(Call call, IOException e) {
                isFetching = false;
                Log.e("main-loadNextBatch", e.toString());
            }

            // in case of success
            @Override
            public void onResponse(Call call, Response r) throws IOException {
                isFetching = false;
                if (r.isSuccessful()) {
                    Log.d("main-loadNextBatch", "Successful Request");
                    String response = r.body().string();

                    Log.d("main-loadNextBatch", "Parsing Json to PokemonBatch");
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
                        // adds all PokemonBatchSlot to the ViewModel in order to store it
                        viewModel.showedData.addAll(pokemonBatch.getBatch());

                        runOnUiThread(() -> updateUIWithBatch(pokemonBatch));
                        offset += BATCH_SIZE;
                    } catch (Exception e) {
                        Log.e("main-loadNextBatch", e.toString());
                    }
                } else {
                    Log.e("main-loadNextBatch", String.valueOf(r.code()));
                }
            }
        });
    }

    private void updateUIWithBatch(PokemonBatch batch) {
        Log.d("main-updateUIWithBatch", "updating UI");
        Log.d("main-updateUIWithBatch", batch.toString());
        loadingScreen.setVisibility(View.GONE);
        loadingScreen.cancelAnimation(); // stops animation
        loadingText.setVisibility(View.GONE);

        for (int i = 0; i < batch.getBatch().size(); i++) {
            PokemonBatchSlot slot = batch.getBatch().get(i);

            // Inflar la vista desde el XML
            LinearLayout pokemonView = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.pokemon_list_element_template, pokemonList, false);

            // Configurar los datos en la vista inflada
            TextView pokemonDexNumber = pokemonView.findViewById(R.id.id);
            TextView pokemonName = pokemonView.findViewById(R.id.name);
            ImageView pokemonPreview = pokemonView.findViewById(R.id.pokemonPreview);

            pokemonDexNumber.setText(pokemonDexNumber.getText().toString() + pokedexNumberCount); // Asigna el ID del Pokémon
            pokemonName.setText(slot.getName()); // Asigna el nombre del Pokémon

            String pokemonPreviewURL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"+pokedexNumberCount+".png";

            Glide.with(this)
                .load(pokemonPreviewURL)
                .error(pokemonPreviewURL) // if fails load retries to load once again the img
                .into(pokemonPreview);

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