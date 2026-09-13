from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()
cart = root / "app/src/main/res/drawable/ic_inquiry_cart.xml"

assert "import android.webkit.WebView" not in main
assert "versionCode 811" in gradle
assert "versionName '8.0-step11-cart-swipe-stock-feed'" in gradle
assert "STEP 11" in manifest
assert "tests/verify_step11.py" in workflow

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create

assert 'tag.equals("shopitem")' in main
assert 'tag.equals("productname")' in main
assert 'tag.equals("categorytext")' in main
assert 'tag.equals("delivery_date")' in main
assert "private boolean isInStock(String availability)" in main
assert "!cleanName.isEmpty() && isInStock(availability)" in main
assert 'normalized.equals("in stock")' in main
assert 'normalized.startsWith("delivery_date:")' in main

assert "CATALOG_FEED_URL_KEY" in main
assert "showCatalogFeedSettings()" in main
assert 'secondaryButton("NASTAVENÍ FEEDU KATALOGU")' in main
assert '"www.zbrane-kspol.cz".equalsIgnoreCase(uri.getHost())' in main

assert "public boolean dispatchTouchEvent(MotionEvent event)" in main
assert "deltaX < -dp(72)" in main
assert "goBack();" in main
assert "R.drawable.ic_inquiry_cart" in main
assert 'cart.setOnClickListener' in main
assert "showInquiry();" in main
assert cart.is_file()

header = re.search(
    r"private View createHeader\(String title, String breadcrumbPath\).*?\n    \}",
    main,
    re.S,
).group(0)
assert "block.addView(header" in header
assert "block.addView(breadcrumb" in header
assert header.index("block.addView(header") < header.index("block.addView(breadcrumb")
assert "breadcrumb.setOnClickListener(v -> goBack())" in header
assert 'breadcrumbPath + " › " + subcategory' in main
assert '"Domů › " + initialCategory' in main

assert "Dotaz odeslán. Číslo dotazu:" in main
assert "ZOBRAZIT V E-SHOPU ↗" in main
print("STEP 11 cart, swipe, breadcrumb and strict-stock feed checks: OK")
