package org.teamavion.pcomp.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.teamavion.pcomp.tile.TileEntityComputer;
import org.teamavion.util.support.Reflection;
import org.teamavion.util.support.Result;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class GUIComputer extends GuiScreen{

    protected final int viewportMin = -90, viewportMax = 45, maxInputLines = 128, textFieldLen = 240, textColor = 0xFFFFFF;
    protected final long keyPressTimeout = 0; // timeout period between keypresses to minimize spam
    protected final ArrayList<GuiTextField> viewportInput = new ArrayList<>();
    protected final TileEntityComputer computer;
    protected boolean viewportUpdated = true;
    protected int selected = 0, viewportSelect = 0;
    protected long keyPressTime = 0;
    protected GuiTextField[] inputLines;
    protected GuiButton button;

    public GUIComputer(TileEntityComputer computer){ this.computer = computer; }

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
            StringBuilder sb = new StringBuilder();
            File f = File.createTempFile("exec", ".java");
            ArrayList<GuiTextField> skip = new ArrayList<>();
            for(GuiTextField t : inputLines) if(t.getText().startsWith("import ") && t.getText().endsWith(";")){ skip.add(t); sb.append(t.getText()); }
            sb.append("public class ").append(f.getName().substring(0, f.getName().length() - 5)).append("{public static void main(String[] args){");
            for(GuiTextField t : inputLines) if(!skip.contains(t)) sb.append(t.getText());
            sb.append("}}");
            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
            try (OutputStream out = new FileOutputStream(f)) { out.write(sb.toString().getBytes()); }
            int result = jc.run(null, null, null, f.getAbsolutePath());
            if(result==0){
                //noinspection ResultOfMethodCallIgnored
                f.delete();
                f = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-5)+".class");
                byte[] b;
                try{
                    InputStream i = new FileInputStream(f);
                    ArrayList<Byte> a = new ArrayList<>();
                    byte[] b1 = new byte[4096];
                    int i1;
                    while(i.available()>0){
                        i1 = i.read(b1);
                        for(int j = 0; j<i1; ++j) a.add(b1[j]);
                    }
                    i.close();
                    b = new byte[a.size()];
                    for(int j = 0; j<a.size(); ++j) b[j] = a.get(j);
                }catch(Exception e){ return; }
                finally {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
                Result<Class<?>> compiled = (Result<Class<?>>) Reflection.invokeMethod(
                        Reflection.getMethod(ClassLoader.class, "defineClass", byte[].class, int.class, int.class),
                        GUIComputer.class.getClassLoader(),
                        b,
                        0,
                        b.length);
                if(compiled.success){
                    for(Method m : compiled.value.getDeclaredMethods())
                        if(m.getName().equals("main") && Modifier.isStatic(m.getModifiers()) && Arrays.equals(m.getParameterTypes(), new Class<?>[]{String[].class}))
                        {
                            m.setAccessible(true);
                            try {
                                m.invoke(null, (Object) new String[]{});
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        button.mouseReleased(mouseX, mouseY);
    }
}