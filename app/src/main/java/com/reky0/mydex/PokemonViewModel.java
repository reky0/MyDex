package com.reky0.mydex;

import androidx.lifecycle.ViewModel;

import com.reky0.mydex.pokemon.PokemonBatch;

import java.util.ArrayList;

/**
 * ViewModel that stores the fetched data as long as the owner activity is closed.<br><br>
 * Meant to store all the data retrieved in the Main activity so that the info is not lost and <br>
 * can persist in the lifecycle of the app.
 */
public class PokemonViewModel extends ViewModel {
    public ArrayList<PokemonBatch.PokemonBatchSlot> showedData = new ArrayList<>();
}