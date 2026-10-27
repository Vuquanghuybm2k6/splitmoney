package model;

/** Một gợi ý chuyển tiền CHƯA xảy ra. */
public record Transfer(int fromId, int toId, long amount) {
}
