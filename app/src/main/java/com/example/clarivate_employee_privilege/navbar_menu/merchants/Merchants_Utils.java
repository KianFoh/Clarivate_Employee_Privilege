package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Merchants_Utils {

    /**
     * Initializes category names from a JsonArray.
     * @param categoriesJson The JsonArray containing category data.
     * @return A list of category names.
     */
    public static List<String> initializeCategoryNames(JsonArray categoriesJson) {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All"); // Add "All" category

        if (categoriesJson != null) {
            for (JsonElement categoryElement : categoriesJson) {
                if (categoryElement.isJsonObject()) {
                    JsonObject category = categoryElement.getAsJsonObject();
                    categoryNames.add(category.get("Name").getAsString());
                }
            }
        }
        return categoryNames;
    }

    /**
     * Converts a JsonArray to a list of JsonObjects.
     * @param jsonArray The JsonArray to convert.
     * @return A list of JsonObjects.
     */
    public static List<JsonObject> convertJsonArrayToList(JsonArray jsonArray) {
        List<JsonObject> list = new ArrayList<>();
        if (jsonArray != null) {
            for (JsonElement element : jsonArray) {
                if (element.isJsonObject()) {
                    list.add(element.getAsJsonObject());
                }
            }
        }
        return list;
    }

    /**
     * Gets merchant names by selected categories.
     * @param allMerchants The JsonArray containing all merchants.
     * @param selectedCategories The list of selected categories.
     * @return A list of merchant names.
     */
    public static List<String> getMerchantNamesBySelectedCategories(JsonArray allMerchants, List<String> selectedCategories) {
        List<String> merchantNames = new ArrayList<>();
        if (allMerchants != null) {
            for (JsonElement merchantElement : allMerchants) {
                if (merchantElement.isJsonObject()) {
                    JsonObject merchant = merchantElement.getAsJsonObject();
                    String merchantCategory = merchant.get("Category").getAsString();
                    if (selectedCategories.contains("All") || selectedCategories.contains(merchantCategory)) {
                        merchantNames.add(merchant.get("Name").getAsString());
                    }
                }
            }
        }
        return merchantNames;
    }

    /**
     * Filters merchants by name and selected categories.
     * @param allMerchants The JsonArray containing all merchants.
     * @param name The name to filter by.
     * @param selectedCategories The list of selected categories.
     * @return A JsonArray of filtered merchants.
     */
    public static JsonArray filterMerchantsByName(JsonArray allMerchants, String name, List<String> selectedCategories) {
        JsonArray filteredMerchants = new JsonArray();

        if (allMerchants == null) {
            return filteredMerchants; // Return an empty JsonArray if allMerchants is null
        }
        for (JsonElement merchantElement : allMerchants) {
            if (merchantElement.isJsonObject()) {
                JsonObject merchant = merchantElement.getAsJsonObject();
                String merchantName = merchant.get("Name").getAsString().toLowerCase();
                String merchantCategory = merchant.get("Category").getAsString();
                if (merchantName.contains(name.toLowerCase()) &&
                        (selectedCategories.contains("All") || selectedCategories.contains(merchantCategory))) {
                    filteredMerchants.add(merchant);
                }
            }
        }
        return filteredMerchants;
    }
}