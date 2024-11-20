package com.reky0.mydex;

public interface PokemonSpecieCallback {
    void onSuccess(String pokemonData);
    void onError(String errorMessage);
}