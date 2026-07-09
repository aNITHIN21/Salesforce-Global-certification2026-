import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main application window: year navigation buttons, a 12-month
 * calendar grid, a 7-pole visual dashboard, and a details sidebar.
 */
public class AppWindow extends JFrame {

    private static final Color DEFAULT_BG = Color.WHITE;
    private static final Color HOVER_BG = new Color(200, 225, 255);
    private static final Color SELECTED_BG = new Color(70, 130, 180);
    private static final Color HOLIDAY_BG = new Color(230, 210, 250);
    private static final Color SUNDAY_TEXT = new Color(180, 40, 40);
    private static final Color ON_COLOR = new Color(46, 160, 67);
    private static final Color OFF_COLOR = new Color(200, 60, 60);
    private static final Color HOLIDAY_COLOR = new Color(140, 70, 190);

    private final HolidayManager holidayManager = new HolidayManager();
    private final PoleLogic poleLogic = new PoleLogic(holidayManager);

    private int currentYear = 2026;
    private LocalDate selectedDate;

    private final JLabel yearLabel = new JLabel();
    private final JPanel calendarGrid = new JPanel(new GridLayout(4, 3, 8, 8));
    private final Map<LocalDate, JButton> dateButtons = new HashMap<>();
    private final Map<LocalDate, Color> baseBg = new HashMap<>();
    private final Map<LocalDate, Color> baseFg = new HashMap<>();

    private final JPanel[] poleTiles = new JPanel[PoleLogic.POLE_COUNT];

    private final JLabel dateValue = new JLabel("-");
    private final JLabel dayValue = new JLabel("-");
    private final JLabel holidayValue = new JLabel("-");
    private final JLabel originalPolesValue = new JLabel("-");
    private final JLabel effectiveDayValue = new JLabel("-");
    private final JLabel finalPolesValue = new JLabel("-");

    public AppWindow() {
        super("Smart Pole Lighting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 780));
        setLayout(new BorderLayout(8, 8));

        add(buildTopBar(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(calendarGrid);
        scroll.setBorder(BorderFactory.createTitledBorder("Calendar"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.add(buildDashboard());
        side.add(Box.createVerticalStrut(10));
        side.add(buildInfoPanel());
        side.setPreferredSize(new Dimension(360, 780));
        add(side, BorderLayout.EAST);

        buildCalendarForYear(currentYear);
        selectDate(LocalDate.of(currentYear, 1, 1));

        pack();
        setLocationRelativeTo(null);
    }

    // ---------- Top bar: previous / next year buttons ----------

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));

        JButton prev = new JButton("\u25C0 Prev Year");
        JButton next = new JButton("Next Year \u25B6");

        yearLabel.setFont(yearLabel.getFont().deriveFont(Font.BOLD, 18f));

        prev.addActionListener(e -> changeYear(-1));
        next.addActionListener(e -> changeYear(1));

        bar.add(prev);
        bar.add(yearLabel);
        bar.add(next);
        updateYearLabel();
        return bar;
    }

    private void updateYearLabel() {
        yearLabel.setText("Year: " + currentYear);
        setTitle("Smart Pole Lighting System - " + currentYear);
    }

    private void changeYear(int delta) {
        int newYear = currentYear + delta;
        if (newYear < HolidayManager.MIN_YEAR || newYear > HolidayManager.MAX_YEAR) {
            return;
        }
        currentYear = newYear;
        updateYearLabel();
        buildCalendarForYear(currentYear);
        selectDate(LocalDate.of(currentYear, 1, 1));
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    // ---------- Calendar grid ----------

    private void buildCalendarForYear(int year) {
        calendarGrid.removeAll();
        dateButtons.clear();
        baseBg.clear();
        baseFg.clear();

        for (Month month : Month.values()) {
            calendarGrid.add(buildMonthPanel(year, month));
        }
    }

    private JPanel buildMonthPanel(int year, Month month) {
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.setBorder(BorderFactory.createTitledBorder(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)));
        panel.setBackground(Color.WHITE);

        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        grid.setBackground(Color.WHITE);

        for (String h : new String[]{"M", "T", "W", "T", "F", "S", "S"}) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 10f));
            grid.add(lbl);
        }

        LocalDate first = LocalDate.of(year, month, 1);
        int leadingBlanks = first.getDayOfWeek().getValue() - 1; // 0=Mon
        for (int i = 0; i < leadingBlanks; i++) {
            grid.add(new JLabel(""));
        }

