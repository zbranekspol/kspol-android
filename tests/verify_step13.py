from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()

assert "versionCode 813" in gradle
assert "versionName '8.0-step13-product-sorting'" in gradle
assert "STEP 13" in manifest
assert "tests/verify_step13.py" in workflow

for label in (
    "Cena: od nejnižší",
    "Cena: od nejvyšší",
    "Název: A–Z",
    "Název: Z–A",
):
    assert label in main

assert "private enum SortMode" in main
assert "PRICE_ASC" in main and "PRICE_DESC" in main
assert "NAME_ASC" in main and "NAME_DESC" in main
assert "private View createSortControl(Runnable onChanged)" in main
assert "private List<Product> sortedProducts(List<Product> source)" in main
assert "compareProductPrices" in main
assert "Collator.getInstance(new Locale(\"cs\", \"CZ\"))" in main
assert "parsePriceValue(price)" in main
assert "Double.isNaN(left.numericPrice)" in main

search = re.search(
    r"private void renderSearchResults\(String rawQuery\).*?\n    \}", main, re.S
).group(0)
assert "matches = sortedProducts(matches);" in search

category = re.search(
    r"private void showShopCategory\(String title, List<Product> categoryProducts.*?\n    \}",
    main,
    re.S,
).group(0)
assert "createSortControl" in category
assert "sortedProducts(categoryProducts)" in category
assert "sortedProducts(matches)" in category

assert 'android:enableOnBackInvokedCallback="true"' in manifest
assert "registerOnBackInvokedCallback" in main
assert "R.drawable.ic_inquiry_cart" in main
assert "Dotaz odeslán. Číslo dotazu:" in main
print("STEP 13 product sorting and retained stability checks: OK")
