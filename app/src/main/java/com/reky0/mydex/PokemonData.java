package com.reky0.mydex;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.airbnb.lottie.LottieAnimationView;

// GSON
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// POKEMON
import com.reky0.mydex.pokemon.Pokemon;
// JETBRAINS
import org.jetbrains.annotations.NotNull;

// JAVA STANDARD CLASSES
import java.io.IOException;

// OKHTTP3
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PokemonData extends AppCompatActivity {
    private PDInfo pdInfo;
    private PDMovements pdMovements;
    private PDEvoChain pdEvoChain;
    private PDStats pdStats;

    private String pokemonSearched;

    private ImageView logo;
    private BottomNavigationView bottomNav;

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

        Intent intent = getIntent();
        pokemonSearched = intent.getStringExtra("name");

        pdInfo = PDInfo.newInstance(pokemonSearched);
        pdMovements = new PDMovements();
        pdEvoChain = new PDEvoChain();
        pdStats = new PDStats();

        logo = findViewById(R.id.logo);
        bottomNav = findViewById(R.id.bottomNav);

        // closes view when clicking the logo (sends back to main menu)
        logo.setOnClickListener(v -> finish());

        disableBottomNavButtons();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.info) {
                Toast.makeText(this, "To INFO", Toast.LENGTH_SHORT).show();

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, pdInfo, "info")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            } else if (id == R.id.movements) {
                Toast.makeText(this, "To MOVEMENTS", Toast.LENGTH_SHORT).show();

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, pdMovements, "movements")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            } else if (id == R.id.evolutionChain) {
                Toast.makeText(this, "To EVO.CHAIN", Toast.LENGTH_SHORT).show();

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, pdEvoChain, "evo_chain")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            } else if (id == R.id.stats) {
                Toast.makeText(this, "To STATS", Toast.LENGTH_SHORT).show();

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, pdStats, "stats")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            }

            return true;
        });

//        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, pdInfo, "info").commit();

        if (savedInstanceState != null) {
            // Restore the last fragment
            String lastFragmentTag = savedInstanceState.getString("last_fragment");
            Fragment lastFragment = getSupportFragmentManager().findFragmentByTag(lastFragmentTag);
            if (lastFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, lastFragment, lastFragmentTag)
                        .commit();
            }
        } else {
            // Load the default fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, pdInfo, "info").commit();
        }
    }

    public void disableBottomNavButtons() {
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setEnabled(false);
        }
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            Log.d("PokemonData-disableBottomNavButtons", "bottomNavMenu-isEnabled: "+bottomNav.getMenu().getItem(i).isEnabled());
        }
    }

    public void enableBottomNavButtons() {
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setEnabled(true);
        }
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            Log.d("PokemonData-enableBottomNavButtons", "bottomNavMenu-isEnabled: "+bottomNav.getMenu().getItem(i).isEnabled());
        }
    }
}