        int daysInMonth = first.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            grid.add(buildDateButton(date));
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JButton buildDateButton(LocalDate date) {
        JButton btn = new JButton(String.valueOf(date.getDayOfMonth()));
        btn.setMargin(new Insets(1, 1, 1, 1));
        btn.setFont(btn.getFont().deriveFont(11f));
        btn.setFocusPainted(false);
        btn.setOpaque(true);

        boolean isHoliday = holidayManager.isHoliday(date);
        boolean isSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY;

        Color bg = isHoliday ? HOLIDAY_BG : DEFAULT_BG;
        Color fg = (isSunday && !isHoliday) ? SUNDAY_TEXT : Color.BLACK;

        baseBg.put(date, bg);
        baseFg.put(date, fg);
        btn.setBackground(bg);
        btn.setForeground(fg);

        if (isHoliday) {
            btn.setToolTipText(holidayManager.getHolidayName(date));
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!date.equals(selectedDate)) {
                    btn.setBackground(HOVER_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!date.equals(selectedDate)) {
                    btn.setBackground(baseBg.get(date));
                }
            }
        });

        btn.addActionListener(e -> selectDate(date));
        dateButtons.put(date, btn);
        return btn;
    }

    private void selectDate(LocalDate date) {
        if (selectedDate != null) {
            JButton prevBtn = dateButtons.get(selectedDate);
            if (prevBtn != null) {
                prevBtn.setBackground(baseBg.get(selectedDate));
                prevBtn.setForeground(baseFg.get(selectedDate));
            }
        }

        selectedDate = date;
        JButton curBtn = dateButtons.get(date);
        if (curBtn != null) {
            curBtn.setBackground(SELECTED_BG);
            curBtn.setForeground(Color.WHITE);
        }

        updateSidePanels(date);
    }

    // ---------- Pole dashboard (7 poles) ----------

    private JPanel buildDashboard() {
        JPanel dash = new JPanel(new GridLayout(2, 4, 8, 8));
        dash.setBorder(BorderFactory.createTitledBorder("Pole Dashboard (7 Poles)"));
        dash.setBackground(Color.WHITE);

        for (int i = 0; i < PoleLogic.POLE_COUNT; i++) {
            JPanel tile = new JPanel(new BorderLayout());
            tile.setBackground(OFF_COLOR);
            tile.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

            JLabel label = new JLabel("Pole " + (i + 1), SwingConstants.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
            tile.add(label, BorderLayout.CENTER);

            poleTiles[i] = tile;
            dash.add(tile);
        }
        return dash;
    }

    private void updateDashboard(PoleLogic.DayStatus status) {
        for (int i = 0; i < PoleLogic.POLE_COUNT; i++) {
            switch (status.poleStates[i]) {
                case 1:
                    poleTiles[i].setBackground(ON_COLOR);
                    break;
                case 2:
                    poleTiles[i].setBackground(HOLIDAY_COLOR);
                    break;
                default:
                    poleTiles[i].setBackground(OFF_COLOR);
            }
        }
    }

    // ---------- Info panel ----------

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Selected Date Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        row = addRow(panel, gbc, row, "Date:", dateValue);
        row = addRow(panel, gbc, row, "Day of Week:", dayValue);
        row = addRow(panel, gbc, row, "Holiday Status:", holidayValue);
        row = addRow(panel, gbc, row, "Original Assigned Poles:", originalPolesValue);
        row = addRow(panel, gbc, row, "Effective Working Day:", effectiveDayValue);
        addRow(panel, gbc, row, "Final Shifted Active Poles:", finalPolesValue);

        for (JLabel v : new JLabel[]{dateValue, dayValue, holidayValue, originalPolesValue, effectiveDayValue, finalPolesValue}) {
            v.setFont(v.getFont().deriveFont(Font.BOLD));
        }
        return panel;
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(value, gbc);
        return row + 1;
    }

    private void updateSidePanels(LocalDate date) {
        PoleLogic.DayStatus status = poleLogic.computeStatus(date);

        dateValue.setText(status.date.toString());
        dayValue.setText(status.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        if (status.holiday) {
            holidayValue.setText(status.holidayName + " (Holiday)");
            holidayValue.setForeground(HOLIDAY_COLOR);
        } else {
            holidayValue.setText("Working Day");
            holidayValue.setForeground(new Color(0, 128, 0));
        }

        originalPolesValue.setText(formatPoles(status.originalPoles));

        if (status.holiday) {
            effectiveDayValue.setText(status.effectiveWorkingDay + " ("
                    + status.effectiveWorkingDay.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ")");
        } else {
            effectiveDayValue.setText("Same day (no shift needed)");
        }

        finalPolesValue.setText(formatPoles(status.finalPoles));

        updateDashboard(status);
    }

    private String formatPoles(Set<Integer> poles) {
        return poles.stream().sorted().map(id -> "P" + id).collect(Collectors.joining(", "));
    }
}
