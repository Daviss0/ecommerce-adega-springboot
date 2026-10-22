package com.adega.adega.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

import java.util.List;

public final class ProductCategories {

    private ProductCategories() {
    }

    public static final List<String> ALL = List.of(
      "Cervejas",
      "Whiskys",
      "Vodkas",
      "Gins",
      "Vinhos",
      "Destilados",
      "Energéticos",
      "Refrigerantes",
      "Bebidas sem álcool",
      "Drinks prontos",
      "Gelos",
      "Acessórios para narguilé"
    );

    public static boolean isValid(String category) {
        return category != null && ALL.contains(category);
    }

    public static String toSlug(String category) {

        String normalized = Normalizer.normalize(category, Normalizer.Form.NFD);

        return normalized.replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    public static Optional<String> fromSlug(String slug) {

        if (slug == null) {
            return Optional.empty();
        }

        return ALL.stream()
                .filter(category -> toSlug(category).equals(slug))
                .findFirst();
    }
}
