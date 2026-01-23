package com.zebra.ai_multibarcodes_capture.eventinjection;

import android.util.SparseIntArray;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class KeyMappingHelper {

    private static final SparseIntArray keyCodeMap = new SparseIntArray();
    public static long delayBetweenKeys = 10; // delay in ms

    static {
        // ASCII characters
        keyCodeMap.put('a', KeyEvent.KEYCODE_A);
        keyCodeMap.put('b', KeyEvent.KEYCODE_B);
        keyCodeMap.put('c', KeyEvent.KEYCODE_C);
        keyCodeMap.put('d', KeyEvent.KEYCODE_D);
        keyCodeMap.put('e', KeyEvent.KEYCODE_E);
        keyCodeMap.put('f', KeyEvent.KEYCODE_F);
        keyCodeMap.put('g', KeyEvent.KEYCODE_G);
        keyCodeMap.put('h', KeyEvent.KEYCODE_H);
        keyCodeMap.put('i', KeyEvent.KEYCODE_I);
        keyCodeMap.put('j', KeyEvent.KEYCODE_J);
        keyCodeMap.put('k', KeyEvent.KEYCODE_K);
        keyCodeMap.put('l', KeyEvent.KEYCODE_L);
        keyCodeMap.put('m', KeyEvent.KEYCODE_M);
        keyCodeMap.put('n', KeyEvent.KEYCODE_N);
        keyCodeMap.put('o', KeyEvent.KEYCODE_O);
        keyCodeMap.put('p', KeyEvent.KEYCODE_P);
        keyCodeMap.put('q', KeyEvent.KEYCODE_Q);
        keyCodeMap.put('r', KeyEvent.KEYCODE_R);
        keyCodeMap.put('s', KeyEvent.KEYCODE_S);
        keyCodeMap.put('t', KeyEvent.KEYCODE_T);
        keyCodeMap.put('u', KeyEvent.KEYCODE_U);
        keyCodeMap.put('v', KeyEvent.KEYCODE_V);
        keyCodeMap.put('w', KeyEvent.KEYCODE_W);
        keyCodeMap.put('x', KeyEvent.KEYCODE_X);
        keyCodeMap.put('y', KeyEvent.KEYCODE_Y);
        keyCodeMap.put('z', KeyEvent.KEYCODE_Z);

        keyCodeMap.put('A', KeyEvent.KEYCODE_A);
        keyCodeMap.put('B', KeyEvent.KEYCODE_B);
        keyCodeMap.put('C', KeyEvent.KEYCODE_C);
        keyCodeMap.put('D', KeyEvent.KEYCODE_D);
        keyCodeMap.put('E', KeyEvent.KEYCODE_E);
        keyCodeMap.put('F', KeyEvent.KEYCODE_F);
        keyCodeMap.put('G', KeyEvent.KEYCODE_G);
        keyCodeMap.put('H', KeyEvent.KEYCODE_H);
        keyCodeMap.put('I', KeyEvent.KEYCODE_I);
        keyCodeMap.put('J', KeyEvent.KEYCODE_J);
        keyCodeMap.put('K', KeyEvent.KEYCODE_K);
        keyCodeMap.put('L', KeyEvent.KEYCODE_L);
        keyCodeMap.put('M', KeyEvent.KEYCODE_M);
        keyCodeMap.put('N', KeyEvent.KEYCODE_N);
        keyCodeMap.put('O', KeyEvent.KEYCODE_O);
        keyCodeMap.put('P', KeyEvent.KEYCODE_P);
        keyCodeMap.put('Q', KeyEvent.KEYCODE_Q);
        keyCodeMap.put('R', KeyEvent.KEYCODE_R);
        keyCodeMap.put('S', KeyEvent.KEYCODE_S);
        keyCodeMap.put('T', KeyEvent.KEYCODE_T);
        keyCodeMap.put('U', KeyEvent.KEYCODE_U);
        keyCodeMap.put('V', KeyEvent.KEYCODE_V);
        keyCodeMap.put('W', KeyEvent.KEYCODE_W);
        keyCodeMap.put('X', KeyEvent.KEYCODE_X);
        keyCodeMap.put('Y', KeyEvent.KEYCODE_Y);
        keyCodeMap.put('Z', KeyEvent.KEYCODE_Z);

        // Digits
        keyCodeMap.put('0', KeyEvent.KEYCODE_0);
        keyCodeMap.put('1', KeyEvent.KEYCODE_1);
        keyCodeMap.put('2', KeyEvent.KEYCODE_2);
        keyCodeMap.put('3', KeyEvent.KEYCODE_3);
        keyCodeMap.put('4', KeyEvent.KEYCODE_4);
        keyCodeMap.put('5', KeyEvent.KEYCODE_5);
        keyCodeMap.put('6', KeyEvent.KEYCODE_6);
        keyCodeMap.put('7', KeyEvent.KEYCODE_7);
        keyCodeMap.put('8', KeyEvent.KEYCODE_8);
        keyCodeMap.put('9', KeyEvent.KEYCODE_9);

        // Special Characters
        keyCodeMap.put(' ', KeyEvent.KEYCODE_SPACE);
        keyCodeMap.put('\n', KeyEvent.KEYCODE_ENTER);
        keyCodeMap.put('\t', KeyEvent.KEYCODE_TAB);
        keyCodeMap.put('\r', KeyEvent.KEYCODE_ENTER); // Carriage return
        keyCodeMap.put(27, KeyEvent.KEYCODE_ESCAPE); // Escape character

        // Punctuation and Other Symbols
        keyCodeMap.put('!', KeyEvent.KEYCODE_1); // shift + 1
        keyCodeMap.put('"', KeyEvent.KEYCODE_APOSTROPHE); // shift + '
        keyCodeMap.put('#', KeyEvent.KEYCODE_3); // shift + 3
        keyCodeMap.put('$', KeyEvent.KEYCODE_4); // shift + 4
        keyCodeMap.put('%', KeyEvent.KEYCODE_5); // shift + 5
        keyCodeMap.put('&', KeyEvent.KEYCODE_7); // shift + 7
        keyCodeMap.put('\'', KeyEvent.KEYCODE_APOSTROPHE);
        keyCodeMap.put('(', KeyEvent.KEYCODE_9); // shift + 9
        keyCodeMap.put(')', KeyEvent.KEYCODE_0); // shift + 0
        keyCodeMap.put('*', KeyEvent.KEYCODE_8); // shift + 8
        keyCodeMap.put('+', KeyEvent.KEYCODE_PLUS);
        keyCodeMap.put(',', KeyEvent.KEYCODE_COMMA);
        keyCodeMap.put('-', KeyEvent.KEYCODE_MINUS);
        keyCodeMap.put('.', KeyEvent.KEYCODE_PERIOD);
        keyCodeMap.put('/', KeyEvent.KEYCODE_SLASH);
        keyCodeMap.put(':', KeyEvent.KEYCODE_SEMICOLON); // shift + ;
        keyCodeMap.put(';', KeyEvent.KEYCODE_SEMICOLON);
        keyCodeMap.put('<', KeyEvent.KEYCODE_COMMA); // shift + ,
        keyCodeMap.put('=', KeyEvent.KEYCODE_EQUALS);
        keyCodeMap.put('>', KeyEvent.KEYCODE_PERIOD); // shift + .
        keyCodeMap.put('?', KeyEvent.KEYCODE_SLASH); // shift + /
        keyCodeMap.put('@', KeyEvent.KEYCODE_2); // shift + 2
        keyCodeMap.put('[', KeyEvent.KEYCODE_LEFT_BRACKET);
        keyCodeMap.put('\\', KeyEvent.KEYCODE_BACKSLASH);
        keyCodeMap.put(']', KeyEvent.KEYCODE_RIGHT_BRACKET);
        keyCodeMap.put('^', KeyEvent.KEYCODE_6); // shift + 6
        keyCodeMap.put('_', KeyEvent.KEYCODE_MINUS); // shift + -
        keyCodeMap.put('`', KeyEvent.KEYCODE_GRAVE);
        keyCodeMap.put('{', KeyEvent.KEYCODE_LEFT_BRACKET); // shift + [
        keyCodeMap.put('|', KeyEvent.KEYCODE_BACKSLASH); // shift + \
        keyCodeMap.put('}', KeyEvent.KEYCODE_RIGHT_BRACKET); // shift + ]
        keyCodeMap.put('~', KeyEvent.KEYCODE_GRAVE); // shift + `

        // Control Characters (additional if needed)
        keyCodeMap.put('\b', KeyEvent.KEYCODE_DEL); // Backspace

        // ASCII Control Characters (Optional)
        // Uncomment if you need these
        // for (char c = 0; c < 32; c++) {
        //     keyCodeMap.put(c, KeyEvent.KEYCODE_UNKNOWN); // Not standard, you may want to customize it
        // }
    }

    public static int getKeyCode(char character)
    {
        return keyCodeMap.get(character, KeyEvent.KEYCODE_UNKNOWN);
    }

    public static List<KeyEvent> stringToKeyEventList(String input) {
        List<KeyEvent> keyEvents = new ArrayList<>();
        long eventTime = System.currentTimeMillis();

        for (char c : input.toCharArray()) {
            int keyCode = getKeyCode(c);
            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                // Create KeyEvent for key down
                keyEvents.add(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0));
                // Create KeyEvent for key up
                keyEvents.add(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0));
            } else {
                // Handle unknown key code if needed
                System.out.println("Unknown key code for character: " + c);
            }
            //eventTime += delayBetweenKeys;
        }
        return keyEvents;
    }
}
