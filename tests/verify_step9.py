from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()

assert "import android.webkit.WebView" not in main
assert 'android.permission.INTERNET' in manifest
assert "versionCode 809" in gradle
assert "versionName '8.0-step9-header-title'" in gradle
assert "STEP 9" in manifest
assert "tests/verify_step9.py" in workflow

home = re.search(
    r"private void showHomeScreen\(\) \{(.*?)\n    \}", main, re.S
).group(1)
assert 'createHeader("Ověření dostupnosti produktů na prodejně")' in home
assert 'label("Ověření dostupnosti' not in home
assert 'createHeader("+K SPOL. S R.O.")' not in home

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create
assert "HttpURLConnection" not in on_create

assert "R.drawable.kspol_shop_logo" in main
assert "R.drawable.kspol_facebook_header_bg" in main
assert "public void onBackPressed()" in main
assert "backAction = parentAction;" in main
assert "Dotaz odeslán. Číslo dotazu:" in main
assert "ZOBRAZIT V E-SHOPU ↗" in main
assert "PRO ZAMĚSTNANCE" in main
print("STEP 9 header-title checks: OK")
