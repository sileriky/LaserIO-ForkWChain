package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.ChannelButton;
import com.direwolf20.laserio.client.screens.widgets.NumberButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.customslot.CardItemSlot;
import com.direwolf20.laserio.common.containers.customslot.CardOverclockSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenNode;
import com.direwolf20.laserio.common.network.packets.PacketUpdateCard;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CardEnergyScreen extends AbstractContainerScreen<CardEnergyContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/energycard.png");

    protected final CardEnergyContainer container;
    protected byte currentMode;
    protected byte currentChannel;
    protected int currentEnergyExtractAmt;
    protected short currentPriority;
    protected byte currentSneaky;
    protected int currentTicks;
    protected boolean currentExact;
    protected int currentRoundRobin;
    protected boolean currentRegulate;
    protected int currentExtractLimitPercent;
    protected int currentInsertLimitPercent;
    protected final ItemStack card;
    protected Map<String, Button> buttons = new HashMap<>();

    protected final String[] sneakyNames = {
            "screen.laserio.default",
            "screen.laserio.down",
            "screen.laserio.up",
            "screen.laserio.north",
            "screen.laserio.south",
            "screen.laserio.west",
            "screen.laserio.east",
    };

    public CardEnergyScreen(CardEnergyContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.card = container.cardItem;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        Button modeButton = buttons.get("mode");
        if (MiscTools.inBounds(modeButton.x, modeButton.y, modeButton.getWidth(), modeButton.getHeight(), mouseX, mouseY)) {
            TranslatableComponent translatableComponents[] = new TranslatableComponent[3];
            translatableComponents[0] = new TranslatableComponent("screen.laserio.insert");
            translatableComponents[1] = new TranslatableComponent("screen.laserio.extract");
            translatableComponents[2] = new TranslatableComponent("screen.laserio.stock");
            this.renderTooltip(matrixStack, translatableComponents[currentMode], mouseX, mouseY);
        }
        Button channelButton = buttons.get("channel");
        if (MiscTools.inBounds(channelButton.x, channelButton.y, channelButton.getWidth(), channelButton.getHeight(), mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TextComponent(String.valueOf(currentChannel)), mouseX, mouseY);
        }
        Button sneakyButton = buttons.get("sneaky");
        if (MiscTools.inBounds(sneakyButton.x, sneakyButton.y, sneakyButton.getWidth(), sneakyButton.getHeight(), mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TranslatableComponent(String.valueOf(sneakyNames[currentSneaky + 1])), mouseX, mouseY);
        }
        Button amountButton = buttons.get("amount");
        if (MiscTools.inBounds(amountButton.x, amountButton.y, amountButton.getWidth(), amountButton.getHeight(), mouseX, mouseY)) {
            if (showExtractAmt()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.extractamt"), mouseX, mouseY);
            }
            if (showPriority()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.priority"), mouseX, mouseY);
            }
        }
        Button regulate = buttons.get("regulate");
        if (MiscTools.inBounds(regulate.x, regulate.y, regulate.getWidth(), regulate.getHeight(), mouseX, mouseY)) {
            if (showRegulate()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.regulate"), mouseX, mouseY);
            }
        }
        Button roundrobin = buttons.get("roundrobin");
        if (MiscTools.inBounds(roundrobin.x, roundrobin.y, roundrobin.getWidth(), roundrobin.getHeight(), mouseX, mouseY)) {
            if (showRoundRobin()) {
                TranslatableComponent translatableComponents[] = new TranslatableComponent[3];
                translatableComponents[0] = new TranslatableComponent("screen.laserio.false");
                translatableComponents[1] = new TranslatableComponent("screen.laserio.true");
                translatableComponents[2] = new TranslatableComponent("screen.laserio.enforced");
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.roundrobin").append(translatableComponents[currentRoundRobin]), mouseX, mouseY);
            }
        }
        Button exact = buttons.get("exact");
        if (MiscTools.inBounds(exact.x, exact.y, exact.getWidth(), exact.getHeight(), mouseX, mouseY)) {
            if (showExtractAmt()) { //Exact is the same conditions as ExtractAmt
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.exact"), mouseX, mouseY);
            }
        }
        Button speedButton = buttons.get("speed");
        if (MiscTools.inBounds(speedButton.x, speedButton.y, speedButton.getWidth(), speedButton.getHeight(), mouseX, mouseY)) {
            if (showExtractAmt()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.tickSpeed"), mouseX, mouseY);
            }
        }
        Button limitButton = buttons.get("limit");
        if (MiscTools.inBounds(limitButton.x, limitButton.y, limitButton.getWidth(), limitButton.getHeight(), mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.energylimit"), mouseX, mouseY);
        }
    }

    public void addAmtButton() {
        buttons.put("amount", new NumberButton(getGuiLeft() + 125, getGuiTop() + 25, 46, 12, currentMode == 0 ? currentPriority : currentEnergyExtractAmt, (button) -> {
            changeAmount(-1);
        }));
    }

    public void addLimitButton() {
        buttons.put("limit", new NumberButton(getGuiLeft() + 147, getGuiTop() + 53, 24, 12, showExtractLimit() ? currentExtractLimitPercent : currentInsertLimitPercent, (button) -> {
            changeLimitAmount(-1);
        }));
    }

    public void addModeButton() {
        ResourceLocation[] modeTextures = new ResourceLocation[3];
        modeTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeinserter.png");
        modeTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeextractor.png");
        modeTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modestocker.png");
        buttons.put("mode", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, modeTextures, currentMode, (button) -> {
            currentMode = BaseCard.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
            ((NumberButton) buttons.get("amount")).setValue(currentMode == 0 ? currentPriority : currentEnergyExtractAmt);
            ((NumberButton) buttons.get("limit")).setValue(showExtractLimit() ? currentExtractLimitPercent : currentInsertLimitPercent);
            modeChange();
        }));
    }

    @Override
    public void init() {
        super.init();
        currentMode = BaseCard.getTransferMode(card);
        currentChannel = BaseCard.getChannel(card);
        currentEnergyExtractAmt = CardEnergy.getEnergyExtractAmt(card);
        currentPriority = BaseCard.getPriority(card);
        currentSneaky = BaseCard.getSneaky(card);
        currentTicks = CardEnergy.getExtractSpeed(card);
        currentExact = BaseCard.getExact(card);
        currentRoundRobin = BaseCard.getRoundRobin(card);
        currentRegulate = BaseCard.getRegulate(card);
        currentExtractLimitPercent = CardEnergy.getExtractLimitPercent(card);
        currentInsertLimitPercent = CardEnergy.getInsertLimitPercent(card);

        addAmtButton();
        addLimitButton();

        buttons.put("speed", new NumberButton(getGuiLeft() + 147, getGuiTop() + 39, 24, 12, currentTicks, (button) -> {
            changeTick(-1);
        }));

        ResourceLocation[] exactTextures = new ResourceLocation[2];
        exactTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/exactfalse.png");
        exactTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/exacttrue.png");
        buttons.put("exact", new ToggleButton(getGuiLeft() + 25, getGuiTop() + 25, 16, 16, exactTextures, currentExact ? 1 : 0, (button) -> {
            currentExact = !currentExact;
            ((ToggleButton) button).setTexturePosition(currentExact ? 1 : 0);
        }));

        ResourceLocation[] roundRobinTextures = new ResourceLocation[3];
        roundRobinTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobinfalse.png");
        roundRobinTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobintrue.png");
        roundRobinTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobinenforced.png");
        buttons.put("roundrobin", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, roundRobinTextures, currentRoundRobin, (button) -> {
            currentRoundRobin = currentRoundRobin == 2 ? 0 : currentRoundRobin + 1;
            ((ToggleButton) button).setTexturePosition(currentRoundRobin);
        }));

        ResourceLocation[] regulateTextures = new ResourceLocation[2];
        regulateTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/regulatefalse.png");
        regulateTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/regulatetrue.png");
        buttons.put("regulate", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, regulateTextures, currentRegulate ? 1 : 0, (button) -> {
            currentRegulate = !currentRegulate;
            ((ToggleButton) button).setTexturePosition(currentRegulate ? 1 : 0);
        }));

        addModeButton();

        buttons.put("channel", new ChannelButton(getGuiLeft() + 5, getGuiTop() + 65, 16, 16, currentChannel, (button) -> {
            currentChannel = BaseCard.nextChannel(card);
            ((ChannelButton) button).setChannel(currentChannel);
        }));

        ResourceLocation[] sneakyTextures = new ResourceLocation[7];
        sneakyTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky.png");
        sneakyTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-down.png");
        sneakyTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-up.png");
        sneakyTextures[3] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-north.png");
        sneakyTextures[4] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-south.png");
        sneakyTextures[5] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-west.png");
        sneakyTextures[6] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-east.png");
        buttons.put("sneaky", new ToggleButton(getGuiLeft() + 25, getGuiTop() + 5, 16, 16, sneakyTextures, currentSneaky + 1, (button) -> {
            currentSneaky = BaseCard.nextSneaky(card);
            ((ToggleButton) button).setTexturePosition(currentSneaky + 1);
        }));

        if (container.direction != -1) {
            buttons.put("return", new Button(getGuiLeft() - 25, getGuiTop() + 1, 25, 20, new TextComponent("<--"), (button) -> {
                openNode();
            }));
        }

        for (Map.Entry<String, Button> button : buttons.entrySet()) {
            addRenderableWidget(button.getValue());
        }

        if (card.getCount() > 1) {
            for (int i = 0; i < container.SLOTS; i++) {
                if (i >= container.slots.size()) continue;
                Slot slot = container.getSlot(i);
                if (slot instanceof CardItemSlot cardItemSlot)
                    cardItemSlot.setEnabled(false);
                if (slot instanceof CardOverclockSlot cardOverclockSlot)
                    cardOverclockSlot.setEnabled(false);
            }
        }
        modeChange();
    }

    public void modeChange() {
        Button speedButton = buttons.get("speed");
        Button exactButton = buttons.get("exact");
        Button rrButton = buttons.get("roundrobin");
        Button regulateButton = buttons.get("regulate");
        if (currentMode == 0) { //insert
            removeWidget(speedButton);
            removeWidget(exactButton);
            removeWidget(rrButton);
            removeWidget(regulateButton);
        } else if (currentMode == 1) { //extract
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            if (!renderables.contains(exactButton))
                addRenderableWidget(exactButton);
            if (!renderables.contains(rrButton))
                addRenderableWidget(rrButton);
            removeWidget(regulateButton);
        } else { //stock
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            if (!renderables.contains(exactButton))
                addRenderableWidget(exactButton);
            if (!renderables.contains(regulateButton))
                addRenderableWidget(regulateButton);
            removeWidget(rrButton);
        }
    }

    public void changeAmount(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 100;
        int overClockers = container.getSlot(0).getItem().getCount();
        int max = 1000;
        switch (overClockers) {
            case 1:
                max = 4000;
                break;
            case 2:
                max = 16000;
                break;
            case 3:
                max = 32000;
                break;
            case 4:
                max = 100000;
                break;
        }
        if (change < 0) {
            if (currentMode == 0) {
                currentPriority = (short) (Math.max(currentPriority + change, -4096));
            } else {
                currentEnergyExtractAmt = (Math.max(currentEnergyExtractAmt + change, 100));
            }
        } else {
            if (currentMode == 0) {
                currentPriority = (short) (Math.min(currentPriority + change, 4096));
            } else {
                currentEnergyExtractAmt = (Math.min(currentEnergyExtractAmt + change, max));
            }
        }
    }

    public void changeLimitAmount(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 100;
        if (change < 0) {
            if (showExtractLimit()) {
                currentExtractLimitPercent = Math.max(currentExtractLimitPercent + change, 0);
            } else {
                currentInsertLimitPercent = Math.max(currentInsertLimitPercent + change, 0);
            }
        } else {
            if (showExtractLimit()) {
                currentExtractLimitPercent = Math.min(currentExtractLimitPercent + change, 100);
            } else {
                currentInsertLimitPercent = Math.min(currentInsertLimitPercent + change, 100);
            }
        }
    }

    public void changeTick(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 64;
        if (change < 0) {
            currentTicks = (Math.max(currentTicks + change, 1));
        } else {
            currentTicks = (Math.min(currentTicks + change, 1200));
        }
    }

    private boolean showExtractAmt() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.INSERT;
    }

    private boolean showPriority() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.INSERT;
    }

    private boolean showRegulate() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.STOCK;
    }

    private boolean showRoundRobin() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.EXTRACT;
    }

    private boolean showExtractLimit() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.EXTRACT;
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        /*stack.pushPose();
        stack.scale(0.5f, 0.5f, 0.5f);
        if (showExtractAmt()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.extractamt").getString() + ":", 5*2, 45*2, Color.DARK_GRAY.getRGB());
        }
        if (showPriority()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.priority").getString() + ":", 5*2, 50*2, Color.DARK_GRAY.getRGB());
        }
        stack.popPose();*/
        //super.renderLabels(matrixStack, x, y);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        saveSettings();
        super.onClose();
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        InputConstants.Key mouseKey = InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_);
        if (p_keyPressed_1_ == 256 || minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            onClose();

            return true;
        }

        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }


    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static TranslatableComponent getTrans(String key, Object... args) {
        return new TranslatableComponent(LaserIO.MODID + "." + key, args);
    }

    public void setExtract(NumberButton amountButton, int btn) {
        if (btn == 0)
            changeAmount(100);
        else if (btn == 1)
            changeAmount(-100);
        amountButton.setValue(currentMode == 0 ? currentPriority : currentEnergyExtractAmt);
        amountButton.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void setLimitExtract(NumberButton amountButton, int btn) {
        if (btn == 0)
            changeLimitAmount(1);
        else if (btn == 1)
            changeLimitAmount(-1);
        amountButton.setValue(showExtractLimit() ? currentExtractLimitPercent : currentInsertLimitPercent);
        amountButton.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void saveSettings() {
        PacketHandler.sendToServer(new PacketUpdateCard(currentMode, currentChannel, currentEnergyExtractAmt, currentPriority, currentSneaky, (short) currentTicks, currentExact, currentRegulate, (byte) currentRoundRobin, currentExtractLimitPercent, currentInsertLimitPercent));
    }

    public void openNode() {
        saveSettings();
        PacketHandler.sendToServer(new PacketOpenNode(container.sourceContainer, container.direction));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        ChannelButton channelButton = ((ChannelButton) buttons.get("channel"));
        if (MiscTools.inBounds(channelButton.x, channelButton.y, channelButton.getWidth(), channelButton.getHeight(), x, y)) {
            if (btn == 0)
                currentChannel = BaseCard.nextChannel(card);
            else if (btn == 1)
                currentChannel = BaseCard.previousChannel(card);
            channelButton.setChannel(currentChannel);
            channelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        NumberButton amountButton = ((NumberButton) buttons.get("amount"));
        if (MiscTools.inBounds(amountButton.x, amountButton.y, amountButton.getWidth(), amountButton.getHeight(), x, y)) {
            setExtract(amountButton, btn);
            return true;
        }
        NumberButton limitButton = ((NumberButton) buttons.get("limit"));
        if (MiscTools.inBounds(limitButton.x, limitButton.y, limitButton.getWidth(), limitButton.getHeight(), x, y)) {
            setLimitExtract(limitButton, btn);
            return true;
        }

        NumberButton speedButton = ((NumberButton) buttons.get("speed"));
        if (MiscTools.inBounds(speedButton.x, speedButton.y, speedButton.getWidth(), speedButton.getHeight(), x, y)) {
            if (btn == 0)
                changeTick(1);
            else if (btn == 1)
                changeTick(-1);
            speedButton.setValue(currentTicks);
            speedButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return super.mouseClicked(x, y, btn);
    }
}