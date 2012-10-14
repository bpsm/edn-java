package us.bpsm.edn.performance;


public class IndividualBenchmarks extends ABenchmark {

    public void time_open_and_close(int reps) {
        parse(reps, "large-keyword-map.edn", false);
    }

    public void time_large_keyword_map(int reps) {
        parse(reps, "large-keyword-map.edn");
    }

    public void time_large_symbol_map(int reps) {
        parse(reps, "large-symbol-map.edn");
    }

    public void time_list_of_nil(int reps) {
        parse(reps, "list-of-nil.edn");
    }

    public void time_map_of_maps(int reps) {
        parse(reps, "map-of-maps.edn");
    }

    public void time_map_tree(int reps) {
        parse(reps, "map-tree.edn");
    }

    public void time_mixed_vector(int reps) {
        parse(reps, "mixed-vector.edn");
    }

    public void time_set_of_keywords(int reps) {
        parse(reps, "set-of-keywords.edn");
    }

    public void time_set_of_longs(int reps) {
        parse(reps, "set-of-longs.edn");
    }

    public void time_set_of_symbols(int reps) {
        parse(reps, "set-of-symbols.edn");
    }

    public void time_vecor_of_maps(int reps) {
        parse(reps, "vecor-of-maps.edn");
    }

    public void time_vector_of_bigdecs(int reps) {
        parse(reps, "vector-of-bigdecs.edn");
    }

    public void time_vector_of_bigints(int reps) {
        parse(reps, "vector-of-bigints.edn");
    }

    public void time_vector_of_booleans(int reps) {
        parse(reps, "vector-of-booleans.edn");
    }

    public void time_vector_of_chars(int reps) {
        parse(reps, "vector-of-chars.edn");
    }


    public void time_vector_of_doubles(int reps) {
        parse(reps, "vector-of-doubles.edn");
    }

    public void time_vector_of_instants(int reps) {
        parse(reps, "vector-of-instants.edn");
    }

    public void time_vector_of_ints(int reps) {
        parse(reps, "vector-of-ints.edn");
    }

    public void time_vector_of_keywords(int reps) {
        parse(reps, "vector-of-keywords.edn");
    }

    public void time_vector_of_longs(int reps) {
        parse(reps, "vector-of-longs.edn");
    }

    public void time_vector_of_nil(int reps) {
        parse(reps, "vector-of-nil.edn");
    }

    public void time_vector_of_strings(int reps) {
        parse(reps, "vector-of-strings.edn");
    }

    public void time_vector_of_symbols(int reps) {
        parse(reps, "vector-of-symbols.edn");
    }

    public void time_vector_of_uuid(int reps) {
        parse(reps, "vector-of-uuid.edn");
    }

    public void time_vector_of_vectors(int reps) {
        parse(reps, "vector-of-vectors.edn");
    }

    public void time_vector_tree(int reps) {
        parse(reps, "vector-tree.edn");
    }

    public static void main(String[] args) {
        new IndividualBenchmarks().run();
    }

}
