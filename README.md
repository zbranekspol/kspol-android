# +K spol. — STABLE RESET v8 / krok 9

Navazuje výhradně na uživatelem potvrzený stabilní krok 8. Kód z nestabilní v7 nebyl použit.

## Rozsah kroku 9

- černobílé logo +K spol. převzaté ze záhlaví oficiálního e-shopu
- pozadí záhlaví z oficiální facebookové prezentace +K spol.
- text „Ověření dostupnosti produktů na prodejně“ je přímo v hlavním záhlaví
- duplicitní nadpis pod záhlavím byl odstraněn
- systémové tlačítko Zpět vrací o jednu obrazovku včetně podkategorií a administrace
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
- potvrzení po vytvoření používá text „Dotaz odeslán“
- každá položka z XML má malý odkaz ZOBRAZIT V E-SHOPU na přesnou produktovou stránku
- odkazy se otevírají v systémovém prohlížeči a jsou omezené na doménu zbrane-kspol.cz
- na titulní stránce je vstup PRO ZAMĚSTNANCE
- zaměstnanecké přihlášení, ADMINISTRAČNÍ ROZHRANÍ, ZMĚNIT HESLO a ODHLÁSIT
- administrační část je zatím označena jako TEST REŽIM a používá lokální přihlášení
- bez WebView

Tento krok je určen k samostatnému testu stability před dalším rozšiřováním.
