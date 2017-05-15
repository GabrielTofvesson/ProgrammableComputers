package org.teamavion.pcomp.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.teamavion.pcomp.PComp;
import org.teamavion.pcomp.net.DataListener;
import org.teamavion.pcomp.tile.TileEntityComputer;
import org.teamavion.util.support.NetworkChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class GUIComputer extends GuiScreen implements DataListener<HashMap<Integer, String>>{

    protected final int viewportMin = -90, viewportMax = 45, maxInputLines = 128, textFieldLen = 240, textColor = 0xFFFFFF;
    protected final long keyPressTimeout = 0; // timeout period between keypresses to minimize spam
    protected final ArrayList<GuiTextField> viewportInput = new ArrayList<>();
    protected final TileEntityComputer computer;
    protected boolean viewportUpdated = true;
    protected int selected = 0, viewportSelect = 0;
    protected long keyPressTime = 0;
    protected GuiTextField[] inputLines;
    protected GuiButton button;

    public GUIComputer(TileEntityComputer computer){
        this.computer = computer;
        NBTTagCompound n = new NBTTagCompound();
        n.setString("update", "");
        PComp.instance.channel.sendToServer(new NetworkChannel.WorldEvent(computer.getPos(), computer.getWorld().provider.getDimension(), n));
    }

    @Override
    public void drawBackground(int tint) {
        super.drawBackground(tint);
    }

    @Override
    public void initGui() {
        inputLines = new GuiTextField[maxInputLines];
        for(int i = 0; i<maxInputLines; ++i) {
            inputLines[i] = new GuiTextField(i, this.fontRendererObj, this.width / 2 - textFieldLen / 2, this.height / 2 + viewportMin + i * 15, textFieldLen, 15);
            inputLines[i].setMaxStringLength(textFieldLen); // Allow 6x as many characters as the length of the graphical element
            inputLines[i].setEnableBackgroundDrawing(false);
        }
        button = new GuiButton(-1, this.width / 2 - textFieldLen / 2, this.height / 2 + viewportMax + 30, "Execute");
        button.width = textFieldLen;
        inputLines[selected].setFocused(true);
        computer.registerDataListener(this);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        long time = System.currentTimeMillis();
        if(time-keyPressTime<keyPressTimeout/(keyCode==14?4:1)) return; // Slower spam type when key is held (spammed, I guess)
        keyPressTime = time;
        super.keyTyped(typedChar, keyCode);
        if(keyCode==Keyboard.KEY_UP){
            inputLines[selected].setFocused(false);
            if(viewportSelect>0) --viewportSelect;
            else if(selected>0){
                viewportUpdated = true;
                for(GuiTextField t : inputLines) t.yPosition += 15;
            }
            if(selected>0) --selected;
            inputLines[selected].setFocused(true);
        }
        else if(keyCode==Keyboard.KEY_DOWN){
            inputLines[selected].setFocused(false);
            if(viewportSelect+1<viewportInput.size()) ++viewportSelect;
            else if(selected+1<maxInputLines){
                viewportUpdated = true;
                for(GuiTextField t : inputLines) t.yPosition -= 15;
            }
            if(selected+1<maxInputLines) ++selected;
            inputLines[selected].setFocused(true);
        }
        else if(keyCode==Keyboard.KEY_RETURN){
            String textAfter = inputLines[selected].getText().substring(inputLines[selected].getCursorPosition());
            inputLines[selected].setText(inputLines[selected].getText().substring(0, inputLines[selected].getCursorPosition()));
            String overflow;
            for(int i = selected+1; i<maxInputLines; ++i){
                overflow = inputLines[i].getText();
                inputLines[i].setText(textAfter);
                textAfter = overflow;
            }
            if(selected+1<maxInputLines){
                inputLines[selected+1].setCursorPosition(0);
                keyTyped('\000', Keyboard.KEY_DOWN);
            }
        }else if(keyCode==Keyboard.KEY_BACK && selected!=0 && inputLines[selected].getCursorPosition()==0){
            keyTyped('\000', Keyboard.KEY_UP);
            int pos = inputLines[selected].getText().length();
            inputLines[selected].setCursorPosition(inputLines[selected].getText().length());
            if(inputLines[selected].getText().length()==0) { // Shift up text to selected line
                for(int i = selected; i+1<maxInputLines; ++i) inputLines[i].setText(inputLines[i+1].getText());
                inputLines[maxInputLines-1].setText("");
                inputLines[selected].setCursorPosition(0);
            }else if(inputLines[selected+1].getText().length()!=0) { // Move text from previous line
                int difference = inputLines[selected].getMaxStringLength() - inputLines[selected].getText().length();
                if(difference>=inputLines[selected+1].getText().length()){
                    inputLines[selected].setText(inputLines[selected].getText()+inputLines[selected+1].getText());
                    for(int i = selected+1; i+1<maxInputLines; ++i) inputLines[i].setText(inputLines[i+1].getText());
                    inputLines[maxInputLines-1].setText("");
                }else{
                    inputLines[selected].setText(inputLines[selected].getText()+inputLines[selected+1].getText().substring(0, difference));
                    inputLines[selected+1].setText(inputLines[selected+1].getText().substring(difference));
                }
            }else{ // Shift all text
                for(int i = selected+1; i+1<maxInputLines; ++i) inputLines[i].setText(inputLines[i+1].getText());
                inputLines[maxInputLines-1].setText("");
            }
            inputLines[selected].setCursorPosition(pos);
        }else if(keyCode==Keyboard.KEY_DELETE){
            String s;
            int pos = inputLines[selected].getCursorPosition();
            inputLines[selected].setText(
                    (s=inputLines[selected].getText()).substring(0, inputLines[selected].getCursorPosition())+
                            inputLines[selected].getText().substring(Math.min(s.length(), inputLines[selected].getCursorPosition()+1)));
            inputLines[selected].setCursorPosition(pos);
            if(selected+1<maxInputLines) {
                boolean b = inputLines[selected].getCursorPosition() == inputLines[selected].getText().length();
                if (inputLines[selected].getText().length() == 0 || (b && inputLines[selected + 1].getText().length() == 0)) {
                    for (int i = selected + (b ? 1 : 0); i + 1 < maxInputLines; ++i) inputLines[i].setText(inputLines[i + 1].getText());
                    inputLines[maxInputLines - 1].setText("");
                } else if (b) {
                    int difference = inputLines[selected].getMaxStringLength() - inputLines[selected].getText().length();
                    if (difference != 0) {
                        inputLines[selected].setText(inputLines[selected].getText() + inputLines[selected + 1].getText().substring(0, difference = Math.min(difference, inputLines[selected + 1].getText().length())));
                        inputLines[selected + 1].setText(inputLines[selected + 1].getText().substring(difference));
                        inputLines[selected].setCursorPosition(inputLines[selected].getText().length() - difference);
                    }
                }
            }
        }
        else inputLines[selected].textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        inputLines[selected].updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if(viewportUpdated) {
            viewportInput.clear();
            for (GuiTextField t : inputLines)
                if (t.yPosition <= height / 2 + viewportMax && t.yPosition >= height / 2 + viewportMin) viewportInput.add(t);
            viewportUpdated = false;
        }
        for(int i = 0; i<inputLines.length; ++i){
            if(viewportInput.contains(inputLines[i])) {
                inputLines[i].drawTextBox();
                String s;
                drawString(fontRendererObj, s=(i+1)+":", inputLines[i].xPosition - s.length()*6, inputLines[i].yPosition, textColor);
            }
        }
        button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int initSelect = 0;
        for(int i = 0; i<viewportInput.size(); ++i)
            if(viewportInput.get(i).isFocused()){
                initSelect = i;
                break;
            }
        for(GuiTextField t : viewportInput) t.mouseClicked(mouseX, mouseY, mouseButton);
        for(int i = 0; i<viewportInput.size(); ++i)
            if(viewportInput.get(i).isFocused()){
                selected+=i-initSelect;
                viewportSelect+=i-initSelect;
                break;
            }
        if(button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)){
                NBTTagCompound n = new NBTTagCompound();
                n.setString("exec", "");
                sync(n);
        }
    }

    @Override
    public void onGuiClosed() {
        computer.unregisterDataListener(this);
        sync(new NBTTagCompound());
        super.onGuiClosed();
    }

    protected void sync(NBTTagCompound n){
        for(int i = 0; i<inputLines.length; ++i) computer.writeLine(i, inputLines[i].getText());
        computer.writeToNBT(n);
        PComp.instance.channel.sendToServer(new NetworkChannel.WorldEvent(computer.getPos(), computer.getWorld().provider.getDimension(), n));
        computer.markDirty();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        button.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void getData(HashMap<Integer, String> data) {
        for(int i = 0; i<inputLines.length; ++i) inputLines[i].setText(data.getOrDefault(i, ""));
    }
}
