package com.telit.zhkt_three.Activity.MistakesCollection;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.HashSet;

import com.telit.zhkt_three.R;

/**
 * Decorate 2 days.
 */
public class RangeDayDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> list = new HashSet<>();
    private final Drawable drawable;

    public RangeDayDecorator(final Context context) {
        drawable = context.getResources().getDrawable(R.drawable.calendar_selector);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return list.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }

    /**
     * We're changing the dates, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
     */
    public void addFirstAndLast(final CalendarDay first, final CalendarDay last) {
        list.clear();
        list.add(first);
        list.add(last);
    }
}