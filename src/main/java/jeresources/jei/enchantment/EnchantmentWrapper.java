package jeresources.jei.enchantment;

import jeresources.entry.EnchantmentEntry;
import jeresources.registry.EnchantmentRegistry;
import jeresources.util.Font;
import jeresources.util.TranslationHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class EnchantmentWrapper implements IRecipeWrapper {
    private static final int ENTRIES_PER_PAGE = 11;
    private static final int ENCHANT_X = 35;
    private static final int FIRST_ENCHANT_Y = 7;
    private static final int SPACING_Y = 10;
    private static final int PAGE_X = 55;
    private static final int PAGE_Y = 120;
    private static final int CYCLE_TIME = 2;

    private final ItemStack itemStack;
    private final List<EnchantmentEntry> enchantments;
    private final int lastSet;
    private int set, nextCycle;

    @Nullable
    public static EnchantmentWrapper create(@Nonnull ItemStack itemStack) {
        List<EnchantmentEntry> enchantments = new LinkedList<>(EnchantmentRegistry.getInstance().getEnchantments(itemStack));
        if (enchantments.isEmpty())
            return null;
        return new EnchantmentWrapper(itemStack, enchantments);
    }

    private EnchantmentWrapper(@Nonnull ItemStack itemStack, @Nonnull List<EnchantmentEntry> enchantments) {
        this.itemStack = itemStack;
        this.enchantments = enchantments;
        this.set = 0;
        this.lastSet = this.enchantments.size() / (ENTRIES_PER_PAGE + 1);
        this.nextCycle = ((int) System.currentTimeMillis() / 1000) + CYCLE_TIME;
    }

    public List<EnchantmentEntry> getEnchantments() {
        doCycle();
        int last = set * ENTRIES_PER_PAGE + ENTRIES_PER_PAGE;
        if (last >= this.enchantments.size()) last = this.enchantments.size();
        return this.enchantments.subList(set * ENTRIES_PER_PAGE, last);
    }

    private void doCycle() {
        if (((int) System.currentTimeMillis() / 1000) > nextCycle) {
            if (!GuiScreen.isShiftKeyDown()) // Don't cycle when holding shift
                this.set = this.set == lastSet ? 0 : this.set + 1;
            this.nextCycle = ((int) System.currentTimeMillis() / 1000) + CYCLE_TIME;
        }
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInput(ItemStack.class, itemStack);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int y = FIRST_ENCHANT_Y;
        for (EnchantmentEntry enchantment : getEnchantments()) {
            Font.normal.print(enchantment.getTranslatedWithLevels(), ENCHANT_X, y);
            y += SPACING_Y;
        }
        if (this.lastSet > 0) {
            String toPrint = TranslationHelper.getLocalPageInfo(this.set, this.lastSet);
            Font.normal.print(toPrint, PAGE_X, PAGE_Y);
        }
    }
}
