package com.reky0.mydex.pokemon;

// Gson for JSON use
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
// requests
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
// jetbrains
import org.jetbrains.annotations.NotNull;

// other Java standard libraries
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.net.URL;

public class SearchAndroidVer {
    private static final String BASE_POKEMON_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final String BASE_SPECIE_URL = "https://pokeapi.co/api/v2/pokemon-species/";
    private static final int BATCH_SIZE = 20;

    // requests from Java apps
//    private HttpClient httpClient;
    // requests from Android apps
    OkHttpClient client;
    private Gson gson;
    private Scanner scanner;

    public SearchAndroidVer() {
//        httpClient = HttpClient.newHttpClient();
        client = new OkHttpClient();
        GsonBuilder gb = new GsonBuilder().setPrettyPrinting();
        gson = gb.create();
        scanner = new Scanner(System.in, "CP850");
    }

    public void loadAllPokemon() {
        Scanner scanner = new Scanner(System.in);
        String opt = "";
        int offset = 0;
        boolean hasMore = true;
        String url = BASE_POKEMON_URL +"?offset=0&limit="+BATCH_SIZE;
        String response;
        int statusCode;

        while (hasMore) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try (Response r = client.newCall(request).execute()) {
                    assert r.body() != null;
                    response = r.body().string();
                    statusCode = r.code();
                }


                // parses to jsonobject
                JsonObject jo = gson.fromJson(response, JsonObject.class);

                // next batch's url
                url = jo.get("next").getAsString();

                // jsonarray with all pokemon found
                JsonArray ja = jo.getAsJsonArray("results");
                ArrayList data = gson.fromJson(ja.toString(), ArrayList.class);
                ArrayList<PokemonBatch.PokemonBatchSlot> data2 = gson.fromJson(jo.getAsJsonArray("results").toString(), new TypeToken<ArrayList<PokemonBatch.PokemonBatchSlot>>(){}.getType());
                PokemonBatch pokemonBatch = new PokemonBatch();
                pokemonBatch.setBatch(data2);

                if (statusCode != 200) {
                    System.out.println("Error: " + statusCode);
                }

                if (data.isEmpty()) {
                    hasMore = false;  // stop if no more pokemon
                } else {
                    // process the batch
                    // print all pokemon in the batch
                    for (int i = 0; i < pokemonBatch.getBatch().size(); i++) {
                        System.out.println(String.format("%d - %s", offset+1+i, pokemonBatch.getBatch().get(i).getName()));
                    }
                    System.out.println(String.format("\n%d/%d", offset, offset+BATCH_SIZE));
                    System.out.println("·············································");
                    System.out.println("'N' -> NEXT PAGE"); // page flip simulation
                    System.out.println("NUMBER -> SHOW POKEMON DATA"); // page flip simulation

                    try {
                        opt = scanner.next();

                        offset += BATCH_SIZE;  // moves to next batch
                        if (!opt.equalsIgnoreCase("n")) {
                            printPokemonData(opt);
                        }
                    } catch (Exception e) {
                        scanner = new Scanner(System.in);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                break;  // end search if error
            }
        }
    }

    public Pokemon getSpecieData(@NotNull Pokemon p) throws IOException, InterruptedException {
        String speciesResponse;
        String evolutionResponse;

        Request request = new Request.Builder()
                .url(p.getSpecies().getUrl())
                .build();

        try (Response r = client.newCall(request).execute()) {
            assert r.body() != null;
            speciesResponse = r.body().string();
        }

        JsonObject speciesData = gson.fromJson(speciesResponse, JsonObject.class);

        // legendary and mythical are fields of the specie
        p.setLegendary(speciesData.get("is_legendary").getAsBoolean());
        p.setMythical(speciesData.get("is_mythical").getAsBoolean());

//        System.out.println(p.isLegendary());
//        System.out.println(p.isMythical());

        // ------------------------------------------------------------------
        //                        EVOLUTION CHAIN
        // ------------------------------------------------------------------

        // from the specie data we get the evolution_chain url
        String evolutionChainURL = speciesData.getAsJsonObject("evolution_chain").get("url").getAsString();

        System.out.println(evolutionChainURL);

        // request for the evolution chain
        Request evolutionRequest = new Request.Builder()
                .url(evolutionChainURL)
                .build();

        try (Response r = client.newCall(evolutionRequest).execute()) {
            assert r.body() != null;
            evolutionResponse = r.body().string();
        }

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

        return p;
    }

    public Pokemon getPokemon(@NotNull String pokemonSearched) throws Exception {
        String url = BASE_POKEMON_URL +pokemonSearched;
        Pokemon pokemon = new Pokemon();
        String response;
        int statusCode;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response r = client.newCall(request).execute()) {
            response = r.body().string();
            statusCode = r.code();
        }

