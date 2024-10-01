package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MerchantsUtils {

    public static List<String> initializeCategoryNames(JsonArray categoriesJson) {
        List<String> categoryNames = new ArrayList<>();
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

    public static JsonArray filterMerchantsByCategories(JsonArray allMerchants, List<String> selectedCategories) {
        JsonArray filteredMerchants = new JsonArray();
        if (selectedCategories.contains("All") || selectedCategories.isEmpty()) {
            return allMerchants;
        } else {
            for (JsonElement merchantElement : allMerchants) {
                if (merchantElement.isJsonObject()) {
                    JsonObject merchant = merchantElement.getAsJsonObject();
                    if (selectedCategories.contains(merchant.get("Category").getAsString())) {
                        filteredMerchants.add(merchant);
                    }
                }
            }
        }
        return filteredMerchants;
    }

    public static JsonArray filterMerchantsByName(JsonArray allMerchants, String name, List<String> selectedCategories) {
        JsonArray filteredMerchants = new JsonArray();
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