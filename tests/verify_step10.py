from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()

assert "import android.webkit.WebView" not in main
assert 'android.permission.INTERNET' in manifest
assert "versionCode 810" in gradle
assert "versionName '8.0-step10-breadcrumb-category-search'" in gradle
assert "STEP 10" in manifest
assert "tests/verify_step10.py" in workflow

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create
assert "HttpURLConnection" not in on_create

assert 'createHeader("Ověření dostupnosti produktů na prodejně")' in main
assert "private View createHeader(String title, String breadcrumbPath)" in main
assert '"Domů › " + initialCategory' in main
assert 'breadcrumbPath + " › " + subcategory' in main
assert "breadcrumb.setTextSize" not in main
assert "label(breadcrumbPath, 11" in main
assert "breadcrumb.setOnClickListener(v -> goBack())" in main

category = re.search(
    r"private void showShopCategory\(.*?\n    \}\n\n    private void appendProductBatch",
    main,
    re.S,
).group(0)
assert 'categorySearch.setHint("Hledat v této kategorii podle názvu nebo popisu…")' in category
assert "categorySearch.addTextChangedListener" in category
assert '(product.name + " " + product.description)' in category
assert "defaultSection.setVisibility(View.GONE)" in category
assert "appendProductBatch(filteredSection, matches, 0)" in category
assert 'secondaryButton("← O ÚROVEŇ VÝŠ")' in category

assert "public void onBackPressed()" in main
assert "Dotaz odeslán. Číslo dotazu:" in main
assert "ZOBRAZIT V E-SHOPU ↗" in main
assert "PRO ZAMĚSTNANCE" in main
print("STEP 10 breadcrumb and category-search checks: OK")
