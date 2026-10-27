package model;

import java.util.List;

/** Một khoản chi: người trả ứng tiền, chia đều cho các người tham gia. */
public record Expense(int id, String title, long amount, int paidBy, List<Integer> participantIds) {
    public Expense {
        participantIds = List.copyOf(participantIds);
    }
}
