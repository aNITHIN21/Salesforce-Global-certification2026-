import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Business logic for the Smart Pole Lighting System (7 poles).
 * <p>
 * Weekday rule:
 * <ul>
 *   <li>Monday, Wednesday, Friday, Sunday -&gt; poles 1, 3, 5, 7</li>
 *   <li>Tuesday, Thursday, Saturday -&gt; poles 2, 4, 6</li>
 * </ul>
 * On a holiday, all poles are deactivated for that date, and the
 * system looks ahead to the next non-holiday date to report which
 * poles would be required there ("shifted" requirement).
 */
public class PoleLogic {

    public static final int POLE_COUNT = 7;

    private static final Set<Integer> ODD_GROUP = setOf(1, 3, 5, 7);
    private static final Set<Integer> EVEN_GROUP = setOf(2, 4, 6);

    private final HolidayManager holidayManager;

    public PoleLogic(HolidayManager holidayManager) {
        this.holidayManager = holidayManager;
    }

    /** Result of evaluating one date: holiday status + pole assignments. */
    public static class DayStatus {
        public final LocalDate date;
        public final DayOfWeek dayOfWeek;
        public final boolean holiday;
        public final String holidayName;
        public final Set<Integer> originalPoles;
        public final Set<Integer> finalPoles;
        public final LocalDate effectiveWorkingDay;
        /** Index 0 = pole 1 ... index 6 = pole 7. 0=OFF, 1=ON, 2=HOLIDAY. */
        public final int[] poleStates;

        DayStatus(LocalDate date, DayOfWeek dow, boolean holiday, String holidayName,
                  Set<Integer> originalPoles, Set<Integer> finalPoles,
                  LocalDate effectiveWorkingDay, int[] poleStates) {
            this.date = date;
            this.dayOfWeek = dow;
            this.holiday = holiday;
            this.holidayName = holidayName;
            this.originalPoles = originalPoles;
            this.finalPoles = finalPoles;
            this.effectiveWorkingDay = effectiveWorkingDay;
            this.poleStates = poleStates;
        }
    }

    /** Returns the poles assigned to the given weekday under normal rotation. */
    public static Set<Integer> polesForDay(DayOfWeek dow) {
        switch (dow) {
            case MONDAY:
            case WEDNESDAY:
            case FRIDAY:
            case SUNDAY:
                return ODD_GROUP;
            default:
                return EVEN_GROUP;
        }
    }

    /** Computes the full pole status for a given date, applying holiday shifting. */
    public DayStatus computeStatus(LocalDate date) {
        boolean isHoliday = holidayManager.isHoliday(date);
        String holidayName = holidayManager.getHolidayName(date);
        Set<Integer> originalPoles = polesForDay(date.getDayOfWeek());

        Set<Integer> finalPoles;
        LocalDate effectiveDay;

        if (isHoliday) {
            effectiveDay = date.plusDays(1);
            while (holidayManager.isHoliday(effectiveDay)) {
                effectiveDay = effectiveDay.plusDays(1);
            }
            finalPoles = polesForDay(effectiveDay.getDayOfWeek());
        } else {
            effectiveDay = date;
            finalPoles = originalPoles;
        }

        int[] states = new int[POLE_COUNT];
        for (int i = 0; i < POLE_COUNT; i++) {
            int poleId = i + 1;
            if (isHoliday) {
                states[i] = 2; // HOLIDAY
            } else if (originalPoles.contains(poleId)) {
                states[i] = 1; // ON
            } else {
                states[i] = 0; // OFF
            }
        }

        return new DayStatus(date, date.getDayOfWeek(), isHoliday, holidayName,
                originalPoles, finalPoles, effectiveDay, states);
    }

    private static Set<Integer> setOf(int... ids) {
        Set<Integer> s = new LinkedHashSet<>();
        for (int id : ids) {
            s.add(id);
        }
        return s;
    }
}
