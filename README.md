# +K spol. — STABLE RESET v8 / krok 14

Navazuje výhradně na uživatelem potvrzený stabilní krok 9. Kód z nestabilní v7 nebyl použit.

## Rozsah kroku 14

- černobílé logo +K spol. převzaté ze záhlaví oficiálního e-shopu
- pozadí záhlaví z oficiální facebookové prezentace +K spol.
- text „Ověření dostupnosti produktů na prodejně“ je přímo v hlavním záhlaví
- duplicitní nadpis pod záhlavím byl odstraněn
- ikona košíku je v pravém horním rohu obrázku záhlaví a otevírá celou poptávku
- drobečková cesta je pod obrázkem záhlaví, nad vyhledáváním, a ukazuje aktuální kategorii i podkategorii
- klepnutí na drobečkovou cestu vrací o jednu úroveň výš
- systémové gesto Zpět z levého i pravého okraje provede stejný krok zpět jako drobečková cesta
- vlastní gesto zprava doleva uvnitř obsahu zůstává podporované
- produkty lze řadit podle ceny vzestupně i sestupně a podle názvu A–Z i Z–A
- zvolené řazení platí v kategoriích, podkategoriích i výsledcích hledání
- popisek řazení a rozbalovací nabídka jsou v jednom kompaktním řádku
- každá kategorie a podkategorie má vlastní pole hledání hned pod záhlavím
- hledání filtruje pouze názvy a popisy produktů v právě otevřené části katalogu
- systémové tlačítko Zpět vrací o jednu obrazovku včetně podkategorií a administrace
- produktové karty s obrázky
- vícepoložková poptávka s možností ODEBRAT
- MOJE DOTAZY a lokální čísla dotazů
- vyhledávací okénko na hlavní obrazovce
- nápověda „Hledat podle názvu nebo popisu…“
- okamžité výsledky při psaní
- hledání pouze podle názvu produktu a jeho popisu
- parser podporuje Google i vlastní Heureka XML Shop5 a zahazuje vše, co není výslovně skladem
- URL feedu lze změnit pouze v zaměstnanecké administraci a uloží se lokálně v telefonu
- denní cache se obnovuje po 6:00
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
