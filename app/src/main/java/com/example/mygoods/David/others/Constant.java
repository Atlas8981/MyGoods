package com.example.mygoods.David.others;

public class Constant {
    public static String itemCollection     = "items";
    public static String userCollection     = "users";
    public static String recentSearchCollection = "recentSearch";

    public static String usernameField      = "username";
    public static String subCategoryField   = "subCategory";
    public static String mainCategoryField  = "mainCategory";
    public static String viewField          = "views";
    public static String viewerField        = "viewer";
    public static String dateField          = "date";
    public static String priceField         = "price";
    public static String ownerField         = "owner";
    public static String itemNameField      = "name";

    //Intent
    public static String intentFromTrending = "Trending";
    public static String intentFromRecommendation = "Recommendation";
    public static String titleIntentFromHome      = "titleFromHomeActivity";
    public static String dataIntentFromHome = "dataFromHomeActivity";
    public static String intentFromSubCat   = "moveFromSubCat";
    public static String intentFromMainCat  = "moveFromMainCat";
    public static String mainCatTitle       = "mainCatTitle";
    public static String userIDFromHomeActivity = "userIDFromHomeActivity";

    public static String capitalize(String str) {
        if(str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
