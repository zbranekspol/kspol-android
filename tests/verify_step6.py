from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()

assert "import android.webkit.WebView" not in main
assert 'android.permission.INTERNET' in manifest
assert "versionCode 806" in gradle
assert "versionName '8.0-step6-home-catalog'" in gradle

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create
assert "loadCatalogData" not in on_create
assert "HttpURLConnection" not in on_create

home = re.search(r"private void showHomeScreen\(\) \{(.*?)\n    \}", main, re.S).group(1)
assert 'searchInput.setHint("Hledat podle názvu nebo popisu…")' in home
assert 'label("Všechny kategorie e-shopu"' in home
assert "for (String category : shopCategories)" in home
assert "showShopCatalog(category)" in home
assert home.index("searchInput.setHint") < home.index("Všechny kategorie e-shopu")
assert home.index("Všechny kategorie e-shopu") < home.index("MOJE DOTAZY")
assert "String[] categories" not in home
assert 'categoryCard("ZOBRAZIT VŠE"' not in home

search = re.search(
    r"private void renderSearchResults\(String rawQuery\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "catalogProducts" in search
assert "product.name" in search
assert "product.description" in search
assert "product.category" not in search
assert "loadCatalogData" in search

assert "Rezervační číslo" not in main
assert "Číslo dotazu" in main
assert "Dotaz uložen. Číslo dotazu:" in main
assert "ČEKÁ NA VYŘÍZENÍ" in main
assert "rezervace vznikne až po potvrzení zaměstnancem" in main

for preserved in ["MOJE DOTAZY", "ODEBRAT", "SharedPreferences", "NAČÍST DALŠÍ"]:
    assert preserved in main

print("STEP 6 home catalog and question wording checks: OK")
