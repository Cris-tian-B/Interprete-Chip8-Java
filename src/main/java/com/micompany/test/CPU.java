/*
* Autor: Cris-tian-B
* Fecha: 9/26/2025
* Interprete Chip-8
* V0.9.0
*/
package com.micompany.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Toolkit;

public class CPU {
    private final byte[] fontSet; // Fuente del Chip-8
    private byte[] display; // Display (64x32 pixeles)
    private byte[] memory; // Memoria del Chip-8
    private int[] stack; // Pila del Chip-8
    private byte[] V; // V0-VF Registros
    private int pc; // Contador de Programa
    private int I; // Registro de Indice
    private int sp; // Puntero de la pila
    boolean sound; // Variable de condición para el sonido.
    private int delayTimer; // Delay Timer
    private int soundTimer; // Sound Timer
    private final byte[] keys; // Fuente del Chip-8

    public int getDisplayValue(int i) {
        return display[i];
    }

    public void getKeyValue(int i, int e) {
        keys[i] = (byte) e;
    }

    public CPU() {
        keys = new byte[16];
        sp = 0; // Puntero de la pila inicializado en 0
        sound = true; // Inicializar la variable de condición
        pc = 0x200; // Contador de Programa inicializado en 0x200
        I = 0; // Registro de Indice inicializado en 0x0000
        display = new byte[64 * 32]; // Initialize Display
        stack = new int[16]; // Pila de 16 elementos
        V = new byte[16]; // registros V0-VF
        memory = new byte[4096];
        delayTimer = 0;
        soundTimer = 0;
        fontSet = new byte[] { // Inicializar Fuente del Chip-8
                (byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0, // 0
                (byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70, // 1
                (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // 2
                (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 3
                (byte) 0x90, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10, // 4
                (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 5
                (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 6
                (byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40, // 7
                (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0, // 8
                (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0, // 9
                (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0x90, // A
                (byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0, // B
                (byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0, // C
                (byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0, // D
                (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0, // E
                (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80  // F
        };

        System.arraycopy(fontSet, 0, memory, 0, fontSet.length);

        // Cargar ROM desde un archivo
        byte[] ROM = null;
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setAcceptAllFileFilterUsed(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Chip8", "ch8");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.showOpenDialog(null);
        File file = fileChooser.getSelectedFile();

        try {
            FileInputStream fis = new FileInputStream(file);
            ROM = new byte[(int) file.length()];
            int bytesRead = fis.read(ROM);
            fis.close();
            if (bytesRead != ROM.length) {
                System.err.println("Could not read the entire file.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cargar la ROM en la memoria a partir de la direccion 0x200
        System.arraycopy(ROM, 0, memory, 0x200, ROM.length);
    }

    public void executeCycle() {
        int x, y, nn, opt;
        int opcode = (memory[pc] & 0xFF) << 8 | (memory[pc + 1] & 0xFF);
        int firstNibble = (opcode & 0XF000) >>> 12;
        /*for (int i = 0; i < 10; i++) {
            System.out.printf("memory[%d] = 0x%02X (%d)%n",
                    i, memory[i] & 0xFF, memory[i] & 0xFF);
        }
        
          System.out.println("Opcode [" + String.format("%04X", opcode)
          + "] en PC: " + String.format("%04X", pc));
         */

        switch (firstNibble) {
            case 0:
                if (opcode == 0x00E0) { // 00E0 Limpiar pantalla
                    Arrays.fill(display, (byte) 0);
                    pc += 2;
                    break;
                }

                if (opcode == 0x00EE) { // 00EE Volver de subrutina
                    sp--;
                    pc = stack[sp];
                    pc += 2;
                    break;
                }

                if (opcode == 0x00FF) { // 00FF Salir del programa
                    System.out.println("Programa terminado.");
                    break;
                }

                break;

            case 1: // 1NNN Saltar a la direccion NNN
                pc = opcode & 0x0FFF; // NNN
                break;

            case 2: // 2NNN Llamar a subrutina en NNN
                stack[sp] = pc;
                sp++;
                pc = opcode & 0x0FFF;
                break;

            case 3: // 3xNN salto condicional si vX = NN
                x = (opcode & 0x0F00) >>> 8;
                nn = opcode & 0x00FF;
                pc += 2;
                if ((V[x] & 0xFF) == nn) {
                    pc += 2; // Saltar 2 bytes
                }
                break;

            case 4: // 4XNN Salto condicional si vX != NN
                x = (opcode & 0x0F00) >>> 8;
                nn = opcode & 0x00FF;
                pc += 2;
                if ((V[x] & 0xFF) != nn) {
                    pc += 2; // Saltar 2 bytes
                }
                break;

            case 5: // 5XY0 Salto condicional si vX == vY
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                pc += 2;
                if ((V[x] & 0xFF) == (V[y] & 0xFF)) {
                    pc += 2;
                }
                break;

            case 6: // 6XNN Cargar valor vX de NN
                x = (opcode & 0x0F00) >>> 8;
                nn = opcode & 0x00FF;
                V[x] = (byte) nn;
                pc += 2;
                break;

            case 7: // 7XNN Sumar valor vX de NN
                x = (opcode & 0x0F00) >>> 8;
                nn = opcode & 0x00FF;
                V[x] = (byte) ((V[x] + nn) & 0xFF); // Wrap-around a 8 bits
                pc += 2;
                break;

            case 8: // 8XYN Operaciones con registros
                opt = (opcode & 0x000F);

                if (opt == 0) { // 8XY0 Cargar vX en vY
                    x = (opcode & 0x0F00) >>> 8;
                    y = (opcode & 0x00F0) >>> 4;
                    V[x] = V[y];
                    pc += 2;
                    break;
                }

                if (opt == 1) { // 8XY1 OR vX con vY
                    x = (opcode & 0x0F00) >>> 8;
                    y = (opcode & 0x00F0) >>> 4;
                    V[x] |= V[y];
                    V[0xF] = 0;
                    pc += 2;
                    break;
                }

                if (opt == 2) { // 8XY2 AND vX con vY
                    x = (opcode & 0x0F00) >>> 8;
                    y = (opcode & 0x00F0) >>> 4;
                    V[x] &= V[y];
                    V[0xF] = 0;
                    pc += 2;
                    break;
                }

                if (opt == 3) { // 8XY3 XOR vX con vY
                    x = (opcode & 0x0F00) >>> 8;
                    y = (opcode & 0x00F0) >>> 4;
                    V[x] ^= V[y];
                    V[0xF] = 0;
                    pc += 2;
                    break;
                }

                if (opt == 4) { // 8XY4 Sumar vX con vY
                    x = (opcode & 0x0F00) >>> 8;
                    y = (opcode & 0x00F0) >>> 4;
                    int sm = (V[x] & 0xFF) + (V[y] & 0xFF);
                    V[0xF] = (byte) ((sm > 255) ? 1 : 0);
                    V[x] = (byte) sm;
                    pc += 2;
                    break;
                }

                if (opt == 5) { // 8XY5 Resta vY de vX
                    x = (opcode & 0x0F00) >>> 8;
                    y = (opcode & 0x00F0) >>> 4;
                    int vX = V[x] & 0xFF;
                    int vY = V[y] & 0xFF;
                    V[0xF] = (byte) (vX >= vY ? 1 : 0); // Flag
                    V[x] = (byte) ((vX - vY) & 0xFF); // Resta
                    pc += 2;
                    break;
                }

                if (opt == 6) { // 8XY6 Desplazar vX a la derecha
                    x = (opcode & 0x0F00) >>> 8;
                    V[0xF] = (byte) (V[x] & 0x1);
                    V[x] = (byte) ((V[x] & 0xFF) >>> 1); // Desplazamiento lógico a la derecha
                    pc += 2;
                    break;
                }

                if (opt == 7) { // 8XY7 Resta vY de vX
                    x = (opcode & 0x0F00) >>> 8;
                    y = (opcode & 0x00F0) >>> 4;
                    int vxVal = V[x] & 0xFF;
                    int vyVal = V[y] & 0xFF;
                    V[0xF] = (byte) (vyVal >= vxVal ? 1 : 0);
                    V[x] = (byte) ((vyVal - vxVal) & 0xFF);
                    pc += 2;
                    break;
                }

                if (opt == 0xE) { // 8XYE: Shift left Vx
                    x = (opcode & 0x0F00) >>> 8;
                    V[0xF] = (byte) ((V[x] & 0x80) >>> 7);
                    V[x] = (byte) ((V[x] & 0xFF) << 1);
                    pc += 2;
                    break;
                }
                break;

            case 9: // 9xY0 Salto condicional si vX != vY
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                pc += 2;
                if ((V[x] & 0xFF) != (V[y] & 0xFF)) {
                    pc += 2;
                }
                break;

            case 10: // ANNN Cargar valor I de NNN
                I = opcode & 0x0FFF; /// NNN
                pc += 2;
                break;

            case 11: // BNNN Saltar a la direccion NNN + v0
                int nnn = opcode & 0x0FFF;
                pc = nnn + (V[0] & 0xFF);
                break;

            case 12: // CxNN Generar numero aleatorio AND NN
                x = (opcode & 0x0F00) >>> 8;
                nn = opcode & 0x00FF;
                int rdm = (int) (Math.random() * 256) & 0xFF;
                V[x] = (byte) (rdm & nn);
                pc += 2;
                break;

            case 13: // DXYN Dibujar sprite en pantalla
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                int n = opcode & 0x000F;
                V[0x0F] = 0;

                for (int row = 0; row < n; row++) {
                    byte spriteByte = memory[I + row];
                    int spriteY = ((V[y] & 0xFF) + row) % 32;
                    int baseIndex = spriteY * 64;

                    for (int col = 0; col < 8; col++) {
                        // Check if this sprite pixel is set
                        if ((spriteByte & (0x80 >> col)) != 0) {
                            int spriteX = ((V[x] & 0xFF) + col) % 64;
                            int pixelIndex = baseIndex + spriteX;

                            // Colicion
                            if (display[pixelIndex] == 1) {
                                V[0x0F] = 1;
                            }
                            // XOR
                            display[pixelIndex] ^= 1;
                        }
                    }
                }

                pc += 2;
                break;

            case 14: // EX9E y EXA1 Operaciones con teclado
                opt = opcode & 0x00FF;

                if (opt == 0x9E) { // EX9E: Saltar si la tecla en Vx está presionada
                    x = (opcode & 0x0F00) >>> 8;
                    pc += 2;
                    int key = V[x] & 0x0F;
                    if (keys[key] != 0) {
                        pc += 2;
                    }
                    break;
                }
                if (opt == 0xA1) { // EXA1: Saltar si la tecla en Vx NO está presionada
                    x = (opcode & 0x0F00) >>> 8;
                    pc += 2;
                    int key = V[x] & 0x0F;
                    if (keys[key] == 0) {
                        pc += 2;
                    }
                    break;
                }
                break;

            case 15: // FXNN Operaciones con registros y memoria
                opt = opcode & 0x00FF;

                if (opt == 0x07) { // FX07 Cargar vX de delay timer
                    x = (opcode & 0x0F00) >>> 8;
                    V[x] = (byte) (delayTimer & 0xFF);
                    pc += 2;
                    break;
                }

                if (opt == 0x15) { // FX15 Cargar delay timer de vX
                    x = (opcode & 0x0F00) >>> 8;
                    delayTimer = (V[x] & 0xFF);
                    pc += 2;
                    break;
                }

                if (opt == 0x18) { // FX18 Cargar sound timer de vX
                    x = (opcode & 0x0F00) >>> 8;
                    soundTimer = (V[x] & 0xFF);
                    pc += 2;
                    break;
                }

                if (opt == 0x29) { // FX29 Cargar sprite de vX en I
                    x = (opcode & 0x0F00) >>> 8;
                    I = (V[x] & 0xFF) * 5; // Cada sprite ocupa 5 bytes
                    pc += 2;
                    break;
                }

                if (opt == 0x33) { // Fx33 Almacena BCD de vX en memoria
                    x = (opcode & 0x0F00) >>> 8;
                    int val = V[x] & 0xFF;
                    memory[I] = (byte) (val / 100);
                    memory[I + 1] = (byte) ((val % 100) / 10);
                    memory[I + 2] = (byte) (val % 10);
                    pc += 2;
                    break;
                }

                if (opt == 0x55) { // FX55 Almacenar v0 a vX en memoria
                    x = (opcode & 0x0F00) >>> 8;
                    for (int i = 0; i <= x; i++) {
                        memory[I + i] = V[i];
                    }
                    pc += 2;
                    break;
                }

                if (opt == 0x65) { // FX65 Rellenar v0 a vX con valores de memoria
                    x = (opcode & 0x0F00) >>> 8;
                    for (int i = 0; i <= x; i++) {
                        V[i] = memory[I + i];
                    }
                    pc += 2;
                    break;
                }

                if (opt == 0x1E) { // FX1E Sumar I con vX
                    x = (opcode & 0x0F00) >>> 8;
                    I = (I + (V[x] & 0xFF)) & 0xFFFF;
                    pc += 2;
                    break;
                }

                if (opt == 0x0A) { // FX0A con timeout
                    x = (opcode & 0x0F00) >>> 8;
                    boolean keyPress = false;
                    for (int i = 0; i < keys.length; i++) {
                        if (keys[i] != 0) {
                            V[x] = (byte) i;
                            keyPress = true;
                            break;
                        }
                    }
                    if (!keyPress) {
                        pc -= 2;
                    } else {
                        pc += 2;
                    }
                }
                break;

            default:
                System.out.println("Opcode no reconocido: " + String.format("%04X", opcode));
                break;
        }
        if (delayTimer > 0)
            delayTimer--;
        if (soundTimer > 0) {
            if (sound) {
                Toolkit.getDefaultToolkit().beep();
            }
            soundTimer--;
        }
    }
}
