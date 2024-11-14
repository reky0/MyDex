//package com.reky0.mydex.pokemon;
//
//import com.google.gson.*;
//import com.google.gson.reflect.TypeToken;
//import org.jetbrains.annotations.NotNull;
//
//import javax.swing.*;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URL;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.ArrayList;
//import java.util.Scanner;
//
//public class Main {
//    private static final String BASE_POKEMON_URL = "https://pokeapi.co/api/v2/pokemon/";
//    private static final String BASE_SPECIE_URL = "https://pokeapi.co/api/v2/pokemon-species/";
//    private static final int BATCH_SIZE = 10;
//
//    private HttpClient httpClient;
//    private Gson gson;
//    private Scanner scanner;
//    private JFrame detailsFrame = null;
//
//    public Main() {
//        httpClient = HttpClient.newHttpClient();
//        GsonBuilder gb = new GsonBuilder().setPrettyPrinting();
//        gson = gb.create();
//        scanner = new Scanner(System.in, "CP850");
//    }
//
//    public void loadAllPokemon() {
//        Scanner scanner = new Scanner(System.in);
//        String opt = "";
//        int offset = 0;
//        boolean hasMore = true;
//        String url = BASE_POKEMON_URL +"?offset=0&limit="+BATCH_SIZE;
//
//        while (hasMore) {
//            try {
//                // sets request
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create(url))
//                        .GET()
//                        .build();
//                // send request
//                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//                // for test purposes
////                System.out.println(response.body());
//                // parses to jsonobject
//                JsonObject jo = gson.fromJson(response.body(), JsonObject.class);
//                // next batch's url
//                url = jo.get("next").getAsString();
//                // jsonarray with all pokemon found
//                JsonArray ja = jo.getAsJsonArray("results");
//                // for test purposes
////                System.out.println(ja);
//                ArrayList data = gson.fromJson(jo.getAsJsonArray("results").toString(), ArrayList.class);
//                ArrayList<PokemonBatch.PokemonBatchSlot> data2 = gson.fromJson(jo.getAsJsonArray("results").toString(), new TypeToken<ArrayList<PokemonBatch.PokemonBatchSlot>>(){}.getType());
//                PokemonBatch pokemonBatch = new PokemonBatch();
//                pokemonBatch.setBatch(data2);
//
//                if (response.statusCode() != 200) {
//                    System.out.println("Error: " + response.statusCode());
//                }
//
//                if (data.isEmpty()) {
//                    hasMore = false;  // stop if no more pokemon
//                } else {
//                    // process the batch
//                    // print all pokemon in the batch
//                    for (int i = 0; i < pokemonBatch.getBatch().size(); i++) {
//                        System.out.println(String.format("%d - %s", offset+1+i, pokemonBatch.getBatch().get(i).getName()));
//                    }
//                    System.out.println(String.format("\n%d/%d", offset, offset+BATCH_SIZE));
//                    System.out.println("·············································");
//                    System.out.println("'N' -> NEXT PAGE"); // page flip simulation
//                    System.out.println("NUMBER -> SHOW POKEMON DATA"); // page flip simulation
//
//                    try {
//                        opt = scanner.next();
//
//                        offset += BATCH_SIZE;  // moves to next batch
//                        if (!opt.equalsIgnoreCase("n")) {
//                            showPokemonData(opt);
//                        }
//                    } catch (Exception e) {
//                        scanner = new Scanner(System.in);
//                    }
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                break;  // end search if error
//            }
//        }
//    }
//
//    public Pokemon getSpecieData(@NotNull Pokemon p) throws IOException, InterruptedException {
//        HttpRequest speciesRequest = HttpRequest.newBuilder()
//                .uri(URI.create(p.getSpecies().getUrl()))
//                .GET()
//                .build();
//
//        // makes request for specie
//        HttpResponse<String> speciesResponse = httpClient.send(speciesRequest, HttpResponse.BodyHandlers.ofString());
//        JsonObject speciesData = gson.fromJson(speciesResponse.body(), JsonObject.class);
//
//        // legendary and mythical are fields of the specie
//        p.setLegendary(speciesData.get("is_legendary").getAsBoolean());
//        p.setMythical(speciesData.get("is_mythical").getAsBoolean());
//
//        // ------------------------------------------------------------------
//        //                        EVOLUTION CHAIN
//        // ------------------------------------------------------------------
//
//        // from the specie data we get the evolution_chain url
//        String evolutionChainURL = speciesData.getAsJsonObject("evolution_chain").get("url").getAsString();
//        // request for the evolution chain
//        HttpRequest evolutionRequest = HttpRequest.newBuilder()
//                .uri(URI.create(evolutionChainURL))
//                .GET()
//                .build();
//        HttpResponse<String> evolutionResponse = httpClient.send(evolutionRequest, HttpResponse.BodyHandlers.ofString());
//        JsonObject evolutionChainData = gson.fromJson(evolutionResponse.body(), JsonObject.class);
//
//        // we isolate the chain field
//        JsonObject chain = evolutionChainData.getAsJsonObject("chain");
//
//        // adds first stage to the evolution chain
//        String speciesURL = chain.getAsJsonObject("species").get("url").getAsString();
//        p.addEvolution(speciesURL);
//
//        // adding each stage of the chain to the pokemon evolution_chain ArrayList
//        while (chain != null) {
//            // gets the next stage
//            JsonArray evolvesTo = chain.getAsJsonArray("evolves_to");
//
//            // for each possible evolution in that stage, adds its URL (like pokemons suchs as Gardevoir/Gallade)
//            for (JsonElement je : evolvesTo) {
//                p.addEvolution(((JsonObject) je).getAsJsonObject("species").get("url").getAsString());
//            }
//
//            if (evolvesTo.isEmpty()) { // if it has not more content (is in last stage) then sets to null -> end loop
//                chain = null;
//            } else { // if it has content (there's a next stage) shortens the chain,
//                chain = evolvesTo.get(0).getAsJsonObject();
//            }
////            chain = !evolvesTo.isEmpty() ? chain : null;
//        }
//
//        // ------------------------------------------------------------------
//        //                            VARIETIES
//        // ------------------------------------------------------------------
//
//        for (JsonElement je : speciesData.getAsJsonArray("varieties")) {
//            p.addVarietySlot(gson.fromJson(je.toString(), Pokemon.VarietySlot.class));
//        }
//
//        return p;
//    }
//
//    public Pokemon getPokemon(@NotNull String pokemonSearched) throws Exception {
//        String url = BASE_POKEMON_URL +pokemonSearched;
//        Pokemon pokemon = new Pokemon();
//
//        // sets request for the pokemon
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .GET()
//                .build();
//        // sends request
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        // System.out.println(response.body());
//
//        // status code assertion
//        if (response.statusCode() != 200) {
//            switch (response.statusCode()) {
//                case 404: // if the pokemon isnt found, it tries searching from the specie, then finding the default variation
//                    pokemon = gson.fromJson(getPokemonFromSpecie(pokemonSearched), Pokemon.class);
//            }
//        } else {
//            // parsing data to Pokemon class
//            pokemon = gson.fromJson(response.body(), Pokemon.class);
//        }
//
//        // gets evolution chain and some other specie-related data
//        pokemon = getSpecieData(pokemon);
//
//        return pokemon;
//    }
//
//    public String getPokemonFromSpecie(@NotNull String pokemonSearched) throws Exception {
//        String url = BASE_SPECIE_URL + pokemonSearched;
//        String pokemonSearchedURL = "";
//
//        // sets request for the pokemon
//        HttpRequest specieRequest = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .GET()
//                .build();
//        // sends request
//        HttpResponse<String> specieResponse = httpClient.send(specieRequest, HttpResponse.BodyHandlers.ofString());
//        JsonObject specieResponseData = gson.fromJson(specieResponse.body(), JsonObject.class);
//
//        // status code assertion
//        HttpResponse<String> pokemonSearchedResponse = null;
//        if (specieResponse.statusCode() != 200) {
//            switch (specieResponse.statusCode()) {
//                case 404:
//                    throw new Exception("Request error: Pokémon \"" + pokemonSearched + "\" not found");
//            }
//        } else {
//            for (JsonElement je : specieResponseData.getAsJsonArray("varieties")) {
//                if (je.getAsJsonObject().get("is_default").getAsBoolean()) {
//                    pokemonSearchedURL = je.getAsJsonObject().getAsJsonObject("pokemon").get("url").getAsString();
//                }
//            }
//
//            HttpRequest pokemonSearchedRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(pokemonSearchedURL))
//                    .GET()
//                    .build();
//            // sends request
//            pokemonSearchedResponse = httpClient.send(pokemonSearchedRequest, HttpResponse.BodyHandlers.ofString());
//        }
//
//        return pokemonSearchedResponse.body();
//    }
//
//    public void printPokemonData(String pokemonSearched) throws Exception {
//        // instantiates self
//        Main loader = new Main();
//
//        // pokemon search
//        Pokemon pokemon = loader.getPokemon(pokemonSearched);
//
//        System.out.println("Pokédex number: " + pokemon.getId());
//        System.out.println("Name: " + pokemon.getName());
//        System.out.println("Height: " + pokemon.getHeight() + "M");
//        System.out.println("Weight: " + pokemon.getWeight() + "KG");
//        System.out.println("Legendary: " + pokemon.isLegendary());
//        System.out.println("Mythical: " + pokemon.isMythical());
//
//        System.out.println("\n------------------------------------\nMoves\n------------------------------------");
////        System.out.println(pokemon.getMoves());
//        for (Pokemon.MoveSlot ms : pokemon.getMoves()) {
//            System.out.println(" - " + ms.getMove().getName());
//        }
//
//        System.out.println("\n------------------------------------\nTypes\n------------------------------------");
////        System.out.println(pokemon.getTypes());
//        for (Pokemon.TypeSlot ts : pokemon.getTypes()) {
//            System.out.println(" - " + ts.getType().getName());
//        }
//
//        System.out.println("\n------------------------------------\nSprites\n------------------------------------");
//        System.out.println(pokemon.getSprites());
//
//        System.out.println("\n------------------------------------\nStats\n------------------------------------");
////        System.out.println(pokemon.getStats());
//        for (Pokemon.StatSlot ss : pokemon.getStats()) {
//            System.out.println(" - " + ss.getStat().getName() + ": "+ss.getBaseStat());
//        }
//
//        System.out.println("\n------------------------------------\nEvolution chain\n------------------------------------");
////        System.out.println(pokemon.getEvolutionChain());
//        for (String evolURL : pokemon.getEvolutionChain()) {
//            HttpRequest evolRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(evolURL))
//                    .GET()
//                    .build();
//            HttpResponse<String> evolResponse = loader.httpClient.send(evolRequest, HttpResponse.BodyHandlers.ofString());
//            System.out.println(" - " + loader.gson.fromJson(evolResponse.body(), Pokemon.class).getName());
//        }
//
//        System.out.println("\n------------------------------------\nVarieties\n------------------------------------");
////        System.out.println(pokemon.getVarieties());
//        for (Pokemon.VarietySlot vs : pokemon.getVarieties()) {
//            System.out.println(" - " + vs.getVariety().getName() + " -> "+vs.getVariety().getUrl());
//        }
//    }
//
//    public void showPokemonData(String pokemonSearched) throws Exception {
//        Main loader = new Main();
//        Pokemon p = loader.getPokemon(pokemonSearched);
//        String pokemonData = "";
//        ArrayList<String> imagePaths = new ArrayList<>(4);
//
//        pokemonData += "Pokédex number: " + p.getId();
//        pokemonData += "\nName: " + p.getName();
//        pokemonData += "\nHeight: " + p.getHeight() + "M";
//        pokemonData += "\nWeight: " + p.getWeight() + "KG";
//        pokemonData += "\nLegendary: " + p.isLegendary();
//        pokemonData += "\nMythical: " + p.isMythical();
//
//        pokemonData += "\n------------------------------------\nMoves\n------------------------------------";
////        System.out.println(pokemon.getMoves());
//        for (Pokemon.MoveSlot ms : p.getMoves()) {
//            pokemonData += "\n - " + ms.getMove().getName();
//        }
//
//        pokemonData += "\n------------------------------------\nTypes\n------------------------------------";
////        System.out.println(pokemon.getTypes());
//        for (Pokemon.TypeSlot ts : p.getTypes()) {
//            pokemonData += "\n - " + ts.getType().getName();
//        }
//
//        imagePaths.add(p.getSprites().getFrontDefault());
//        imagePaths.add(p.getSprites().getBackDefault());
//        imagePaths.add(p.getSprites().getFrontShiny());
//        imagePaths.add(p.getSprites().getBackShiny());
//
//        pokemonData += "\n------------------------------------\nStats\n------------------------------------";
////        System.out.println(pokemon.getStats());
//        for (Pokemon.StatSlot ss : p.getStats()) {
//            pokemonData += "\n - " + ss.getStat().getName() + ": "+ss.getBaseStat();
//        }
//
//        pokemonData += "\n------------------------------------\nEvolution chain\n------------------------------------";
////        System.out.println(pokemon.getEvolutionChain());
//        for (String evolURL : p.getEvolutionChain()) {
//            HttpRequest evolRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(evolURL))
//                    .GET()
//                    .build();
//            HttpResponse<String> evolResponse = loader.httpClient.send(evolRequest, HttpResponse.BodyHandlers.ofString());
//            pokemonData += "\n - " + loader.gson.fromJson(evolResponse.body(), Pokemon.class).getName();
//        }
//
//        pokemonData += "\n------------------------------------\nVarieties\n------------------------------------";
////        System.out.println(pokemon.getVarieties());
//        for (Pokemon.VarietySlot vs : p.getVarieties()) {
//            pokemonData += "\n - " + vs.getVariety().getName() + " -> "+vs.getVariety().getUrl();
//        }
//
////        // Create a new JFrame for the window
////        JFrame frame = new JFrame("Pokémon Data");
////        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
////        frame.setSize(400, 600); // Adjust size as needed
////
////        // Create a JTextArea to display data, and set it as non-editable
////        JTextArea textArea = new JTextArea();
////        textArea.setEditable(false);
////
////        // Load Pokémon data into the JTextArea
////        textArea.append(pokemonData);
////
////        // Wrap the JTextArea in a JScrollPane for scrollability
////        JScrollPane scrollPane = new JScrollPane(textArea);
////
////        // Add the scroll pane to the frame's content pane
////        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
////
////        // Make the frame visible
////        frame.setVisible(true);
//
//        // Create a new JFrame for the window
//        JFrame frame = new JFrame("Pokémon Data & Images");
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.setSize(500, 700); // Adjust size as needed
//
//        // Create a main panel with vertical BoxLayout for text and images
//        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//
//        // Create a JTextArea to display textual data
//        JTextArea textArea = new JTextArea();
//        textArea.setEditable(false);
//        textArea.append(pokemonData);
//        mainPanel.add(textArea);
//
//        // Add a separator between text data and images
//        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
//
//        // Add images from URLs to the main panel
//        for (String urlString : imagePaths) {
//            try {
//                URL url = new URL(urlString);
//                ImageIcon imageIcon = new ImageIcon(url);
//                JLabel imageLabel = new JLabel(imageIcon);
//                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//                mainPanel.add(imageLabel);
//                mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between images
//            } catch (Exception e) {
//                System.err.println("Error loading image from URL: " + urlString);
//                e.printStackTrace();
//            }
//        }
//
//        // Wrap the main panel in a JScrollPane for scrolling
//        JScrollPane scrollPane = new JScrollPane(mainPanel);
//
//        // Add the scroll pane to the frame's content pane
//        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
//
//        // Make the frame visible
//        frame.setVisible(true);
//    }
//
//    public static void main(String[] args) throws Exception {
//        String pokemonSearched = "kyogre";
//        Main main = new Main();
//
//        main.loadAllPokemon();
//    }
//}
