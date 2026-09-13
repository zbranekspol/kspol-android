from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()
cart = root / "app/src/main/res/drawable/ic_inquiry_cart.xml"

assert "import android.webkit.WebView" not in main
assert "versionCode 812" in gradle
assert "versionName '8.0-step12-system-back-gesture'" in gradle
assert "STEP 12" in manifest
assert 'android:enableOnBackInvokedCallback="true"' in manifest
assert "tests/verify_step12.py" in workflow

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "BackGestureApi33.register(this)" in on_create
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create

assert "private boolean runBackAction()" in main
assert "private void handleSystemBackGesture()" in main
assert "android.window.OnBackInvokedCallback" in main
assert "registerOnBackInvokedCallback" in main
assert "unregisterOnBackInvokedCallback" in main
assert "moveTaskToBack(true)" in main

assert "public boolean dispatchTouchEvent(MotionEvent event)" in main
assert "deltaX < -dp(72)" in main
assert "R.drawable.ic_inquiry_cart" in main
assert cart.is_file()

assert 'tag.equals("shopitem")' in main
assert "private boolean isInStock(String availability)" in main
assert "CATALOG_FEED_URL_KEY" in main
assert "showCatalogFeedSettings()" in main

header = re.search(
    r"private View createHeader\(String title, String breadcrumbPath\).*?\n    \}",
    main,
    re.S,
).group(0)
assert header.index("block.addView(header") < header.index("block.addView(breadcrumb")
assert "breadcrumb.setOnClickListener(v -> goBack())" in header

assert "Dotaz odeslán. Číslo dotazu:" in main
assert "ZOBRAZIT V E-SHOPU ↗" in main
print("STEP 12 system back gesture checks: OK")
