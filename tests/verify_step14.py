from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()

assert "versionCode 814" in gradle
assert "versionName '8.0-step14-compact-sort-row'" in gradle
assert "STEP 14" in manifest
assert "tests/verify_step14.py" in workflow

sort_control = re.search(
    r"private View createSortControl\(Runnable onChanged\).*?\n    \}", main, re.S
).group(0)
assert "control.setOrientation(LinearLayout.HORIZONTAL)" in sort_control
assert "control.setGravity(Gravity.CENTER_VERTICAL)" in sort_control
assert 'label("Seřadit produkty"' in sort_control
assert '"Cena ↑"' in sort_control
assert '"Cena ↓"' in sort_control
assert '"Název: A–Z"' in sort_control
assert '"Název: Z–A"' in sort_control
assert "spinnerParams" in sort_control

assert "private List<Product> sortedProducts(List<Product> source)" in main
assert "compareProductPrices" in main
assert "registerOnBackInvokedCallback" in main
assert "R.drawable.ic_inquiry_cart" in main
assert "Dotaz odeslán. Číslo dotazu:" in main
print("STEP 14 compact sort row and retained stability checks: OK")
