package com.reky0.mydex.pokemon;

import android.util.Log;

import com.google.gson.annotations.SerializedName; // tag to indicate which JSON key is represented by the variable

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Storage class for every Pokémon
 */
public class Pokemon {
    private int id; // pokedex number
    private Cries cries;
    private final String name;
    private float height;
    private float weight;
    private boolean is_legendary; // flag for legendary being
    private boolean is_mythical; // flag for mythical being
    private Species species;
    private final ArrayList<TypeSlot> types; // types collection
    private Sprites sprites; // sprites del pokemon
    private final ArrayList<MoveSlot> moves; // moves collection
    private final ArrayList<StatSlot> stats; // collection with every stat and its values
    private final ArrayList<String> evolutionChain; // collection with the Pokémon evolutions
    private final ArrayList<VarietySlot> varieties;

    /**
     * Default pokemon, mostly for errors
     */
    public Pokemon() {
        this.name = "ERROR";
        this.types = new ArrayList<>();
        this.moves = new ArrayList<>();
        this.stats = new ArrayList<>();
        this.evolutionChain = new ArrayList<>();
        this.varieties = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public Cries getCries() {
        return cries;
    }

    public String getName() {
        return name;
    }

    public float getHeight() {
        Log.d("TEST", String.valueOf(height));
        return height/10;
    }

    public float getWeight() {
        return weight/10;
    }

    public void setLegendary(boolean is_legendary) {
        this.is_legendary = is_legendary;
    }

    public boolean isLegendary() {
        return is_legendary;
    }

    public void setMythical(boolean is_mythical) {
        this.is_mythical = is_mythical;
    }

    public boolean isMythical() {
        return is_mythical;
    }

    public Species getSpecies() {
        return species;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public ArrayList<TypeSlot> getTypes() {
        return types;
    }

    public ArrayList<MoveSlot> getMoves() {
        return moves;
    }

    public ArrayList<StatSlot> getStats() {
        return stats;
    }

    public void addEvolution(String evolutionURL) {
        this.evolutionChain.add(evolutionURL);
    }

    public ArrayList<String> getEvolutionChain() {
        return evolutionChain;
    }

    public void addVarietySlot(VarietySlot vs) {
        this.varieties.add(vs);
    }

    public ArrayList<VarietySlot> getVarieties() {
        return varieties;
    }

    public static class Cries {
        private String latest;
        private String legacy;

        public String getLatest() {
            return latest;
        }

        public String getLegacy() {
            return legacy;
        }

        @Override
        public String toString() {
            return "Cries{" +
                    "latest='" + latest + '\'' +
                    ", legacy='" + legacy + '\'' +
                    '}';
        }
    }

    public static class Species {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return "Species{" +
                    name +
                    ", " + url +
                    '}';
        }
    }

    public static class TypeSlot {
        private int slot;
        private Type type;

        public int getSlot() {
            return slot;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return "TypeSlot{" +
                    slot +
                    "," + type +
                    '}';
        }

        public static class Type {
            private String name;
            private String url;

            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            @Override
            public String toString() {
                return "Type{" +
                        name +
                        ", " + url +
                        '}';
            }
        }
    }

    public static class Sprites {
        private String back_default;
        private String back_female;
        private String back_shiny;
        private String back_shiny_female;
        private String front_default;
        private String front_female;
        private String front_shiny;
        private String front_shiny_female;

        public String getFrontDefault() {
            return front_default;
        }

        public ArrayList<String> getAllSprites() {
            ArrayList<String> allSprites = new ArrayList<>();
            allSprites.add(front_default);
            allSprites.add(back_default);
            allSprites.add(front_shiny);
            allSprites.add(back_shiny);
            allSprites.add(front_female);
            allSprites.add(back_female);
            allSprites.add(front_shiny_female);
            allSprites.add(back_shiny_female);

            return allSprites;
        }

        @Override
        public String toString() {
            return "Sprites{" +
                    "back_default='" + back_default + '\'' +
                    ", back_female='" + back_female + '\'' +
                    ", back_shiny='" + back_shiny + '\'' +
                    ", back_shiny_female='" + back_shiny_female + '\'' +
                    ", front_default='" + front_default + '\'' +
                    ", front_female='" + front_female + '\'' +
                    ", front_shiny='" + front_shiny + '\'' +
                    ", front_shiny_female='" + front_shiny_female + '\'' +
                    '}';
        }
    }

    public static class MoveSlot {
        private Move move;

        public Move getMove() {
            return move;
        }

        @Override
        public String toString() {
            return move.toString();
        }

        public static class Move {
            private String name;
            private String url;

            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            @Override
            public String toString() {
                return "{" +
                        name +
                        ", " + url +
                        '}';
            }
        }
    }

    public static class StatSlot {
        private int base_stat; // base value for the stat
        private int effort; // stat points that this Pokémon gives for that specific stat when defeated
        private Stat stat; // which stat it is (hp/atk/sp.atk/...)

        public int getBaseStat() {
            return base_stat;
        }

        public int getEffort() {
            return effort;
        }

        public Stat getStat() {
            return stat;
        }

        @Override
        public String toString() {
            return "StatSlot{" +
                    "base_stat=" + base_stat +
                    ", effort=" + effort +
                    ", stat=" + stat +
                    '}';
        }

        public static class Stat {
            private String name;
            private String url;

            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            @Override
            public String toString() {
                return "Stat{" +
                        "statName='" + name + '\'' +
                        ", statUrl='" + url + '\'' +
                        '}';
            }
        }
    }

    public static class VarietySlot {
        private boolean is_default;
        @SerializedName("pokemon")
        private Variety variety;

        public boolean isDefault() {
            return is_default;
        }

        public Variety getVariety() {
            return variety;
        }

        @Override
        public String toString() {
            return "VarietySlot{" +
                    is_default +
                    ", " + variety +
                    '}';
        }

        public static class Variety {
            private String name;
            private String url;

            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            @Override
            public String toString() {
                return "Variety{" +
                        name +
                        ", " + url +
                        '}';
            }
        }
    }
}