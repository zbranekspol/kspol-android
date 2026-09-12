package cz.kspol.stableapp.resetv8;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * STABLE RESET v8 / krok 4.
 * Start je stále bez sítě, WebView a čtení lokálních dat. Uložené dotazy se
 * načtou až po otevření obrazovky MOJE DOTAZY.
 */
public final class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(18, 61, 47);
    private static final int BLACK = Color.rgb(11, 13, 12);
    private static final int PAPER = Color.rgb(243, 245, 244);
    private static final int BORDER = Color.rgb(220, 225, 222);
    private static final int RED = Color.rgb(155, 45, 45);
    private static final String PREFS = "kspol_step3_local_inquiries";
    private static final String INQUIRY_COUNT = "inquiry_count";

    private final List<Product> selectedProducts = new ArrayList<>();
    private EditText searchInput;
    private TextView searchSummary;
    private LinearLayout searchResults;

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
        content.addView(label("STABLE RESET v8 • TEST KROK 4", 14, Color.DKGRAY, false));

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

        Button myQuestions = actionButton("MOJE DOTAZY", GREEN);
        myQuestions.setOnClickListener(v -> showMyQuestions());
        addTopMargin(content, myQuestions, 12);

        String[] categories = {
                "KRÁTKÉ ZBRANĚ", "DLOUHÉ ZBRANĚ", "STŘELIVO",
                "OPTIKA", "PŘÍSLUŠENSTVÍ", "KOMISNÍ PRODEJ"
        };
        for (String category : categories) {
            TextView card = categoryCard(category, false);
            card.setOnClickListener(v -> showProducts(category));
            content.addView(card);
        }
        TextView all = categoryCard("ZOBRAZIT VŠE", true);
        all.setOnClickListener(v -> showProducts(null));
        content.addView(all);

        if (!selectedProducts.isEmpty()) {
            Button inquiry = actionButton("MOJE POPTÁVKA (" + selectedProducts.size() + ")", GREEN);
            inquiry.setOnClickListener(v -> showInquiry());
            addTopMargin(content, inquiry, 16);
        }
        TextView note = label(
                "Krok 4 přidává okamžité lokální vyhledávání. Dotazy a rezervační čísla "
                        + "zůstávají uložené pouze v telefonu. "
                        + "Aplikace při startu nepoužívá internet.",
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
        int matchCount = 0;
        for (Product product : products) {
            String searchable = (product.name + " " + product.subtitle)
                    .toLowerCase(Locale.getDefault());
            if (searchable.contains(query)) {
                searchResults.addView(productCard(product, true));
                matchCount++;
            }
        }
        searchSummary.setText(matchCount == 0
                ? "Žádný produkt nenalezen"
                : "Nalezené produkty: " + matchCount);
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
                String reservationNumber = saveLocalInquiry();
                selectedProducts.clear();
                Toast.makeText(this, "Dotaz uložen. Rezervační číslo: " + reservationNumber,
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
        String reservationNumber = datePrefix + "-" + String.format(Locale.ROOT, "%03d", sequence);
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
                .putString("inquiry_" + index + "_number", reservationNumber)
                .putString("inquiry_" + index + "_created", createdAt)
                .putString("inquiry_" + index + "_products", productNames.toString())
                .putInt(INQUIRY_COUNT, index + 1)
                .commit();
        return reservationNumber;
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
        card.addView(label("Rezervační číslo " + inquiry.number, 18, GREEN, true));
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
        card.addView(image, new LinearLayout.LayoutParams(dp(96), dp(96)));
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(14), 0, 0, 0);
        text.addView(label(product.name, 17, BLACK, true));
        text.addView(label(product.subtitle, 14, Color.DKGRAY, false));
        text.addView(label(product.category, 11, GREEN, true));
        boolean selected = selectedProducts.contains(product);
        Button add = actionButton(selected ? "V POPTÁVCE" : "PŘIDAT DO POPTÁVKY",
                selected ? Color.GRAY : GREEN);
        add.setEnabled(!selected);
        add.setOnClickListener(v -> {
            if (!selectedProducts.contains(product)) {
                selectedProducts.add(product);
                if (searchMode && searchInput != null) {
                    renderSearchResults(searchInput.getText().toString());
                } else {
                    showProducts(null);
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

    private static final class Product {
        final String name;
        final String subtitle;
        final String category;
        final int imageRes;

        Product(String name, String subtitle, String category, int imageRes) {
            this.name = name;
            this.subtitle = subtitle;
            this.category = category;
            this.imageRes = imageRes;
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
