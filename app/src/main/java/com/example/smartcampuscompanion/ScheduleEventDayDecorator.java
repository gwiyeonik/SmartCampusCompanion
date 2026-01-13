package com.example.smartcampuscompanion;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;

public class ScheduleEventDayDecorator implements DayViewDecorator {

    private HashSet<CalendarDay> eventDays;

    public ScheduleEventDayDecorator(HashSet<CalendarDay> eventDays) {
        this.eventDays = eventDays;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return eventDays.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(10, 0xFF3C7962)); // green dot
    }
}
