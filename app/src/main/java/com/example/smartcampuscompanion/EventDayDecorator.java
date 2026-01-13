package com.example.smartcampuscompanion;

import android.graphics.Color;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;

public class EventDayDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;

    public EventDayDecorator(HashSet<CalendarDay> dates) {
        this.dates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(8, Color.parseColor("#3c7962")));
    }
}
