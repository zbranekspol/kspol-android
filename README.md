# +K spol. — STABLE RESET v8 / krok 6

Navazuje výhradně na uživatelem potvrzený stabilní krok 5. Kód z nestabilní v7 nebyl použit.

## Rozsah kroku 6

- zachované logo +K spol.
- produktové karty s obrázky
- vícepoložková poptávka s možností ODEBRAT
- MOJE DOTAZY a lokální čísla dotazů
- vyhledávací okénko na hlavní obrazovce
- nápověda „Hledat podle názvu nebo popisu…“
- okamžité výsledky při psaní
- hledání pouze podle názvu produktu a jeho popisu
- všech 25 hlavních kategorií Shop5 je přímo pod vyhledáváním na titulní stránce
- šest původních výběrových kategorií a tlačítko ZOBRAZIT VŠE byly odstraněny
- kategorie, podkategorie a aktivní zboží se čtou z denního XML katalogu Shop5
- produktové údaje zahrnují název, popis, cenu, dostupnost, kategorii a obrázek
- katalog se stahuje až po kliknutí uživatele, nikdy při spuštění aplikace
- lokální cache má denní cyklus od 6:00 českého času
- při chybě aktualizace se použije poslední funkční kopie
- produkty se zobrazují po 20 položkách, další se načtou tlačítkem
- zákaznický záznam je vždy dotaz; rezervace vznikne až po pozdějším potvrzení zaměstnancem
- bez WebView

Tento krok je určen k samostatnému testu stability před dalším rozšiřováním.
