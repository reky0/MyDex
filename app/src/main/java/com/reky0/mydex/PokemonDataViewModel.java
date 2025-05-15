package com.reky0.mydex;

import androidx.lifecycle.ViewModel;
import com.reky0.mydex.pokemon.Pokemon;

public class PokemonDataViewModel extends ViewModel {
     private Pokemon savedPokemon;

     public Pokemon getSavedPokemon() {
          return savedPokemon;
     }

     public void setSavedPokemon(Pokemon savedPokemon) {
          this.savedPokemon = savedPokemon;
     }
}