        // System.out.println(response.body());

        // status code assertion
        if (statusCode!= 200) {
            switch (statusCode) {
                case 404: // if the pokemon isnt found, it tries searching from the specie, then finding the default variation
                    pokemon = gson.fromJson(getPokemonFromSpecie(pokemonSearched), Pokemon.class);
            }
        } else {
            // parsing data to Pokemon class
            pokemon = gson.fromJson(response, Pokemon.class);
        }

        // gets evolution chain and some other specie-related data
        pokemon = getSpecieData(pokemon);

        return pokemon;
    }

    public String getPokemonFromSpecie(@NotNull String pokemonSearched) throws Exception {
        String url = BASE_SPECIE_URL + pokemonSearched;
        String pokemonSearchedURL = "";
        String specieResponse;
        int specieResponseStatusCode;
        String pokemonSearchedResponse = "";

        Request specieRequest = new Request.Builder()
                .url(url)
                .build();

        try (Response r = client.newCall(specieRequest).execute()) {
            specieResponse = r.body().string();
            specieResponseStatusCode = r.code();
        }


        JsonObject specieResponseData = gson.fromJson(specieResponse, JsonObject.class);

        // status code assertion
        if (specieResponseStatusCode != 200) {
            switch (specieResponseStatusCode) {
                case 404:
                    throw new Exception("Request error: Pokémon \"" + pokemonSearched + "\" not found");
            }
        } else {
            for (JsonElement je : specieResponseData.getAsJsonArray("varieties")) {
                if (je.getAsJsonObject().get("is_default").getAsBoolean()) {
                    pokemonSearchedURL = je.getAsJsonObject().getAsJsonObject("pokemon").get("url").getAsString();
                }
            }

            Request pokemonSearchedRequest = new Request.Builder()
                    .url(pokemonSearchedURL)
                    .build();

            try (Response r = client.newCall(specieRequest).execute()) {
                pokemonSearchedResponse = r.body().string();
            }
        }

        return pokemonSearchedResponse;
    }

    public void printPokemonData(String pokemonSearched) throws Exception {
        String evolResponse;
        // instantiates self
        SearchAndroidVer loader = new SearchAndroidVer();

        // pokemon search
        Pokemon pokemon = loader.getPokemon(pokemonSearched);

        System.out.println("Pokédex number: " + pokemon.getId());
        System.out.println("Name: " + pokemon.getName());
        System.out.println("Height: " + pokemon.getHeight() + "M");
        System.out.println("Weight: " + pokemon.getWeight() + "KG");
        System.out.println("Legendary: " + pokemon.isLegendary());
        System.out.println("Mythical: " + pokemon.isMythical());

        System.out.println("\n------------------------------------\nMoves\n------------------------------------");
//        System.out.println(pokemon.getMoves());
        for (Pokemon.MoveSlot ms : pokemon.getMoves()) {
            System.out.println(" - " + ms.getMove().getName());
        }

        System.out.println("\n------------------------------------\nTypes\n------------------------------------");
//        System.out.println(pokemon.getTypes());
        for (Pokemon.TypeSlot ts : pokemon.getTypes()) {
            System.out.println(" - " + ts.getType().getName());
        }

        System.out.println("\n------------------------------------\nSprites\n------------------------------------");
        System.out.println(pokemon.getSprites());

        System.out.println("\n------------------------------------\nStats\n------------------------------------");
//        System.out.println(pokemon.getStats());
        for (Pokemon.StatSlot ss : pokemon.getStats()) {
            System.out.println(" - " + ss.getStat().getName() + ": "+ss.getBaseStat());
        }

        System.out.println("\n------------------------------------\nEvolution chain\n------------------------------------");
//        System.out.println(pokemon.getEvolutionChain());
        for (String evolURL : pokemon.getEvolutionChain()) {
            Request request = new Request.Builder()
                    .url(evolURL)
                    .build();

            try (Response r = client.newCall(request).execute()) {
                assert r.body() != null;
                evolResponse = r.body().string();
            }

            System.out.println(" - " + loader.gson.fromJson(evolResponse, Pokemon.class).getName());
        }

        System.out.println("\n------------------------------------\nVarieties\n------------------------------------");
//        System.out.println(pokemon.getVarieties());
        for (Pokemon.VarietySlot vs : pokemon.getVarieties()) {
            System.out.println(" - " + vs.getVariety().getName() + " -> "+vs.getVariety().getUrl());
        }
    }

    public static void main(String[] args) throws Exception {
        String pokemonSearched = "charizard";
        SearchAndroidVer main = new SearchAndroidVer();

        main.loadAllPokemon();

//        main.showPokemonData(pokemonSearched);
    }
}
