from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()
workflow = (root / ".github/workflows/android.yml").read_text()

assert "versionCode 815" in gradle
assert "versionName '8.0-step15-inquiry-confirmation'" in gradle
assert "STEP 15" in manifest
assert "tests/verify_step15.py" in workflow

home = re.search(r"private void showHomeScreen\(\).*?\n    \}", main, re.S).group(0)
assert "searchSortControl.setVisibility(View.GONE)" in home
assert "value.toString().trim().isEmpty()" in home
assert "Všechny kategorie e-shopu" in home

assert 'INQUIRY_STATUS_IN_STOCK = "in_stock"' in main
assert 'INQUIRY_STATUS_RESERVATION_REQUESTED = "reservation_requested"' in main
assert 'INQUIRY_STATUS_RESERVED = "reserved"' in main
assert "ZBOŽÍ JE NA PRODEJNĚ SKLADEM" in main
assert "REZERVOVAT" in main
assert "BEZ REZERVACE" in main
assert "POTVRDIT REZERVACI" in main
assert "ZAREZERVOVÁNO DO KONCE PRACOVNÍ DOBY" in main
assert "showMyQuestions(Runnable parentAction, boolean adminMode)" in main
assert "registerOnBackInvokedCallback" in main
assert "R.drawable.ic_inquiry_cart" in main
print("STEP 15 inquiry availability and reservation confirmation checks: OK")
