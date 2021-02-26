package com.example.mygoods.David.others;

import com.example.mygoods.Model.Image;
import com.example.mygoods.Model.Item;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    public static void AddDummyData(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        Image[] images = new Image[]{
                new Image("https://firebasestorage.googleapis.com/v0/b/mygoods-e042f.appspot.com/o/images%2F851bf13d-1c4a-4674-8696-0080c428148f?alt=media&token=ea3c5b3d-1b46-4c61-affa-467cc6684ecd"),
                new Image("https://firebasestorage.googleapis.com/v0/b/mygoods-e042f.appspot.com/o/images%2Fd3bc38ab-dfe8-4a33-8910-ecd549605d0f?alt=media&token=06827f6c-6b2f-4b64-8c7f-a79914306306"),
                new Image("https://firebasestorage.googleapis.com/v0/b/mygoods-e042f.appspot.com/o/images%2F0b881a87-648d-4ff3-a9bf-16762f1638b5?alt=media&token=16cf5bf3-4ca2-4a2f-906f-a870a84268fe"),
                new Image("https://firebasestorage.googleapis.com/v0/b/mygoods-e042f.appspot.com/o/images%2F5058b3b6-3714-4773-a770-6bcd310e7943?alt=media&token=45c8cd8a-b9da-4e6e-b7d5-49e69bfa7998"),
                new Image("https://firebasestorage.googleapis.com/v0/b/mygoods-e042f.appspot.com/o/images%2F69724620-eab2-4597-986d-18c75570f2ce?alt=media&token=1089d025-b3df-4269-a097-b9299ce57407"),
                new Image("https://firebasestorage.googleapis.com/v0/b/mygoods-e042f.appspot.com/o/images%2Fb8ee703f-3416-47a1-9673-0cbc0477090e?alt=media&token=5d708c18-de2e-4dd2-95e2-906469b42c86"),
                new Image("https://firebasestorage.googleapis.com/v0/b/mygoods-e042f.appspot.com/o/images%2F009d39a4-7479-4488-9309-f658a1e9322a?alt=media&token=ae563fb6-c119-4122-9df3-e86919021fe5"),
        };

        Item item1 = new Item(
                "String name 1",
                "String address",
                Arrays.asList(images[0],images[1]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "bqUqbTiB0HSSTKDtNMSw2NW8Lev2",
                "0123456789",
                357.632,
                new Date());
        Item item2 = new Item(
                "String name 2",
                "String address",
                Arrays.asList(images[2],images[3]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "6fSqePdm7Sc2qfLACMJt9w6eUnB3",
                "0123456789",
                357.632,
                new Date());
        Item item3 = new Item(
                "String name 3",
                "String address",
                Arrays.asList(images[4],images[5]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "LFKYgJtu0pPY8uuUthBecM04hE62",
                "0123456789",
                357.632,
                new Date());
        Item item4 = new Item(
                "name 4",
                "String address",
                Arrays.asList(images[4],images[5]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "cdtG88mGoBP4Veh9qL6Dw2h2L0B2",
                "0123456789",
                357.632,
                new Date());
        Item item5 = new Item(
                "name 5",
                "String address",
                Arrays.asList(images[6],images[5]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "cdtG88mGoBP4Veh9qL6Dw2h2L0B2",
                "0123456789",
                357.632,
                new Date());

        Item item6 = new Item(
                "name 6",
                "String address",
                Arrays.asList(images[4],images[3]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "cdtG88mGoBP4Veh9qL6Dw2h2L0B2",
                "0123456789",
                357.632,
                new Date());
        Item item7 = new Item(
                "name 7",
                "String address",
                Arrays.asList(images[2],images[1]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "cdtG88mGoBP4Veh9qL6Dw2h2L0B2",
                "0123456789",
                357.632,
                new Date());
        Item item8 = new Item(
                "name 8",
                "String address",
                Arrays.asList(images[2],images[4]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "eU5UwoRgdfNFXp2EIYAHph8MS6O2",
                "0123456789",
                357.632,
                new Date());

        Item item9 = new Item(
                "name 9",
                "String address",
                Arrays.asList(images[1],images[3]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "cdtG88mGoBP4Veh9qL6Dw2h2L0B2",
                "0123456789",
                357.632,
                new Date());
        Item item10 = new Item(
                "name 10",
                "String address",
                Arrays.asList(images[5],images[3]),
                "Bicycle",
                "Car & Vehicle",
                "String\ndescription",
                "eU5UwoRgdfNFXp2EIYAHph8MS6O2",
                "0123456789",
                357.632,
                new Date());
        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        items.add(item6);
        items.add(item7);
        items.add(item8);
        items.add(item9);
        items.add(item10);
        for (Item i : items){
            db.collection("items").add(i);
        }
    }
}
