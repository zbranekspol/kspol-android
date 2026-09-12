package cz.kspol.stableapp.resetv8;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * STABLE RESET v8 / krok 2.
 * Vše je lokální a deterministické: žádná síť, WebView, databáze ani soubory při startu.
 */
public final class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(18, 61, 47);
    private static final int BLACK = Color.rgb(11, 13, 12);
    private static final int PAPER = Color.rgb(243, 245, 244);
    private static final int BORDER = Color.rgb(220, 225, 222);
    private static final int RED = Color.rgb(155, 45, 45);

    private final List<Product> selectedProducts = new ArrayList<>();
    private LinearLayout screenHost;

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
        screenHost = createRoot();
        screenHost.addView(createHeader("+K SPOL. S R.O."));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = verticalContainer();
        content.addView(label("Katalog", 28, BLACK, true));
        content.addView(label("STABLE RESET v8 • TEST KROK 2", 14, Color.DKGRAY, false));

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
                "Krok 2 přidává produktové obrázky a vícepoložkovou poptávku. " +
                        "Výběr je pouze v paměti telefonu a po zavření aplikace se neukládá.",
                13, Color.DKGRAY, false);
        addTopMargin(content, note, 16);

        scroll.addView(content);
        screenHost.addView(scroll, matchRemaining());
        setContentView(screenHost);
        screenHost.requestApplyInsets();
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
                content.addView(productCard(product));
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

            Button send = actionButton("POPTAT VYBRANÉ POLOŽKY", GREEN);
            send.setOnClickListener(v -> Toast.makeText(
                    this,
                    "Krok 2: výběr funguje. Odeslání bude přidáno v dalším stabilním kroku.",
                    Toast.LENGTH_LONG).show());
            addTopMargin(content, send, 18);
        }

        scroll.addView(content);
        root.addView(scroll, matchRemaining());
        setContentView(root);
        root.requestApplyInsets();
    }

    private LinearLayout productCard(Product product) {
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
        Button add = actionButton(selected ? "V POPTÁVCE" : "PŘIDAT DO POPTÁVKY", selected ? Color.GRAY : GREEN);
        add.setEnabled(!selected);
        add.setOnClickListener(v -> {
            if (!selectedProducts.contains(product)) {
                selectedProducts.add(product);
                showProducts(null);
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

    private void addTopMargin(LinearLayout parent, View child, int topDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(topDp), 0, 0);
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
}
