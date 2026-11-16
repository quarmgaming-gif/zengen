package pl.namegen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NameGenScreen extends Screen {
    // Zapamiętane między otwarciami ekranu
    private static String template = "/login {full}";
    private static int symbolIndex = 0;

    private TextFieldWidget templateField;
    private final ButtonWidget[] symbolButtons = new ButtonWidget[Names.SYMBOLS.length];
    private int page = 0;
    private int perPage = 20;
    private Text status = Text.empty();

    public NameGenScreen() {
        super(Text.literal("NameGen"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        perPage = Math.max(1, (height - 130) / 10);

        templateField = new TextFieldWidget(textRenderer, cx - 150, 30, 300, 20, Text.literal("Szablon"));
        templateField.setMaxLength(256);
        templateField.setText(template);
        templateField.setChangedListener(s -> template = s);
        addDrawableChild(templateField);

        int startX = cx - (Names.SYMBOLS.length * 24) / 2;
        for (int i = 0; i < Names.SYMBOLS.length; i++) {
            final int idx = i;
            symbolButtons[i] = ButtonWidget.builder(Text.literal(Names.SYMBOLS[i]), b -> {
                symbolIndex = idx;
                refreshSymbolButtons();
            }).dimensions(startX + i * 24, 58, 22, 20).build();
            addDrawableChild(symbolButtons[i]);
        }
        refreshSymbolButtons();

        addDrawableChild(ButtonWidget.builder(Text.literal("< Poprz."), b -> changePage(-1))
                .dimensions(cx - 150, height - 28, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Kopiuj komendy"), b -> copyAll())
                .dimensions(cx - 60, height - 28, 120, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Nast. >"), b -> changePage(1))
                .dimensions(cx + 70, height - 28, 80, 20).build());

        setInitialFocus(templateField);
    }

    private void refreshSymbolButtons() {
        for (int i = 0; i < symbolButtons.length; i++) {
            Text label = Text.literal(Names.SYMBOLS[i]);
            symbolButtons[i].setMessage(i == symbolIndex ? label.copy().formatted(Formatting.GREEN, Formatting.BOLD) : label);
        }
    }

    private int totalPages() {
        return (Names.NAMES.length + perPage - 1) / perPage;
    }

    private void changePage(int delta) {
        page = Math.floorMod(page + delta, totalPages());
    }

    private String build(String name) {
        String sym = Names.SYMBOLS[symbolIndex];
        return template
                .replace("{symbol}", sym)
                .replace("{name}", name)
                .replace("{full}", sym + name);
    }

    private void copyAll() {
        StringBuilder sb = new StringBuilder();
        for (String name : Names.NAMES) {
            sb.append(build(name)).append('\n');
        }
        client.keyboard.setClipboard(sb.toString());
        status = Text.literal("Skopiowano " + Names.NAMES.length + " komend do schowka").formatted(Formatting.GREEN);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        changePage(verticalAmount > 0 ? -1 : 1);
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = width / 2;

        context.drawCenteredTextWithShadow(textRenderer, title, cx, 12, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "Szablon: {full} {symbol} {name}", cx - 150, 20, 0x888888);

        int from = page * perPage;
        int to = Math.min(from + perPage, Names.NAMES.length);
        int y = 88;
        for (int i = from; i < to; i++) {
            context.drawTextWithShadow(textRenderer, (i + 1) + ". " + build(Names.NAMES[i]), cx - 150, y, 0xFFFFFF);
            y += 10;
        }

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Strona " + (page + 1) + "/" + totalPages()), cx, height - 42, 0xAAAAAA);
        context.drawCenteredTextWithShadow(textRenderer, status, cx, height - 52, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
