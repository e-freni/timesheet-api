package com.jaewa.timesheet.model.specialday;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public enum FixedSpecialDay {
    NEW_YEARS_EVE(new SpecialDay("1/1", "Capodanno")),
    EPIPHANY(new SpecialDay("6/1", "Epifania")),
    APRIL_TWENTY_FIFTH(new SpecialDay("25/4", "Festa della liberazione")),
    WORKERS_DAY(new SpecialDay("1/5", "Festa dei lavoratori")),
    REPUBLIC_DAY(new SpecialDay("2/6", "Festa della Repubblica")),
    MIDSUMMER(new SpecialDay("24/6", "Festa di San Giovanni (solo a Firenze!)")),
    MID_AUGUST(new SpecialDay("15/8", "Ferragosto")),
    ALL_SAINTS_DAY(new SpecialDay("1/11", "Ognissanti")),
    IMMACULATE_CONCEPTION(new SpecialDay("8/12", "Immacolata concezione")),
    CHRISTMAS(new SpecialDay("25/12", "Natale")),
    BOXING_DAY(new SpecialDay("26/12", "Santo Stefano"));

    private final SpecialDay specialDay;


    public static List<SpecialDay> getAllFixedSpecialDays() {
        ArrayList<SpecialDay> fixedSpecialDays = new ArrayList<>();
        fixedSpecialDays.add(NEW_YEARS_EVE.getSpecialDay());
        fixedSpecialDays.add(EPIPHANY.getSpecialDay());
        fixedSpecialDays.add(APRIL_TWENTY_FIFTH.getSpecialDay());
        fixedSpecialDays.add(WORKERS_DAY.getSpecialDay());
        fixedSpecialDays.add(REPUBLIC_DAY.getSpecialDay());
        fixedSpecialDays.add(MIDSUMMER.getSpecialDay());
        fixedSpecialDays.add(MID_AUGUST.getSpecialDay());
        fixedSpecialDays.add(ALL_SAINTS_DAY.getSpecialDay());
        fixedSpecialDays.add(IMMACULATE_CONCEPTION.getSpecialDay());
        fixedSpecialDays.add(CHRISTMAS.getSpecialDay());
        fixedSpecialDays.add(BOXING_DAY.getSpecialDay());

        return fixedSpecialDays;
    }

}
