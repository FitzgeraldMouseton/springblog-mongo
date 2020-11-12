package com.cheeseind.blogengine;

public class Tests {
    public static void main(String[] args) {

        String MAKE_POST_VISIBLE_CONDITION = "'active' : true, 'moderationStatus' : 'ACCEPTED', 'time' : {$lt : new Date()}";

        System.out.println("{" + MAKE_POST_VISIBLE_CONDITION + "}");
    }
}
