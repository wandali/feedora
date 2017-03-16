package com.sample.foo.simplerssreader;

import java.util.Comparator;
import java.util.Random;

/**
 * Date: 08/03/2017
 * Jack: Created a class for the sorting algorithms used in the application
 *
 * Incoming #3007
 * Incoming #3008
 * Incoming #3009
 * Jack: Returns values used for sorting the articles.
 */

public class Sorting implements Comparator<RssFeedModel> {
    private String sortType;

    Random rand = new Random();

    public Sorting(String sortType) {
        this.sortType = sortType;
    }


    @Override
    public int compare(RssFeedModel article1, RssFeedModel article2) {
        if (sortType.equals("dateOldest")) {
            return article1.date.compareTo(article2.date);
        } else if (sortType.equals("title")) {
            return article1.title.compareTo(article2.title);
        } else if (sortType.equals("author")) {
            return article1.author.compareTo(article2.author);
        } else if (sortType.equals("random")) {
            int r = rand.nextInt(100);
            if (r < 50) {
                return 1;
            } else {
                return -1;
            }
        }
        return 1;
    }

}
