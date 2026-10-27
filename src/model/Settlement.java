package model;

/** Một lần chuyển tiền ĐÃ xảy ra (đã ghi nhận). */
public record Settlement(int id, int fromId, int toId, long amount) {
}
