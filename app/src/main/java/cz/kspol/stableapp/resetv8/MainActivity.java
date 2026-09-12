package cz.kspol.stableapp.resetv8;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.LruCache;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * STABLE RESET v8 / krok 6.
 * Start je stále bez sítě, WebView a čtení lokálních dat. Uložené dotazy se
 * načtou až po otevření obrazovky MOJE DOTAZY. Kategorie Shop5 jsou na titulní
 * stránce a katalog se načítá až po výběru kategorie nebo zahájení hledání.
 */
public final class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(18, 61, 47);
    private static final int BLACK = Color.rgb(11, 13, 12);
    private static final int PAPER = Color.rgb(243, 245, 244);
    private static final int BORDER = Color.rgb(220, 225, 222);
    private static final int RED = Color.rgb(155, 45, 45);
    private static final String PREFS = "kspol_step3_local_inquiries";
    private static final String INQUIRY_COUNT = "inquiry_count";
    private static final String CATALOG_CACHE_FILE = "shop5_catalog.xml";
    private static final int CATALOG_BATCH_SIZE = 20;
    private static final String CATALOG_FEED_URL =
            "https://www.zbrane-kspol.cz/_obchody/zbrane-kspol.shop5.cz/soubory/"
                    + "xml-feedy-cache/export_google-nakupy-cz-CZK-0-10000-"
                    + "mFrQECY6NUOJ7o1Q-jpeg.xml";

    private final List<Product> selectedProducts = new ArrayList<>();
    private final List<Product> catalogProducts = new ArrayList<>();
    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(4);
    private final LruCache<String, Bitmap> imageCache = new LruCache<String, Bitmap>(16 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return Math.max(1, value.getByteCount() / 1024);
        }
    };
    private EditText searchInput;
    private TextView searchSummary;
    private LinearLayout searchResults;
    private boolean catalogSearchLoading;

    private final String[] shopCategories = new String[]{
            "AKCE", "Bazar, komisní prodej", "Zbraně na ZO", "Zbraně bez ZO",
            "Příslušenství pro zbraně", "Tlumiče", "Střelivo a náboje na ZO",
            "Střelivo, náboje bez ZO", "Péče o zbraně", "Přebíjení nábojů",
            "Montáže na optiku", "Optika", "Termovize", "Pouzdra, kufry",
            "Sebeobrana", "Chladné zbraně", "Terče, střelnice", "Trénink, sušení",
            "Lovecké doplňky", "Oblečení a obuv", "Ochranné pomůcky",
            "Zabezpečení zbraní", "Pyrotechnika", "Dárky", "FlobertMaster"
    };

    private final Product[] products = new Product[]{
            new Product("Glock 17 Gen5", "9 mm Luger", "KRÁTKÉ ZBRANĚ", R.drawable.product_pistol),
            new Product("Walther RS3 Chestnut", ".308 Win.", "DLOUHÉ ZBRANĚ", R.drawable.product_rifle),
            new Product("Sellier & Bellot", "9 mm Luger", "STŘELIVO", R.drawable.product_ammo),
            new Product("Puškohled", "lovecká optika", "OPTIKA", R.drawable.product_optic),
            new Product("Ochranná sluchátka", "střelecké příslušenství", "PŘÍSLUŠENSTVÍ", R.drawable.product_accessory),
            new Product("Komisní nabídka", "aktuální bazar", "KOMISNÍ PRODEJ", R.drawable.product_commission)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHomeScreen();
    }

    private void showHomeScreen() {
        LinearLayout root = createRoot();
        root.addView(createHeader("+K SPOL. S R.O."));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = verticalContainer();
        content.addView(label("Katalog", 28, BLACK, true));
        content.addView(label("STABLE RESET v8 • TEST KROK 6", 14, Color.DKGRAY, false));

        searchInput = new EditText(this);
        searchInput.setHint("Hledat podle názvu nebo popisu…");
        searchInput.setSingleLine(true);
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        searchInput.setTextSize(16);
        searchInput.setTextColor(BLACK);
        searchInput.setHintTextColor(Color.GRAY);
        searchInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        searchInput.setBackground(rounded(Color.WHITE, BORDER, 14));
        addTopMargin(content, searchInput, 12);

        searchSummary = label("", 14, Color.DKGRAY, true);
        searchSummary.setVisibility(View.GONE);
        addTopMargin(content, searchSummary, 8);

        searchResults = new LinearLayout(this);
        searchResults.setOrientation(LinearLayout.VERTICAL);
        content.addView(searchResults);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence value, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence value, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable value) {
                renderSearchResults(value.toString());
            }
        });

        content.addView(label("Všechny kategorie e-shopu", 22, BLACK, true));
        for (String category : shopCategories) {
            List<Product> matches = catalogProducts.isEmpty()
                    ? new ArrayList<>()
                    : productsForTopCategory(category);
            String title = matches.isEmpty() ? category : category + " (" + matches.size() + ")";
            TextView card = categoryCard(title, false);
            card.setOnClickListener(v -> showShopCatalog(category));
            content.addView(card);
        }

        Button myQuestions = actionButton("MOJE DOTAZY", GREEN);
        myQuestions.setOnClickListener(v -> showMyQuestions());
        addTopMargin(content, myQuestions, 12);

        if (!selectedProducts.isEmpty()) {
            Button inquiry = actionButton("MOJE POPTÁVKA (" + selectedProducts.size() + ")", GREEN);
            inquiry.setOnClickListener(v -> showInquiry());
            addTopMargin(content, inquiry, 16);
        }
        TextView note = label(
                "Krok 6 zobrazuje všechny kategorie Shop5 přímo na titulní stránce. "
                        + "Číslo označuje dotaz; rezervace vznikne až po potvrzení zaměstnancem, "
                        + "že je zboží skladem na prodejně. Aplikace při startu nepoužívá internet.",
                13, Color.DKGRAY, false);
        addTopMargin(content, note, 16);
        scroll.addView(content);
        root.addView(scroll, matchRemaining());
        setContentView(root);
        root.requestApplyInsets();
    }

    private void renderSearchResults(String rawQuery) {
        if (searchResults == null || searchSummary == null) {
            return;
        }
        searchResults.removeAllViews();
        String query = rawQuery.trim().toLowerCase(Locale.getDefault());
        if (query.isEmpty()) {
            searchSummary.setVisibility(View.GONE);
            return;
        }
        if (catalogProducts.isEmpty()) {
            searchSummary.setText(catalogSearchLoading
                    ? "Načítám katalog pro vyhledávání…"
                    : "Připravuji katalog pro vyhledávání…");
            searchSummary.setVisibility(View.VISIBLE);
            if (!catalogSearchLoading) {
                catalogSearchLoading = true;
                new Thread(() -> {
                    CatalogLoadResult result = loadCatalogData();
                    runOnUiThread(() -> {
                        catalogSearchLoading = false;
                        if (result.products.isEmpty()) {
                            searchSummary.setText("Katalog se nepodařilo načíst. Zkontrolujte připojení.");
                            searchSummary.setVisibility(View.VISIBLE);
                            return;
                        }
                        catalogProducts.clear();
                        catalogProducts.addAll(result.products);
                        if (searchInput != null) {
                            renderSearchResults(searchInput.getText().toString());
                        }
                    });
                }, "shop5-search-loader").start();
            }
            return;
        }
        int matchCount = 0;
        int displayed = 0;
        for (Product product : catalogProducts) {
            String searchable = (product.name + " " + product.description)
                    .toLowerCase(Locale.getDefault());
            if (searchable.contains(query)) {
                matchCount++;
                if (displayed < 30) {
                    searchResults.addView(productCard(product, true));
                    displayed++;
                }
            }
        }
        searchSummary.setText(matchCount == 0
                ? "Žádný produkt nenalezen"
                : "Nalezené produkty: " + matchCount
                        + (matchCount > displayed ? " • zobrazeno prvních " + displayed : ""));
        searchSummary.setVisibility(View.VISIBLE);
    }

    private void showProducts(String category) {
        LinearLayout root = createRoot();
        root.addView(createHeader(category == null ? "VŠECHNY PRODUKTY" : category));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = verticalContainer();
        Button back = secondaryButton("← ZPĚT NA KATALOG");
        back.setOnClickListener(v -> showHomeScreen());
        content.addView(back);
        if (!selectedProducts.isEmpty()) {
            Button inquiry = actionButton("MOJE POPTÁVKA (" + selectedProducts.size() + ")", GREEN);
            inquiry.setOnClickListener(v -> showInquiry());
            addTopMargin(content, inquiry, 10);
        }
        for (Product product : products) {
            if (category == null || category.equals(product.category)) {
                content.addView(productCard(product, false));
            }
        }
        scroll.addView(content);
        root.addView(scroll, matchRemaining());
        setContentView(root);
        root.requestApplyInsets();
    }

    private void showShopCatalog() {
        showShopCatalog(null);
    }

    private void showShopCatalog(String initialCategory) {
        LinearLayout root = createRoot();
        root.addView(createHeader("KATALOG E-SHOPU"));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = verticalContainer();
        Button back = secondaryButton("← ZPĚT NA KATALOG");
        back.setOnClickListener(v -> showHomeScreen());
        content.addView(back);
        TextView status = label("Načítám denní katalog Shop5…", 15, Color.DKGRAY, true);
        content.addView(status);
        LinearLayout categories = new LinearLayout(this);
        categories.setOrientation(LinearLayout.VERTICAL);
        content.addView(categories);
        scroll.addView(content);
        root.addView(scroll, matchRemaining());
        setContentView(root);
        root.requestApplyInsets();

        if (!catalogProducts.isEmpty() && isCatalogCacheCurrent(catalogCacheFile())) {
            if (initialCategory == null) {
                status.setText("Katalog je aktuální pro dnešní cyklus od 6:00.");
                renderShopCategories(categories);
            } else {
                showShopCategory(initialCategory, productsForTopCategory(initialCategory), 1);
            }
            return;
        }

        new Thread(() -> {
            CatalogLoadResult result = loadCatalogData();
            runOnUiThread(() -> {
                if (result.products.isEmpty()) {
                    status.setText("Katalog se nepodařilo načíst. Zkontrolujte připojení.");
                    Button retry = actionButton("ZKUSIT ZNOVU", GREEN);
                    retry.setOnClickListener(v -> showShopCatalog(initialCategory));
                    addTopMargin(categories, retry, 12);
                    return;
                }
                catalogProducts.clear();
                catalogProducts.addAll(result.products);
                if (initialCategory == null) {
                    status.setText(result.usedOlderCache
                            ? "Používám poslední funkční kopii katalogu."
                            : "Aktualizováno pro denní cyklus od 6:00 • produktů: "
                                    + catalogProducts.size());
                    renderShopCategories(categories);
                } else {
                    showShopCategory(initialCategory, productsForTopCategory(initialCategory), 1);
                }
            });
        }, "shop5-catalog-loader").start();
    }

    private void renderShopCategories(LinearLayout target) {
        target.removeAllViews();
        target.addView(label("Všechny kategorie e-shopu", 22, BLACK, true));
        for (String category : shopCategories) {
            List<Product> matches = productsForTopCategory(category);
            String title = matches.isEmpty() ? category : category + " (" + matches.size() + ")";
            TextView card = categoryCard(title, false);
            card.setOnClickListener(v -> showShopCategory(category, matches, 1));
            target.addView(card);
        }
        List<Product> uncategorized = new ArrayList<>();
        for (Product product : catalogProducts) {
            boolean matched = false;
            for (String category : shopCategories) {
                if (matchesTopCategory(product.category, category)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                uncategorized.add(product);
            }
        }
        if (!uncategorized.isEmpty()) {
            TextView other = categoryCard("Ostatní (" + uncategorized.size() + ")", false);
            other.setOnClickListener(v -> showShopCategory("Ostatní", uncategorized, 1));
            target.addView(other);
        }
    }

    private void showShopCategory(String title, List<Product> categoryProducts, int depth) {
        LinearLayout root = createRoot();
        root.addView(createHeader(title.toUpperCase(Locale.getDefault())));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = verticalContainer();
        Button back = secondaryButton("← VŠECHNY KATEGORIE");
        back.setOnClickListener(v -> showHomeScreen());
        content.addView(back);

        Set<String> subcategories = new LinkedHashSet<>();
        for (Product product : categoryProducts) {
            String[] parts = categoryParts(product.category);
            if (parts.length > depth && !parts[depth].isEmpty()) {
                subcategories.add(parts[depth]);
            }
        }
        if (!subcategories.isEmpty()) {
            content.addView(label("Podkategorie", 20, BLACK, true));
            for (String subcategory : subcategories) {
                List<Product> subset = new ArrayList<>();
                for (Product product : categoryProducts) {
                    String[] parts = categoryParts(product.category);
                    if (parts.length > depth && parts[depth].equalsIgnoreCase(subcategory)) {
                        subset.add(product);
                    }
                }
                TextView card = categoryCard(subcategory + " (" + subset.size() + ")", false);
                card.setOnClickListener(v -> showShopCategory(subcategory, subset, depth + 1));
                content.addView(card);
            }
        }

        TextView count = label("Zboží v kategorii: " + categoryProducts.size(), 20, BLACK, true);
        addTopMargin(content, count, 14);
        if (categoryProducts.isEmpty()) {
            content.addView(label("V této kategorii nyní není aktivní zboží.",
                    15, Color.DKGRAY, false));
        } else {
            appendProductBatch(content, categoryProducts, 0);
        }
        scroll.addView(content);
        root.addView(scroll, matchRemaining());
        setContentView(root);
        root.requestApplyInsets();
    }

    private void appendProductBatch(LinearLayout target, List<Product> source, int start) {
        int end = Math.min(source.size(), start + CATALOG_BATCH_SIZE);
        for (int i = start; i < end; i++) {
            target.addView(productCard(source.get(i), false));
        }
        if (end < source.size()) {
            Button more = actionButton("NAČÍST DALŠÍ (" + (source.size() - end) + ")", GREEN);
            more.setOnClickListener(v -> {
                target.removeView(more);
                appendProductBatch(target, source, end);
            });
            addTopMargin(target, more, 12);
        }
    }

    private List<Product> productsForTopCategory(String category) {
        List<Product> matches = new ArrayList<>();
        for (Product product : catalogProducts) {
            if (matchesTopCategory(product.category, category)) {
                matches.add(product);
            }
        }
        return matches;
    }

    private boolean matchesTopCategory(String path, String category) {
        String normalizedPath = normalize(path);
        String normalizedCategory = normalize(category);
        if (normalizedPath.startsWith(normalizedCategory)) {
            return true;
        }
        if (category.equals("AKCE")) {
            return normalizedPath.startsWith("akcni") || normalizedPath.startsWith("zlevnene");
        }
        if (category.equals("Bazar, komisní prodej")) {
            return normalizedPath.startsWith("bazar") || normalizedPath.startsWith("komisni");
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replace('á', 'a').replace('č', 'c').replace('ď', 'd')
                .replace('é', 'e').replace('ě', 'e').replace('í', 'i')
                .replace('ň', 'n').replace('ó', 'o').replace('ř', 'r')
                .replace('š', 's').replace('ť', 't').replace('ú', 'u')
                .replace('ů', 'u').replace('ý', 'y').replace('ž', 'z');
    }

    private String[] categoryParts(String path) {
        if (path == null || path.trim().isEmpty()) {
            return new String[]{"Ostatní"};
        }
        return path.trim().split("\\s*(?:>|/|\\|)\\s*");
    }

    private CatalogLoadResult loadCatalogData() {
        File cache = catalogCacheFile();
        if (isCatalogCacheCurrent(cache)) {
            List<Product> cached = parseCatalog(cache);
            if (!cached.isEmpty()) {
                return new CatalogLoadResult(cached, false);
            }
        }

        File temporary = new File(getFilesDir(), CATALOG_CACHE_FILE + ".tmp");
        try {
            downloadCatalog(temporary);
            List<Product> downloaded = parseCatalog(temporary);
            if (downloaded.isEmpty()) {
                throw new IllegalStateException("Prázdný XML katalog");
            }
            replaceCacheAtomically(temporary, cache);
            return new CatalogLoadResult(downloaded, false);
        } catch (Exception ignored) {
            List<Product> older = parseCatalog(cache);
            return new CatalogLoadResult(older, !older.isEmpty());
        } finally {
            if (temporary.exists()) {
                temporary.delete();
            }
        }
    }

    private void downloadCatalog(File target) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(CATALOG_FEED_URL).openConnection();
        connection.setConnectTimeout(20_000);
        connection.setReadTimeout(90_000);
        connection.setRequestProperty("User-Agent", "+K-spol-Android/8.0");
        connection.setInstanceFollowRedirects(true);
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("HTTP " + status);
            }
            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream output = new FileOutputStream(target, false)) {
                byte[] buffer = new byte[32 * 1024];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.getFD().sync();
            }
        } finally {
            connection.disconnect();
        }
    }

    private List<Product> parseCatalog(File file) {
        List<Product> parsed = new ArrayList<>();
        if (file == null || !file.isFile() || file.length() == 0) {
            return parsed;
        }
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, "UTF-8");
            boolean inItem = false;
            String name = "";
            String description = "";
            String category = "";
            String price = "";
            String availability = "";
            String imageUrl = "";
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String tag = localTag(parser.getName());
                    if (tag.equals("item")) {
                        inItem = true;
                        name = description = category = price = availability = imageUrl = "";
                    } else if (inItem && isCatalogField(tag)) {
                        String value = parser.nextText();
                        if (tag.equals("title")) name = value;
                        else if (tag.equals("description")) description = value;
                        else if (tag.equals("product_type")) category = value;
                        else if (tag.equals("price")) price = value;
                        else if (tag.equals("availability")) availability = value;
                        else if (tag.equals("image_link") && imageUrl.isEmpty()) imageUrl = value;
                    }
                } else if (event == XmlPullParser.END_TAG
                        && localTag(parser.getName()).equals("item") && inItem) {
                    inItem = false;
                    String cleanName = cleanHtml(name);
                    if (!cleanName.isEmpty()) {
                        String cleanDescription = cleanHtml(description);
                        String cleanCategory = cleanHtml(category);
                        if (cleanCategory.isEmpty()) cleanCategory = "Ostatní";
                        String subtitle = formatOffer(price, availability);
                        parsed.add(new Product(cleanName, subtitle, cleanCategory,
                                R.drawable.product_accessory, cleanDescription, imageUrl));
                    }
                }
                event = parser.next();
            }
        } catch (Exception ignored) {
            parsed.clear();
        }
        return parsed;
    }

    private String localTag(String tag) {
        int colon = tag == null ? -1 : tag.indexOf(':');
        return colon >= 0 ? tag.substring(colon + 1) : (tag == null ? "" : tag);
    }

    private boolean isCatalogField(String tag) {
        return tag.equals("title") || tag.equals("description") || tag.equals("product_type")
                || tag.equals("price") || tag.equals("availability") || tag.equals("image_link");
    }

    @SuppressWarnings("deprecation")
    private String cleanHtml(String value) {
        if (value == null) return "";
        return Html.fromHtml(value).toString().replaceAll("\\s+", " ").trim();
    }

    private String formatOffer(String price, String availability) {
        String cleanPrice = cleanHtml(price);
        String stock;
        String normalized = availability == null ? "" : availability.toLowerCase(Locale.ROOT);
        if (normalized.contains("in_stock") || normalized.contains("instock")
                || normalized.contains("in stock")) stock = "Skladem";
        else if (normalized.contains("preorder")) stock = "Na objednávku";
        else if (normalized.contains("out_of_stock") || normalized.contains("outofstock")
                || normalized.contains("out of stock")) {
            stock = "Není skladem";
        } else stock = cleanHtml(availability);
        if (cleanPrice.isEmpty()) return stock;
        if (stock.isEmpty()) return cleanPrice;
        return cleanPrice + " • " + stock;
    }

    private File catalogCacheFile() {
        return new File(getFilesDir(), CATALOG_CACHE_FILE);
    }

    private boolean isCatalogCacheCurrent(File cache) {
        if (cache == null || !cache.isFile() || cache.length() == 0) return false;
        Calendar threshold = Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"));
        threshold.set(Calendar.HOUR_OF_DAY, 6);
        threshold.set(Calendar.MINUTE, 0);
        threshold.set(Calendar.SECOND, 0);
        threshold.set(Calendar.MILLISECOND, 0);
        if (System.currentTimeMillis() < threshold.getTimeInMillis()) {
            threshold.add(Calendar.DAY_OF_MONTH, -1);
        }
        return cache.lastModified() >= threshold.getTimeInMillis();
    }

    private void replaceCacheAtomically(File temporary, File cache) throws Exception {
        File backup = new File(getFilesDir(), CATALOG_CACHE_FILE + ".bak");
        if (backup.exists()) backup.delete();
        if (cache.exists() && !cache.renameTo(backup)) {
            throw new IllegalStateException("Nelze zazálohovat cache");
        }
        if (!temporary.renameTo(cache)) {
            if (backup.exists()) backup.renameTo(cache);
            throw new IllegalStateException("Nelze uložit cache");
        }
        cache.setLastModified(System.currentTimeMillis());
        if (backup.exists()) backup.delete();
    }

    private void showInquiry() {
        LinearLayout root = createRoot();
        root.addView(createHeader("MOJE POPTÁVKA"));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = verticalContainer();
        Button back = secondaryButton("← ZPĚT NA KATALOG");
        back.setOnClickListener(v -> showHomeScreen());
        content.addView(back);
        content.addView(label("Vybrané položky: " + selectedProducts.size(), 20, BLACK, true));
        if (selectedProducts.isEmpty()) {
            TextView empty = label("Poptávka je prázdná.", 15, Color.DKGRAY, false);
            addTopMargin(content, empty, 16);
        } else {
            List<Product> snapshot = new ArrayList<>(selectedProducts);
            for (Product product : snapshot) {
                content.addView(inquiryRow(product));
            }
            Button save = actionButton("VYTVOŘIT DOTAZ", GREEN);
            save.setOnClickListener(v -> {
                String questionNumber = saveLocalInquiry();
                selectedProducts.clear();
                Toast.makeText(this, "Dotaz uložen. Číslo dotazu: " + questionNumber,
                        Toast.LENGTH_LONG).show();
                showMyQuestions();
            });
            addTopMargin(content, save, 18);
        }
        scroll.addView(content);
        root.addView(scroll, matchRemaining());
        setContentView(root);
        root.requestApplyInsets();
    }

    private void showMyQuestions() {
        LinearLayout root = createRoot();
        root.addView(createHeader("MOJE DOTAZY"));
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = verticalContainer();
        Button back = secondaryButton("← ZPĚT NA KATALOG");
        back.setOnClickListener(v -> showHomeScreen());
        content.addView(back);
        List<Inquiry> inquiries = loadLocalInquiries();
        content.addView(label("Uložené dotazy: " + inquiries.size(), 20, BLACK, true));
        if (inquiries.isEmpty()) {
            TextView empty = label("Zatím nemáte žádný uložený dotaz.", 15, Color.DKGRAY, false);
            addTopMargin(content, empty, 16);
        } else {
            for (int i = inquiries.size() - 1; i >= 0; i--) {
                content.addView(inquiryHistoryCard(inquiries.get(i)));
            }
        }
        scroll.addView(content);
        root.addView(scroll, matchRemaining());
        setContentView(root);
        root.requestApplyInsets();
    }

    private String saveLocalInquiry() {
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        Date now = new Date();
        String dayKey = new SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(now);
        String counterKey = "reservation_counter_" + dayKey;
        int sequence = preferences.getInt(counterKey, 0) + 1;
        String datePrefix = new SimpleDateFormat("dd.MM.", Locale.ROOT).format(now);
        String questionNumber = datePrefix + "-" + String.format(Locale.ROOT, "%03d", sequence);
        String createdAt = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(now);
        StringBuilder productNames = new StringBuilder();
        for (Product product : selectedProducts) {
            if (productNames.length() > 0) {
                productNames.append("\n");
            }
            productNames.append(product.name).append(" — ").append(product.subtitle);
        }
        int index = preferences.getInt(INQUIRY_COUNT, 0);
        preferences.edit()
                .putInt(counterKey, sequence)
                .putString("inquiry_" + index + "_number", questionNumber)
                .putString("inquiry_" + index + "_created", createdAt)
                .putString("inquiry_" + index + "_products", productNames.toString())
                .putInt(INQUIRY_COUNT, index + 1)
                .commit();
        return questionNumber;
    }

    private List<Inquiry> loadLocalInquiries() {
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        int count = preferences.getInt(INQUIRY_COUNT, 0);
        List<Inquiry> inquiries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String number = preferences.getString("inquiry_" + i + "_number", "");
            String created = preferences.getString("inquiry_" + i + "_created", "");
            String productNames = preferences.getString("inquiry_" + i + "_products", "");
            if (!number.isEmpty()) {
                inquiries.add(new Inquiry(number, created, productNames));
            }
        }
        return inquiries;
    }

    private LinearLayout inquiryHistoryCard(Inquiry inquiry) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(rounded(Color.WHITE, BORDER, 16));
        card.addView(label("Číslo dotazu " + inquiry.number, 18, GREEN, true));
        card.addView(label(inquiry.createdAt, 13, Color.DKGRAY, false));
        card.addView(label(inquiry.productNames, 15, BLACK, false));
        card.addView(label("ČEKÁ NA VYŘÍZENÍ", 12, GREEN, true));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(8));
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout productCard(Product product, boolean searchMode) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(rounded(Color.WHITE, BORDER, 16));
        ImageView image = new ImageView(this);
        image.setImageResource(product.imageRes);
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        image.setBackgroundColor(Color.rgb(235, 238, 236));
        if (!product.imageUrl.isEmpty()) {
            loadRemoteImage(image, product.imageUrl);
        }
        card.addView(image, new LinearLayout.LayoutParams(dp(96), dp(96)));
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(14), 0, 0, 0);
        text.addView(label(product.name, 17, BLACK, true));
        text.addView(label(product.subtitle, 14, Color.DKGRAY, false));
        if (!product.description.isEmpty() && !product.description.equals(product.subtitle)) {
            String shortDescription = product.description.length() > 180
                    ? product.description.substring(0, 177) + "…"
                    : product.description;
            TextView description = label(shortDescription, 13, Color.DKGRAY, false);
            description.setMaxLines(3);
            text.addView(description);
        }
        text.addView(label(product.category, 11, GREEN, true));
        boolean selected = isSelectedProduct(product);
        Button add = actionButton(selected ? "V POPTÁVCE" : "PŘIDAT DO POPTÁVKY",
                selected ? Color.GRAY : GREEN);
        add.setEnabled(!selected);
        add.setOnClickListener(v -> {
            if (!isSelectedProduct(product)) {
                selectedProducts.add(product);
                if (searchMode && searchInput != null) {
                    renderSearchResults(searchInput.getText().toString());
                } else {
                    add.setText("V POPTÁVCE");
                    add.setEnabled(false);
                    add.setBackground(rounded(Color.GRAY, Color.GRAY, 12));
                }
            }
        });
        addTopMargin(text, add, 8);
        card.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(8));
        card.setLayoutParams(params);
        return card;
    }

    private boolean isSelectedProduct(Product candidate) {
        for (Product selected : selectedProducts) {
            if (selected.name.equals(candidate.name) && selected.category.equals(candidate.category)) {
                return true;
            }
        }
        return false;
    }

    private void loadRemoteImage(ImageView target, String imageUrl) {
        Bitmap cached = imageCache.get(imageUrl);
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }
        imageExecutor.execute(() -> {
            Bitmap downloaded = downloadBitmap(imageUrl);
            if (downloaded == null) return;
            imageCache.put(imageUrl, downloaded);
            runOnUiThread(() -> {
                if (!isFinishing()) target.setImageBitmap(downloaded);
            });
        });
    }

    private Bitmap downloadBitmap(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(12_000);
            connection.setReadTimeout(20_000);
            connection.setRequestProperty("User-Agent", "+K-spol-Android/8.0");
            connection.setInstanceFollowRedirects(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                return null;
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (InputStream input = new BufferedInputStream(connection.getInputStream())) {
                byte[] buffer = new byte[16 * 1024];
                int read;
                int total = 0;
                while ((read = input.read(buffer)) != -1 && total < 8 * 1024 * 1024) {
                    int allowed = Math.min(read, 8 * 1024 * 1024 - total);
                    output.write(buffer, 0, allowed);
                    total += allowed;
                }
            }
            byte[] bytes = output.toByteArray();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            while (bounds.outWidth / options.inSampleSize > 480
                    || bounds.outHeight / options.inSampleSize > 480) {
                options.inSampleSize *= 2;
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private LinearLayout inquiryRow(Product product) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        row.setBackground(rounded(Color.WHITE, BORDER, 14));
        ImageView image = new ImageView(this);
        image.setImageResource(product.imageRes);
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        image.setBackgroundColor(Color.rgb(235, 238, 236));
        row.addView(image, new LinearLayout.LayoutParams(dp(72), dp(72)));
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(12), 0, dp(8), 0);
        text.addView(label(product.name, 16, BLACK, true));
        text.addView(label(product.subtitle, 13, Color.DKGRAY, false));
        row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button remove = actionButton("ODEBRAT", RED);
        remove.setOnClickListener(v -> {
            selectedProducts.remove(product);
            showInquiry();
        });
        row.addView(remove, new LinearLayout.LayoutParams(dp(104), ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(8));
        row.setLayoutParams(params);
        return row;
    }

    private LinearLayout createRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(0, insets.getSystemWindowInsetTop(), 0,
                    insets.getSystemWindowInsetBottom());
            return insets;
        });
        return root;
    }

    private LinearLayout createHeader(String title) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(10), dp(18), dp(10));
        header.setBackgroundColor(GREEN);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.kspol_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        header.addView(logo, new LinearLayout.LayoutParams(dp(64), dp(64)));
        TextView brand = label(title, 20, Color.WHITE, true);
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        brandParams.setMargins(dp(12), 0, 0, 0);
        header.addView(brand, brandParams);
        return header;
    }

    private LinearLayout verticalContainer() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(18), dp(16), dp(24));
        return content;
    }

    private TextView categoryCard(String text, boolean primary) {
        TextView card = label(text, 16, primary ? Color.WHITE : GREEN, true);
        card.setGravity(Gravity.CENTER);
        card.setMinHeight(dp(62));
        card.setBackground(rounded(primary ? GREEN : Color.WHITE, primary ? GREEN : BORDER, 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(params);
        return card;
    }

    private Button actionButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(12);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setAllCaps(false);
        button.setBackground(rounded(color, color, 12));
        button.setPadding(dp(10), dp(8), dp(10), dp(8));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(GREEN);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setAllCaps(false);
        button.setBackground(rounded(Color.WHITE, BORDER, 12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(14));
        button.setLayoutParams(params);
        return button;
    }

    private GradientDrawable rounded(int fill, int stroke, int radiusDp) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(fill);
        background.setCornerRadius(dp(radiusDp));
        background.setStroke(dp(1), stroke);
        return background;
    }

    private TextView label(String text, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(dp(4), dp(6), dp(4), dp(6));
        if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private void addTopMargin(LinearLayout parent, View child, int dpValue) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(dpValue), 0, 0);
        parent.addView(child, params);
    }

    private LinearLayout.LayoutParams matchRemaining() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        imageExecutor.shutdownNow();
        super.onDestroy();
    }

    private static final class Product {
        final String name;
        final String subtitle;
        final String category;
        final int imageRes;
        final String description;
        final String imageUrl;

        Product(String name, String subtitle, String category, int imageRes) {
            this(name, subtitle, category, imageRes, subtitle, "");
        }

        Product(String name, String subtitle, String category, int imageRes,
                String description, String imageUrl) {
            this.name = name;
            this.subtitle = subtitle;
            this.category = category;
            this.imageRes = imageRes;
            this.description = description == null ? "" : description;
            this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
        }
    }

    private static final class CatalogLoadResult {
        final List<Product> products;
        final boolean usedOlderCache;

        CatalogLoadResult(List<Product> products, boolean usedOlderCache) {
            this.products = products;
            this.usedOlderCache = usedOlderCache;
        }
    }

    private static final class Inquiry {
        final String number;
        final String createdAt;
        final String productNames;

        Inquiry(String number, String createdAt, String productNames) {
            this.number = number;
            this.createdAt = createdAt;
            this.productNames = productNames;
        }
    }
}
