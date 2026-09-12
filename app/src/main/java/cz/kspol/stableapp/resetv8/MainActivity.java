package cz.kspol.stableapp.resetv8;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Krok 1 nad potvrzeným stabilním jádrem STABLE RESET v8.
 * Start nečte soubory, databázi ani síť a nevytváří žádná připojení.
 */
public final class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(18, 61, 47);
    private static final int BLACK = Color.rgb(11, 13, 12);
    private static final int PAPER = Color.rgb(243, 245, 244);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createHomeScreen());
    }

    private LinearLayout createHomeScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(0, insets.getSystemWindowInsetTop(), 0,
                    insets.getSystemWindowInsetBottom());
            return insets;
        });

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(12), dp(18), dp(12));
        header.setBackgroundColor(GREEN);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.kspol_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        header.addView(logo, new LinearLayout.LayoutParams(dp(72), dp(72)));

        TextView brand = label("+K SPOL. S R.O.", 22, Color.WHITE, true);
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        brandParams.setMargins(dp(14), 0, 0, 0);
        header.addView(brand, brandParams);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(18), dp(16), dp(24));

        content.addView(label("Katalog", 28, BLACK, true));
        content.addView(label("STABLE RESET v8 • TEST KROK 1", 14,
                Color.DKGRAY, false));

        String[] categories = {
                "KRÁTKÉ ZBRANĚ",
                "DLOUHÉ ZBRANĚ",
                "STŘELIVO",
                "OPTIKA",
                "PŘÍSLUŠENSTVÍ",
                "KOMISNÍ PRODEJ"
        };
        for (String category : categories) {
            content.addView(categoryCard(category, false));
        }
        content.addView(categoryCard("ZOBRAZIT VŠE", true));

        TextView note = label(
                "Tento krok pouze ověřuje stabilní start a domovskou obrazovku. "
                        + "Aplikace při startu nepoužívá internet.",
                13, Color.DKGRAY, false);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.setMargins(0, dp(16), 0, 0);
        note.setLayoutParams(noteParams);
        content.addView(note);

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.requestApplyInsets();
        return root;
    }

    private TextView categoryCard(String text, boolean primary) {
        TextView card = label(text, 16, primary ? Color.WHITE : GREEN, true);
        card.setGravity(Gravity.CENTER);
        card.setMinHeight(dp(62));
        GradientDrawable background = new GradientDrawable();
        background.setColor(primary ? GREEN : Color.WHITE);
        background.setCornerRadius(dp(16));
        if (!primary) {
            background.setStroke(dp(1), Color.rgb(220, 225, 222));
        }
        card.setBackground(background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(params);
        return card;
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
