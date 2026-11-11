package io.github.jimbaj.max.notification;

public record MaxMessage(String text, MaxFormat format, MaxKeyboard keyboard) {
    public MaxMessage {
        if (text != null && text.length() > 4000) {
            throw new IllegalArgumentException("text длиннее 4000 символов: " + text.length());
        }
    }

    public static MaxMessage text(String text) { return new MaxMessage(text, null, null); }
    public static MaxMessage markdown(String text) { return new MaxMessage(text, MaxFormat.MARKDOWN, null); }
    public static MaxMessage html(String text) { return new MaxMessage(text, MaxFormat.HTML, null); }
    public MaxMessage withKeyboard(MaxKeyboard keyboard) { return new MaxMessage(text, format, keyboard); }
}