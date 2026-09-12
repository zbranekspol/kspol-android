from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()

assert "import android.webkit.WebView" not in main
assert 'android.permission.INTERNET' in manifest
assert "versionCode 805" in gradle
assert "versionName '8.0-step5-shop5-catalog'" in gradle

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}",
    main,
    re.S,
).group(1)
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create
assert "loadCatalogData" not in on_create
assert "HttpURLConnection" not in on_create

assert 'all.setOnClickListener(v -> showShopCatalog())' in main
assert "CATALOG_FEED_URL" in main
assert "xml-feedy-cache" in main
assert "HttpURLConnection" in main
assert "XmlPullParser" in main
assert "isCatalogCacheCurrent" in main
assert "Calendar.HOUR_OF_DAY, 6" in main
assert "replaceCacheAtomically" in main
assert "Používám poslední funkční kopii katalogu" in main
assert "NAČÍST DALŠÍ" in main
assert "product.description" in main
assert "product.name" in main
assert "product.category" not in re.search(
    r"private void renderSearchResults\(String rawQuery\) \{(.*?)\n    \}",
    main,
    re.S,
).group(1)

for category in [
    "AKCE",
    "Bazar, komisní prodej",
    "Zbraně na ZO",
    "Zbraně bez ZO",
    "Příslušenství pro zbraně",
    "Střelivo a náboje na ZO",
    "Přebíjení nábojů",
    "Zabezpečení zbraní",
    "FlobertMaster",
]:
    assert category in main

for preserved in ["MOJE DOTAZY", "ODEBRAT", "SharedPreferences", "ZOBRAZIT VŠE"]:
    assert preserved in main

print("STEP 5 Shop5 catalog static checks: OK")
