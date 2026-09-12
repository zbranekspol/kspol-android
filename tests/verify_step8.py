from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()
logo = root / "app/src/main/res/drawable/kspol_shop_logo.webp"
header = root / "app/src/main/res/drawable/kspol_facebook_header_bg.webp"

assert "import android.webkit.WebView" not in main
assert 'android.permission.INTERNET' in manifest
assert "versionCode 808" in gradle
assert "versionName '8.0-step8-branding-back-navigation'" in gradle
assert "STEP 8" in manifest
assert "tests/verify_step8.py" in workflow

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create
assert "HttpURLConnection" not in on_create

assert 'label("Ověření dostupnosti na prodejně"' in main
assert "R.drawable.kspol_shop_logo" in main
assert "R.drawable.kspol_facebook_header_bg" in main
assert "ImageView.ScaleType.CENTER_CROP" in main
assert logo.is_file() and logo.stat().st_size > 2_000
assert logo.read_bytes()[:4] == b"RIFF"
assert header.is_file() and header.stat().st_size > 100_000
header_bytes = header.read_bytes()
assert header_bytes[:4] == b"RIFF" and header_bytes[8:12] == b"WEBP"

assert "public void onBackPressed()" in main
assert "private Runnable backAction;" in main
assert "private void goBack()" in main
assert "backAction = parentAction;" in main
assert "this::showEmployeeLogin" in main
assert "this::showAdminScreen" in main
assert "() -> showShopCategory(title, categoryProducts, depth, parentAction)" in main

assert "Dotaz odeslán. Číslo dotazu:" in main
assert "ZOBRAZIT V E-SHOPU ↗" in main
assert "PRO ZAMĚSTNANCE" in main
assert "NAČÍST DALŠÍ" in main
print("STEP 8 branding and back-navigation checks: OK")
