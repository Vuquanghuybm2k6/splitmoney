package model;

/** Thành viên của nhóm. */
public record Member(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
