package course.examples.Services.KeyCommon;

   interface KeyGenerator {
       boolean checkEmpty();
       List<String> initList();
       List<String> getArtists();
       List<String> getTitles();
       List<String> retrieveForAll();
       List<String> retrieveForOne(in int i, in List<String> allTracks);
       String[] getKey();
   }