package bgu.spl.net.srv.msg;

import java.util.Vector;

public class Filter {
    private final Vector<String> bigBoyWords = new Vector<>();

    public Filter(){
        bigBoyWords.add("Trump");
        bigBoyWords.add("war");
    }
    public boolean shouldBeFiltered(String word){
        return bigBoyWords.contains(word);
    }

}


